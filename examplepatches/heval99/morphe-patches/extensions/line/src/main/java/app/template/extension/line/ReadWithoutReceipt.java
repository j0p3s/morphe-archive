// The viewing-session model (reserve a chat, claim it in ChatHistoryActivity.onCreate, block reads
// for that chat until the activity finishes, survive configuration changes) follows uta-a/linimal
// (GPL-3.0), extensions ReadWithoutReceiptHooks / ReadWithoutReceiptForegroundTracker /
// NotificationReadWithoutReceiptHooks. The menu row and its label are new: LINE 26.15 builds the
// chat-list long-press menu from a list of (label, action) items, so no Compose code is cloned.

package app.template.extension.line;

import android.app.Activity;
import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.SystemClock;
import android.util.Log;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Locale;
import java.util.Map;

/**
 * "Read without receipt": a chat opened from the chat-list menu row, or from a message
 * notification, is shown without marking it read. LINE's read worker and its sendChatChecked call
 * ask {@link #shouldBlockRead(String)} and skip both the local read (the unread badge stays) and
 * the receipt for that chat, until its ChatHistoryActivity finishes.
 *
 * <p>Sessions live in memory: they survive configuration changes and the system destroying a
 * backgrounded chat screen, but not the process being killed (the chat then reads as stock).
 * Every entry point fails open: on any error LINE behaves as stock.
 */
@SuppressWarnings("unused")
public final class ReadWithoutReceipt {
    private static final String TAG = "MorpheLineReadNoReceipt";

    /**
     * Label "resource id" of the injected menu row. Package byte 0xFF never exists, so it cannot
     * collide with a real resource; the patched label lambdas use {@link #menuLabel(int)} for it.
     */
    public static final int MENU_LABEL_ID = 0xFF4C4E31;

    /**
     * A reservation that no ChatHistoryActivity claimed within this time is dropped. Short, so that
     * a launch that never reached the chat screen does not turn a later normal open into a ghost read.
     */
    private static final long RESERVATION_TIMEOUT_MILLIS = 60 * 1000L;

    private static final String SESSION_TOKEN_KEY = "app.template.line.readWithoutReceipt.token";
    private static final String CHAT_URI_PREFIX = "line://nv/openChatroom/?id=";
    private static final String REQUEST_EXTRA = "chatHistoryRequest";

    /** Chat types whose reads go through the patched worker (1:1, group, multi-person room). */
    private static final String[] MENU_CHAT_TYPES = {"SINGLE", "GROUP", "ROOM"};

    private static final Object LOCK = new Object();
    /** One session per chat id. Guarded by {@link #LOCK}. */
    private static final Map<String, Session> SESSIONS = new HashMap<>();
    private static long nextToken;
    private static boolean callbacksRegistered;
    private static volatile Field notificationMarker;

    private static final class Session {
        final long token;
        final long reservedAt;
        boolean claimed;
        /**
         * The chat screen showing this session; null while reserved, and while detached because the
         * screen is being recreated (configuration change, or destroyed in the background).
         */
        Activity owner;

        Session(long token, long reservedAt) {
            this.token = token;
            this.reservedAt = reservedAt;
        }

        boolean isFresh(long now) {
            return now - reservedAt <= RESERVATION_TIMEOUT_MILLIS;
        }
    }

    private ReadWithoutReceipt() {
    }

    // ---------------------------------------------------------------------------------------------
    // Chat-list menu row
    // ---------------------------------------------------------------------------------------------

    /**
     * Called while the chat list's long-press menu builds its rows.
     *
     * @param chatId   the long-pressed chat.
     * @param chatType LINE's chat type enum constant.
     * @return {@link #MENU_LABEL_ID} to add the row, 0 to leave the menu as is.
     */
    public static int menuRowLabel(String chatId, Object chatType) {
        try {
            if (chatId == null || chatId.isEmpty() || !(chatType instanceof Enum)) return 0;
            String type = ((Enum<?>) chatType).name();
            for (String allowed : MENU_CHAT_TYPES) {
                if (allowed.equals(type)) return MENU_LABEL_ID;
            }
        } catch (Throwable t) {
            Log.w(TAG, "menuRowLabel", t);
        }
        return 0;
    }

    /**
     * Puts the injected row at the top of the menu when {@link #menuRowLabel} allowed it.
     *
     * @param rows    the menu's row list, as LINE built it.
     * @param row     the row item; its label is {@code labelId}.
     * @param labelId {@link #menuRowLabel}'s result.
     */
    public static void addMenuRow(ArrayList<Object> rows, Object row, int labelId) {
        try {
            if (labelId != 0 && rows != null && row != null) rows.add(0, row);
        } catch (Throwable t) {
            Log.w(TAG, "addMenuRow", t);
        }
    }

    /** The row's text for {@link #MENU_LABEL_ID}, or null for every real resource id. */
    public static String menuLabel(int labelId) {
        if (labelId != MENU_LABEL_ID) return null;
        Locale locale = Locale.getDefault();
        switch (locale.getLanguage()) {
            case "ja":
                return "既読をつけずに読む";
            case "zh":
                String country = locale.getCountry();
                if ("Hans".equals(locale.getScript()) || "CN".equals(country) || "SG".equals(country)) {
                    return "阅读但不显示已读";
                }
                return "閱讀但不顯示已讀";
            case "th":
                return "อ่านโดยไม่แสดงว่าอ่านแล้ว";
            case "in":
            case "id":
                return "Baca tanpa tanda dibaca";
            default:
                return "Read without receipt";
        }
    }

    /**
     * Called by the menu's action dispatcher after it closed the menu.
     *
     * @return true when the action was the injected row's (the dispatcher then returns at once).
     */
    public static boolean onMenuAction(Object action) {
        if (!(action instanceof ReadWithoutReceiptMenuAction)) return false;
        String chatId = ((ReadWithoutReceiptMenuAction) action).chatId;
        try {
            open(chatId);
        } catch (Throwable t) {
            Log.w(TAG, "Could not open the chat without receipt", t);
            drop(chatId);
        }
        return true;
    }

    private static void open(String chatId) {
        Context context = applicationContext();
        if (context == null || chatId == null || chatId.isEmpty()) return;
        reserve(chatId);
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(CHAT_URI_PREFIX + Uri.encode(chatId)));
        intent.setPackage(context.getPackageName());
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(intent);
    }

    // ---------------------------------------------------------------------------------------------
    // Notifications
    // ---------------------------------------------------------------------------------------------

    /** Called when LINE dispatches a chat launch intent; reserves the chat if it came from a notification. */
    public static void onChatLaunch(Intent intent) {
        try {
            Object request = request(intent);
            if (request != null && isFromNotification(request)) {
                reserve(chatId(request));
            }
        } catch (Throwable t) {
            Log.w(TAG, "onChatLaunch", t);
        }
    }

    /** BackNavigationArgs keeps isFromNotification in its only public instance boolean (checked by the patch). */
    private static boolean isFromNotification(Object request) throws ReflectiveOperationException {
        Object navigation = request.getClass().getMethod("getBackNavigation").invoke(request);
        if (navigation == null) return false;
        Field field = notificationMarker;
        if (field == null || field.getDeclaringClass() != navigation.getClass()) {
            field = null;
            for (Field candidate : navigation.getClass().getFields()) {
                if (candidate.getType() == boolean.class && !Modifier.isStatic(candidate.getModifiers())) {
                    if (field != null) throw new NoSuchFieldException("isFromNotification is not unique");
                    field = candidate;
                }
            }
            if (field == null) throw new NoSuchFieldException("isFromNotification not found");
            notificationMarker = field;
        }
        return field.getBoolean(navigation);
    }

    // ---------------------------------------------------------------------------------------------
    // Sessions
    // ---------------------------------------------------------------------------------------------

    /** Reserves [chatId]; replaces an older session of the same chat, leaves other chats' alone. */
    private static void reserve(String chatId) {
        if (chatId == null || chatId.isEmpty()) return;
        synchronized (LOCK) {
            SESSIONS.put(chatId, new Session(++nextToken, SystemClock.elapsedRealtime()));
        }
    }

    private static void drop(String chatId) {
        if (chatId == null) return;
        synchronized (LOCK) {
            SESSIONS.remove(chatId);
        }
    }

    /** Called right after ChatHistoryActivity's super.onCreate. */
    public static void onChatCreated(Activity activity, Bundle savedState) {
        String chatId = null;
        try {
            chatId = chatId(request(activity.getIntent()));
            long savedToken = savedState == null ? 0L : savedState.getLong(SESSION_TOKEN_KEY, 0L);
            synchronized (LOCK) {
                long now = SystemClock.elapsedRealtime();
                dropExpiredReservations(now);
                Session session = chatId == null ? null : SESSIONS.get(chatId);
                if (session == null) return;
                if (savedToken != 0L) {
                    // A recreated screen takes its detached session back; nothing else changes.
                    if (session.token == savedToken && session.claimed && session.owner == null) {
                        session.owner = activity;
                    }
                    return;
                }
                if (!session.claimed && session.isFresh(now)) {
                    session.claimed = true;
                    session.owner = activity;
                    registerCallbacks(activity);
                } else if (session.claimed) {
                    // A new, normal open of a chat whose earlier no-receipt screen is gone: read as stock.
                    if (session.owner == null || session.owner.isFinishing()) SESSIONS.remove(chatId);
                }
            }
        } catch (Throwable t) {
            Log.w(TAG, "onChatCreated", t);
            drop(chatId);
        }
    }

    /** Called at the start of LINE's read worker and of its sendChatChecked call. */
    public static boolean shouldBlockRead(String chatId) {
        if (chatId == null) return false;
        try {
            synchronized (LOCK) {
                Session session = SESSIONS.get(chatId);
                if (session == null) return false;
                if (session.claimed || session.isFresh(SystemClock.elapsedRealtime())) return true;
                SESSIONS.remove(chatId);
                return false;
            }
        } catch (Throwable t) {
            return false;
        }
    }

    private static void dropExpiredReservations(long now) {
        Iterator<Session> sessions = SESSIONS.values().iterator();
        while (sessions.hasNext()) {
            Session session = sessions.next();
            if (!session.claimed && !session.isFresh(now)) sessions.remove();
        }
    }

    private static void registerCallbacks(Activity activity) {
        if (callbacksRegistered) return;
        Application application = activity.getApplication();
        if (application == null) return;
        application.registerActivityLifecycleCallbacks(new Application.ActivityLifecycleCallbacks() {
            @Override
            public void onActivityCreated(Activity a, Bundle b) {
            }

            @Override
            public void onActivityStarted(Activity a) {
            }

            @Override
            public void onActivityResumed(Activity a) {
            }

            @Override
            public void onActivityPaused(Activity a) {
            }

            @Override
            public void onActivityStopped(Activity a) {
                // Leaving the chat for good ends the session at once, before LINE's on-exit work.
                if (a.isFinishing() && !a.isChangingConfigurations()) end(a, false);
            }

            @Override
            public void onActivitySaveInstanceState(Activity a, Bundle outState) {
                synchronized (LOCK) {
                    for (Session session : SESSIONS.values()) {
                        if (session.owner == a) outState.putLong(SESSION_TOKEN_KEY, session.token);
                    }
                }
            }

            @Override
            public void onActivityDestroyed(Activity a) {
                // Recreated later (configuration change, or destroyed in the background): keep the
                // session for the new screen. Finished: end it.
                end(a, a.isChangingConfigurations() || !a.isFinishing());
            }
        });
        callbacksRegistered = true;
    }

    private static void end(Activity activity, boolean detachOnly) {
        synchronized (LOCK) {
            Iterator<Session> sessions = SESSIONS.values().iterator();
            while (sessions.hasNext()) {
                Session session = sessions.next();
                if (session.owner != activity) continue;
                if (detachOnly) {
                    session.owner = null;
                } else {
                    sessions.remove();
                }
            }
        }
    }

    // ---------------------------------------------------------------------------------------------
    // Helpers
    // ---------------------------------------------------------------------------------------------

    @SuppressWarnings("deprecation") // getParcelable(String, Class) needs API 33; minSdk is lower.
    private static Object request(Intent intent) {
        if (intent == null) return null;
        Bundle extras = intent.getExtras();
        return extras == null ? null : extras.getParcelable(REQUEST_EXTRA);
    }

    private static String chatId(Object request) throws ReflectiveOperationException {
        if (request == null) return null;
        Object id = request.getClass().getMethod("getChatId").invoke(request);
        return id instanceof String ? (String) id : null;
    }

    private static Context applicationContext() {
        try {
            return (Context) Class.forName("android.app.ActivityThread")
                    .getMethod("currentApplication")
                    .invoke(null);
        } catch (Throwable t) {
            return null;
        }
    }
}

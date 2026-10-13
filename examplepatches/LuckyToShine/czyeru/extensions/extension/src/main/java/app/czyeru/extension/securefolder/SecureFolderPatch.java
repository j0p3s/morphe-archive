package app.czyeru.extension.securefolder;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.DialogInterface;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

/**
 * Integrates Samsung "Move to Secure Folder" into Iris Gallery.
 * <p>
 * The patch hooks the click handler behind the "Move to Locked" action. On a device where
 * Secure Folder is available, the user is asked where the selected items should go. On every
 * other device, and whenever anything about the selection looks unexpected, the original
 * handler runs untouched.
 */
@SuppressWarnings("unused")
public final class SecureFolderPatch {
    private static final String TAG = "SecureFolderPatch";

    // Iris Gallery 0.8.0 (versionCode 16) is obfuscated. These names are only valid for it.
    /** Media model: com.iris.gallery "MediaImage". */
    private static final String MEDIA_CLASS = "np1";
    /** MediaImage.path, the absolute file path taken from MediaStore "_data". */
    private static final String MEDIA_PATH_FIELD = "g";
    /** Original handler method name: Function1.a(Object). */
    private static final String HANDLER_METHOD = "a";

    /** Samsung Gallery moves at most this many files per call on devices without unlimited moves. */
    private static final int MAX_PATHS_PER_CALL = 500;

    /** Set while the original handler is re-invoked after the user picked "Iris Locked". */
    private static final ThreadLocal<Boolean> PROCEEDING = new ThreadLocal<>();

    private SecureFolderPatch() {
    }

    /**
     * Called first thing from Iris Gallery's "Move to Locked" handler.
     *
     * @param handler the original handler instance, re-invoked if the user keeps the Iris vault.
     * @param context context the handler was created with (normally the Activity).
     * @param items   the selected media, a {@code List} of Iris Gallery media objects.
     * @return {@code true} if this call took over (the original handler must not run),
     * {@code false} to let the original handler run unchanged.
     */
    public static boolean interceptMoveToLocked(Object handler, Context context, Object items) {
        try {
            if (Boolean.TRUE.equals(PROCEEDING.get())) return false;
            if (handler == null || context == null || !(items instanceof List)) return false;

            List<?> selection = (List<?>) items;
            if (selection.isEmpty()) return false;

            Activity activity = findActivity(context);
            if (activity == null || activity.isFinishing()) return false;

            ArrayList<String> paths = extractPaths(selection);
            if (paths == null) return false;

            if (!SecureFolderHelper.isSecureFolderAvailable(activity)) return false;

            showChooser(activity, handler, selection, paths);
            return true;
        } catch (Throwable t) {
            Log.w(TAG, "Secure Folder hook failed, falling back to Iris Locked", t);
            return false;
        }
    }

    /**
     * Moves files to Secure Folder from any thread. The move runs in the background and the
     * result is reported with a toast.
     *
     * @param context   Application or Activity context.
     * @param filePaths absolute paths of the files to move.
     */
    public static void onMoveToSecureFolderClicked(Context context, List<String> filePaths) {
        final Context appContext = context.getApplicationContext() != null
                ? context.getApplicationContext() : context;
        final ArrayList<String> paths = new ArrayList<>(filePaths);

        if (!SecureFolderHelper.isSecureFolderAvailable(appContext)) {
            toast(appContext, "Secure Folder is not available or enabled on this Samsung device");
            return;
        }

        toast(appContext, "Moving " + paths.size() + " item(s) to Secure Folder...");
        new Thread(new Runnable() {
            @Override
            public void run() {
                boolean success = true;
                for (int start = 0; start < paths.size(); start += MAX_PATHS_PER_CALL) {
                    int end = Math.min(start + MAX_PATHS_PER_CALL, paths.size());
                    List<String> batch = new ArrayList<>(paths.subList(start, end));
                    if (!SecureFolderHelper.moveToSecureFolder(appContext, batch)) {
                        success = false;
                        break;
                    }
                }
                if (!success) toast(appContext, "Failed to move files to Secure Folder");
            }
        }, "SecureFolderMove").start();
    }

    private static void showChooser(final Activity activity, final Object handler,
                                    final List<?> selection, final ArrayList<String> paths) {
        new AlertDialog.Builder(activity)
                .setTitle("Move " + paths.size() + " item(s) to…")
                .setPositiveButton("Secure Folder", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        onMoveToSecureFolderClicked(activity, paths);
                    }
                })
                .setNegativeButton("Iris Locked", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        runOriginalHandler(activity, handler, selection);
                    }
                })
                .setNeutralButton(android.R.string.cancel, null)
                .show();
    }

    private static void runOriginalHandler(Context context, Object handler, List<?> selection) {
        PROCEEDING.set(Boolean.TRUE);
        try {
            handler.getClass().getMethod(HANDLER_METHOD, Object.class).invoke(handler, selection);
        } catch (Throwable t) {
            Log.e(TAG, "Original Move to Locked handler failed", t);
            toast(context, "Failed to move items to Locked");
        } finally {
            PROCEEDING.remove();
        }
    }

    /**
     * Reads the file path of every selected item. Returns {@code null} when the selection is not
     * made of the expected media objects or any path is missing, so the caller can fall back.
     */
    private static ArrayList<String> extractPaths(List<?> selection) throws ReflectiveOperationException {
        ArrayList<String> paths = new ArrayList<>(selection.size());
        Field pathField = null;
        for (Object item : selection) {
            if (item == null || !MEDIA_CLASS.equals(item.getClass().getName())) return null;
            if (pathField == null) {
                pathField = item.getClass().getDeclaredField(MEDIA_PATH_FIELD);
                pathField.setAccessible(true);
            }
            Object value = pathField.get(item);
            if (!(value instanceof String) || ((String) value).isEmpty()) return null;
            paths.add((String) value);
        }
        return paths;
    }

    private static Activity findActivity(Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof Activity) return (Activity) context;
            context = ((ContextWrapper) context).getBaseContext();
        }
        return null;
    }

    private static void toast(final Context context, final String message) {
        new Handler(Looper.getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
            }
        });
    }
}

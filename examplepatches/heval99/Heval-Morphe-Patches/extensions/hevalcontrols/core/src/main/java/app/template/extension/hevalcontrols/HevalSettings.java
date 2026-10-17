// Based on SysAdminDoc/HushMessenger (GPL-3.0), extensions/messenger/src/main/java/app/hushmessenger/extension/Settings.java:
// the enabled() rule (installed && !paused && !safe mode && choice) and the privacy-safe hook-failure record
// (exception class and first own frame, never the message); modified 2026-10-14: rewritten for minSdk 23 as an
// immutable snapshot behind a volatile field, a patch-time registry with per-feature defaults and dependencies,
// commit() writes and fail-open semantics.

package app.template.extension.hevalcontrols;

import android.content.Context;
import android.util.Log;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

/**
 * The Heval Controls settings engine. Patched hooks call {@link #isEnabled(String)}:
 * <pre>
 * const-string vX, "app.feature"
 * invoke-static/range {vX .. vX}, Lapp/template/extension/hevalcontrols/HevalSettings;->isEnabled(Ljava/lang/String;)Z
 * move-result vX
 * if-eqz vX, :stock
 * </pre>
 * Rules, in order:
 * <ol>
 * <li>Before the engine is initialised (no context yet): the feature's registered default.</li>
 * <li>Engine failure, global pause or safe mode: false (stock behaviour) for every feature.</li>
 * <li>Unknown id, unsupported registry, or a dependency that is off or missing: false.</li>
 * <li>Otherwise the user's choice, or the registered default when there is none.</li>
 * </ol>
 * Any exception answers false. "Fail-open" here means the app keeps working and falls back to
 * stock behaviour; it never means a modification is forced on.
 */
@SuppressWarnings("unused")
public final class HevalSettings {
    private static final String TAG = "HevalControls";

    /** Preference key prefix of a feature choice: {@code f.<featureId>}. */
    public static final String FEATURE_PREFIX = "f.";
    static final String KEY_PAUSED = "_paused";
    static final String KEY_SAFE_MODE = "_safe_mode";
    static final String KEY_THEME = "_theme";

    public static final String THEME_SYSTEM = "system";
    public static final String THEME_LIGHT = "light";
    public static final String THEME_DARK = "dark";
    public static final String THEME_AMOLED = "amoled";

    /** What the settings screen shows for a feature. */
    public enum State { ENABLED, DISABLED, RESTART_REQUIRED, UNSUPPORTED, FAILED, BUILD_TIME_ONLY }

    private static final Object LOCK = new Object();
    /** Null until the engine is initialised. Immutable once published. */
    private static volatile Snapshot snapshot;
    /** The first snapshot of this process: what start-only hooks read. */
    private static volatile Snapshot baseline;
    /** Strong reference: it is also the SharedPreferences change listener (see PrefsStore). */
    private static volatile Store store;
    private static volatile String initSource = "none";
    private static volatile String engineError;
    private static final Map<String, String> failures = new ConcurrentHashMap<>();

    // ---------------------------------------------------------------------------------------
    // Hot path
    // ---------------------------------------------------------------------------------------

    /**
     * Whether the patched behaviour of {@code featureId} should run. No file IO, no allocation
     * once the engine is ready: one volatile read and one HashMap lookup.
     */
    public static boolean isEnabled(String featureId) {
        try {
            Snapshot current = snapshot;
            if (current == null) {
                current = initLazily();
                if (current == null) return Registry.get().defaultFor(featureId);
            }
            Boolean on = current.effective.get(featureId);
            return on != null && on;
        } catch (Throwable failure) {
            return false;
        }
    }

    // ---------------------------------------------------------------------------------------
    // Initialisation
    // ---------------------------------------------------------------------------------------

    /** Called by the init provider before Application.onCreate. Idempotent and thread-safe. */
    public static void init(Context context) {
        init(context, "provider");
    }

    static Snapshot init(Context context, String source) {
        Snapshot current = snapshot;
        if (current != null) return current;
        synchronized (LOCK) {
            if (snapshot != null) return snapshot;
            Store created;
            try {
                Context app = context.getApplicationContext();
                created = new PrefsStore(app != null ? app : context);
            } catch (Throwable failure) {
                return publishFailure(failure, source);
            }
            return attach(created, source);
        }
    }

    /** First hook before the provider ran (or in a process without it): find the context lazily. */
    private static Snapshot initLazily() {
        Context app = AppContext.currentApplication();
        // Null inside Application.attachBaseContext: answer the default and retry on the next call.
        if (app == null) return null;
        return init(app, "lazy");
    }

    private static Snapshot attach(Store created, String source) {
        store = created;
        initSource = source;
        created.setOnChange(new Runnable() {
            @Override
            public void run() {
                rebuild();
            }
        });
        Snapshot built = build(created);
        baseline = built;
        snapshot = built;
        return built;
    }

    private static Snapshot publishFailure(Throwable failure, String source) {
        engineError = describe(failure);
        Log.e(TAG, "Settings engine unavailable, every feature stays stock: " + engineError);
        initSource = source;
        Snapshot failed = Snapshot.failed();
        baseline = failed;
        snapshot = failed;
        return failed;
    }

    /** Rebuilds the snapshot after the store changed. */
    static void rebuild() {
        synchronized (LOCK) {
            Store current = store;
            if (current != null) snapshot = build(current);
        }
    }

    private static Snapshot build(Store source) {
        try {
            Snapshot built = Snapshot.from(Registry.get(), source.readAll());
            engineError = null;
            return built;
        } catch (Throwable failure) {
            engineError = describe(failure);
            Log.e(TAG, "Settings could not be read, every feature stays stock: " + engineError);
            return Snapshot.failed();
        }
    }

    // ---------------------------------------------------------------------------------------
    // Writes (settings screen, crash guard). Synchronous commit(), then an immediate rebuild.
    // ---------------------------------------------------------------------------------------

    /** Saves a feature choice. Unknown and build-time-only ids are ignored (false). */
    public static boolean setEnabled(String featureId, boolean enabled) {
        Feature feature = Registry.get().feature(featureId);
        if (feature == null || feature.buildTimeOnly) return false;
        return write(Collections.<String, Object>singletonMap(FEATURE_PREFIX + featureId, enabled), null);
    }

    public static boolean setPaused(boolean paused) {
        return write(Collections.<String, Object>singletonMap(KEY_PAUSED, paused), null);
    }

    public static boolean setSafeMode(boolean safeMode) {
        return write(Collections.<String, Object>singletonMap(KEY_SAFE_MODE, safeMode), null);
    }

    public static boolean setTheme(String theme) {
        if (!THEME_SYSTEM.equals(theme) && !THEME_LIGHT.equals(theme) && !THEME_DARK.equals(theme)
                && !THEME_AMOLED.equals(theme)) return false;
        return write(Collections.<String, Object>singletonMap(KEY_THEME, theme), null);
    }

    /** Every feature back to its default and the pause off. Safe mode and theme are kept. */
    public static boolean resetAll() {
        synchronized (LOCK) {
            Store current = store;
            if (current == null) return false;
            List<String> removes = new ArrayList<>();
            try {
                for (String key : current.readAll().keySet()) {
                    if (key.startsWith(FEATURE_PREFIX) || KEY_PAUSED.equals(key)) removes.add(key);
                }
            } catch (Throwable failure) {
                return false;
            }
            return write(null, removes);
        }
    }

    private static boolean write(Map<String, Object> puts, List<String> removes) {
        synchronized (LOCK) {
            Store current = store;
            if (current == null) return false;
            boolean saved;
            try {
                saved = current.commit(puts, removes);
            } catch (Throwable failure) {
                Log.e(TAG, "Settings could not be saved: " + describe(failure));
                saved = false;
            }
            snapshot = build(current);
            return saved;
        }
    }

    // ---------------------------------------------------------------------------------------
    // Hook failures (privacy-safe)
    // ---------------------------------------------------------------------------------------

    /**
     * For extension helpers that caught an exception in a hook (and returned the stock value).
     * Keeps the exception class and the first Heval frame only, never the message, which could
     * hold user content.
     */
    public static void reportFailure(String featureId, Throwable failure) {
        try {
            if (featureId == null || failure == null) return;
            String description = describe(failure);
            failures.put(featureId, description);
            Log.w(TAG, "Feature " + featureId + " failed and fell back to stock: " + description);
        } catch (Throwable ignored) {
            // Never let failure reporting fail a hook.
        }
    }

    /** "<exception class> at <Class.method:line>" for the first app.template.extension frame. */
    static String describe(Throwable failure) {
        StackTraceElement[] stack = failure.getStackTrace();
        StackTraceElement frame = stack == null || stack.length == 0 ? null : stack[0];
        if (stack != null) {
            for (StackTraceElement element : stack) {
                if (element.getClassName().startsWith("app.template.extension.")) {
                    frame = element;
                    break;
                }
            }
        }
        if (frame == null) return failure.getClass().getName();
        String className = frame.getClassName();
        return failure.getClass().getName() + " at " + className.substring(className.lastIndexOf('.') + 1)
                + "." + frame.getMethodName() + (frame.getLineNumber() >= 0 ? ":" + frame.getLineNumber() : "");
    }

    // ---------------------------------------------------------------------------------------
    // Read API for the settings screen and diagnostics
    // ---------------------------------------------------------------------------------------

    /** Whether the engine has a snapshot (it may be a failed one). */
    public static boolean isReady() {
        return snapshot != null;
    }

    public static boolean isPaused() {
        Snapshot current = snapshot;
        return current != null && current.paused;
    }

    public static boolean isSafeMode() {
        Snapshot current = snapshot;
        return current != null && current.safeMode;
    }

    /** Whether the engine could not read its settings, so every feature is stock. */
    public static boolean isEngineFailed() {
        Snapshot current = snapshot;
        return current != null && current.failed;
    }

    /** The exception class of the last engine failure, or null. */
    public static String engineError() {
        return engineError;
    }

    /** "provider", "lazy", "test" or "none". */
    public static String initSource() {
        return initSource;
    }

    public static String theme() {
        Snapshot current = snapshot;
        return current == null ? THEME_SYSTEM : current.theme;
    }

    /** The saved choice (or the default), ignoring pause, safe mode and dependencies. */
    public static boolean isChosen(String featureId) {
        Snapshot current = snapshot;
        if (current == null) return Registry.get().defaultFor(featureId);
        Boolean chosen = current.chosen.get(featureId);
        return chosen != null && chosen;
    }

    /** The last hook failure of a feature in this process, or null. */
    public static String failureOf(String featureId) {
        return featureId == null ? null : failures.get(featureId);
    }

    /** The first dependency of a feature that this APK does not have, or null. */
    public static String missingDependency(Feature feature) {
        Registry registry = Registry.get();
        for (String dependency : feature.dependencies) {
            if (registry.feature(dependency) == null) return dependency;
        }
        return null;
    }

    public static State stateOf(Feature feature) {
        if (feature.buildTimeOnly) return State.BUILD_TIME_ONLY;
        if (missingDependency(feature) != null) return State.UNSUPPORTED;
        if (failures.containsKey(feature.id)) return State.FAILED;
        if (feature.restartRequired && restartPending(feature.id)) return State.RESTART_REQUIRED;
        return isEnabled(feature.id) ? State.ENABLED : State.DISABLED;
    }

    /** How many restart-required features now answer differently from process start. */
    public static int pendingRestartCount() {
        int count = 0;
        for (Feature feature : Registry.get().features()) {
            if (feature.restartRequired && !feature.buildTimeOnly && restartPending(feature.id)) count++;
        }
        return count;
    }

    private static boolean restartPending(String featureId) {
        Snapshot now = snapshot, start = baseline;
        return now != null && start != null && now.isOn(featureId) != start.isOn(featureId);
    }

    // ---------------------------------------------------------------------------------------
    // Snapshot
    // ---------------------------------------------------------------------------------------

    /** Everything a hook needs, computed once per change. Never mutated after publication. */
    static final class Snapshot {
        final boolean paused, safeMode, failed;
        final String theme;
        /** The user's choice or default per registered runtime feature. */
        final Map<String, Boolean> chosen;
        /** Only the features that are on right now (Boolean.TRUE). */
        final Map<String, Boolean> effective;

        private Snapshot(boolean paused, boolean safeMode, boolean failed, String theme,
                         Map<String, Boolean> chosen, Map<String, Boolean> effective) {
            this.paused = paused;
            this.safeMode = safeMode;
            this.failed = failed;
            this.theme = theme;
            this.chosen = chosen;
            this.effective = effective;
        }

        static Snapshot failed() {
            return new Snapshot(false, false, true, THEME_SYSTEM,
                    Collections.<String, Boolean>emptyMap(), Collections.<String, Boolean>emptyMap());
        }

        static Snapshot from(Registry registry, Map<String, ?> values) {
            boolean paused = Boolean.TRUE.equals(values.get(KEY_PAUSED));
            boolean safeMode = Boolean.TRUE.equals(values.get(KEY_SAFE_MODE));
            Object savedTheme = values.get(KEY_THEME);
            String theme = savedTheme instanceof String ? (String) savedTheme : THEME_SYSTEM;

            Map<String, Boolean> chosen = new HashMap<>();
            for (Feature feature : registry.features()) {
                Object value = feature.buildTimeOnly ? null : values.get(FEATURE_PREFIX + feature.id);
                // Unknown keys in the store are never read; a wrong type falls back to the default.
                chosen.put(feature.id, value instanceof Boolean ? (Boolean) value : feature.defaultEnabled);
            }

            Map<String, Boolean> effective = new HashMap<>();
            if (!paused && !safeMode && registry.decoded.supported) {
                for (Feature feature : registry.features()) {
                    if (resolve(registry, feature, chosen, new HashSet<String>())) {
                        effective.put(feature.id, Boolean.TRUE);
                    }
                }
            }
            return new Snapshot(paused, safeMode, false, theme,
                    Collections.unmodifiableMap(chosen), Collections.unmodifiableMap(effective));
        }

        /** On when chosen and every dependency resolves on; a missing or cyclic dependency is off. */
        private static boolean resolve(Registry registry, Feature feature, Map<String, Boolean> chosen, Set<String> visiting) {
            if (!Boolean.TRUE.equals(chosen.get(feature.id)) || !visiting.add(feature.id)) return false;
            for (String dependencyId : feature.dependencies) {
                Feature dependency = registry.feature(dependencyId);
                if (dependency == null || !resolve(registry, dependency, chosen, visiting)) return false;
            }
            visiting.remove(feature.id);
            return true;
        }

        boolean isOn(String featureId) {
            return Boolean.TRUE.equals(effective.get(featureId));
        }
    }

    // ---------------------------------------------------------------------------------------
    // Tests
    // ---------------------------------------------------------------------------------------

    static void initForTests(Store testStore) {
        synchronized (LOCK) {
            attach(testStore, "test");
        }
    }

    static void resetForTests() {
        synchronized (LOCK) {
            snapshot = null;
            baseline = null;
            store = null;
            initSource = "none";
            engineError = null;
            failures.clear();
        }
    }

    private HevalSettings() { }
}

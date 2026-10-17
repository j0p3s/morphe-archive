package app.template.extension.hevalcontrols;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * One feature of the patched app, as registered by its patch at patch time. Immutable.
 *
 * <p>Runtime features are gated in the app's code by {@link HevalSettings#isEnabled(String)}.
 * Build-time-only features were applied at patch time (manifest, resources, removed code) and are
 * listed for information only; they cannot be switched at runtime.
 */
@SuppressWarnings("unused")
public final class Feature {
    /** Feature ids: lower case, digits, '_' and '.', starting with a letter, at most 64 chars. */
    public static boolean isValidId(String id) {
        if (id == null || id.isEmpty() || id.length() > 64) return false;
        char first = id.charAt(0);
        if (first < 'a' || first > 'z') return false;
        for (int i = 1; i < id.length(); i++) {
            char c = id.charAt(i);
            boolean ok = (c >= 'a' && c <= 'z') || (c >= '0' && c <= '9') || c == '_' || c == '.';
            if (!ok) return false;
        }
        return true;
    }

    public final String id;
    public final String name;
    public final String description;
    public final String category;
    public final boolean defaultEnabled;
    /** The app reads this feature once per process, so a change applies after a restart. */
    public final boolean restartRequired;
    /** Applied at patch time; shown read-only. */
    public final boolean buildTimeOnly;
    /** Ids of features that must be on for this one to take effect. */
    public final List<String> dependencies;

    public Feature(String id, String name, String description, String category, boolean defaultEnabled,
                   boolean restartRequired, boolean buildTimeOnly, List<String> dependencies) {
        if (!isValidId(id)) throw new IllegalArgumentException("Invalid feature id");
        this.id = id;
        this.name = name == null || name.isEmpty() ? id : name;
        this.description = description == null ? "" : description;
        this.category = category == null || category.isEmpty() ? "General" : category;
        this.defaultEnabled = defaultEnabled;
        this.restartRequired = restartRequired;
        this.buildTimeOnly = buildTimeOnly;
        this.dependencies = dependencies == null || dependencies.isEmpty()
                ? Collections.<String>emptyList()
                : Collections.unmodifiableList(new ArrayList<>(dependencies));
    }
}

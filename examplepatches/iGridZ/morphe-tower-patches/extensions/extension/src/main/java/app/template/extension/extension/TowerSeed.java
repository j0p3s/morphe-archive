package app.template.extension.extension;

import android.content.Context;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;

/**
 * Seeds the Firebase Remote Config "activated" cache from the APK on first start.
 *
 * The game reads shop / mini boss / event config from Remote Config and breaks in SaveLoad.Load
 * when it is empty (no network on first start), which also blocks saving. A normal install has this
 * cache after its first online fetch, a fresh offline clone does not.
 */
@SuppressWarnings("unused")
public class TowerSeed {
    private static final String ASSET = "tower_frc.json";
    private static final String TARGET =
            "frc_1:219234862051:android:53efc5501a98c8297c56d6_firebase_activate.json";

    /** Called at the very start of FirebaseInitProvider.onCreate, before Firebase reads any cache. */
    public static void seedProvider(android.content.ContentProvider provider) {
        try {
            seed(provider.getContext());
        } catch (Throwable ignored) {
        }
    }

    public static void seed(Context context) {
        try {
            File out = new File(context.getFilesDir(), TARGET);
            if (out.exists() && out.length() > 0) return;
            File dir = out.getParentFile();
            if (dir != null && !dir.exists()) dir.mkdirs();
            try (InputStream in = context.getAssets().open(ASSET);
                 OutputStream os = new FileOutputStream(out)) {
                byte[] buf = new byte[16384];
                int n;
                while ((n = in.read(buf)) > 0) os.write(buf, 0, n);
            }
        } catch (Throwable ignored) {
            // Never block game start because of seeding.
        }
    }
}

package app.clearscanner.extension;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.view.View;

import java.io.OutputStream;

/**
 * Local "auto backup" support for Clear Scanner.
 *
 * The Sync button opens the app's own backup screen. The first time, the user creates a backup
 * file as usual and this class remembers that file. Afterwards the "Create a backup" button is
 * pressed automatically and the backup is written over the remembered file. If the file can no
 * longer be written, the normal file picker is shown again.
 */
@SuppressWarnings("unused")
public final class AutoBackup {

    private static final String PREFS_NAME = "morphe_auto_backup";
    private static final String KEY_URI = "backup_file_uri";
    private static final String ACTION_AUTO_BACKUP = "app.clearscanner.action.AUTO_BACKUP";

    /** Resource id of the "Create a backup" button in the Clear Scanner 10.2.18 backup screen. */
    private static final int BACKUP_BUTTON_ID = 0x7f090100;

    /** True only while the automatic click on the backup button is being processed. */
    private static volatile boolean autoRequested;

    private AutoBackup() {
    }

    private static SharedPreferences prefs(Context context) {
        return context.getApplicationContext().getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
    }

    /** Called when the Sync button opens the backup screen. */
    public static void prepareIntent(Context context, Intent intent) {
        try {
            if (prefs(context).getString(KEY_URI, null) != null) {
                intent.setAction(ACTION_AUTO_BACKUP);
            }
        } catch (Throwable ignored) {
            // Fall back to the normal backup screen.
        }
    }

    /** Called when the user has picked a backup file, right before the backup is written. */
    public static void rememberFile(Context context, Uri uri) {
        if (uri == null) return;
        try {
            context.getContentResolver().takePersistableUriPermission(
                    uri,
                    Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
        } catch (Throwable ignored) {
            // Without write access the auto backup falls back to the picker next time.
        }
        try {
            prefs(context).edit().putString(KEY_URI, uri.toString()).apply();
        } catch (Throwable ignored) {
        }
    }

    /** Called when the backup screen has been created. */
    public static void onBackupScreenCreated(Activity activity) {
        try {
            Intent intent = activity.getIntent();
            if (intent == null || !ACTION_AUTO_BACKUP.equals(intent.getAction())) return;
            // Only once per launch, also after rotation.
            intent.setAction(null);

            final View button = activity.findViewById(BACKUP_BUTTON_ID);
            if (button == null) return;

            autoRequested = true;
            button.post(new Runnable() {
                @Override
                public void run() {
                    try {
                        button.performClick();
                    } finally {
                        autoRequested = false;
                    }
                }
            });
        } catch (Throwable ignored) {
            autoRequested = false;
        }
    }

    /**
     * Called by the "Create a backup" button after the app's own checks (backup already running,
     * nothing to back up) have passed, and before the file picker is opened.
     *
     * @return the stream to write the backup to, or null to continue with the normal file picker.
     */
    public static OutputStream openStream(Activity activity) {
        if (!autoRequested) return null;
        autoRequested = false;

        String saved;
        try {
            saved = prefs(activity).getString(KEY_URI, null);
        } catch (Throwable t) {
            return null;
        }
        if (saved == null) return null;

        try {
            // "wt" truncates, so a shorter backup does not leave old bytes behind.
            OutputStream out = activity.getContentResolver().openOutputStream(Uri.parse(saved), "wt");
            if (out != null) return out;
        } catch (Throwable ignored) {
            // Deleted, moved or no longer permitted.
        }

        try {
            prefs(activity).edit().remove(KEY_URI).apply();
        } catch (Throwable ignored) {
        }
        return null;
    }
}

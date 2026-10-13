package hoodles.morphe.extension.soundcloud.ui;

import android.view.View;
import hoodles.morphe.extension.soundcloud.settings.MorpheSettings;

public final class MorpheUiVisibility {
    private MorpheUiVisibility() {}

    public static boolean shouldShowCast() {
        return !Boolean.TRUE.equals(MorpheSettings.HIDE_TOP_CAST.get());
    }

    public static boolean shouldShowUpload() {
        return !Boolean.TRUE.equals(MorpheSettings.HIDE_TOP_UPLOAD.get());
    }

    public static boolean shouldShowInbox() {
        return !Boolean.TRUE.equals(MorpheSettings.HIDE_TOP_INBOX.get());
    }

    public static boolean shouldShowNotification() {
        return !Boolean.TRUE.equals(MorpheSettings.HIDE_TOP_NOTIFICATIONS.get());
    }

    public static void applyCastButtonVisibility(View view) {
        if (view != null) {
            view.setVisibility(shouldShowCast() ? View.VISIBLE : View.GONE);
        }
    }
}

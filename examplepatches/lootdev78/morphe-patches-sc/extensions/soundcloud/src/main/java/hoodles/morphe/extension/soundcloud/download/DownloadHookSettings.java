package hoodles.morphe.extension.soundcloud.download;

import app.morphe.extension.shared.settings.BooleanSetting;
import app.morphe.extension.shared.settings.StringSetting;

public final class DownloadHookSettings {
    public static final BooleanSetting ENABLED = new BooleanSetting("morphe_download_hook_enabled", false);
    public static final StringSetting TARGET_PACKAGE = new StringSetting("morphe_download_hook_package", "");

    private DownloadHookSettings() {}
}

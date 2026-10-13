package hoodles.morphe.extension.soundcloud.settings;

import app.morphe.extension.shared.settings.BooleanSetting;

public final class MorpheSettings {
    private MorpheSettings() {}

    // Navigation / Top bar
    public static final BooleanSetting HIDE_TOP_CAST = new BooleanSetting("morphe_sc_hide_top_cast", false);
    public static final BooleanSetting HIDE_TOP_UPLOAD = new BooleanSetting("morphe_sc_hide_top_upload", false);
    public static final BooleanSetting HIDE_TOP_INBOX = new BooleanSetting("morphe_sc_hide_top_inbox", false);
    public static final BooleanSetting HIDE_TOP_NOTIFICATIONS = new BooleanSetting("morphe_sc_hide_top_notifications", false);

    // Home feed SDUI sections
    public static final BooleanSetting HIDE_HOME_SHORTCUTS = new BooleanSetting("morphe_sc_hide_home_shortcuts", false);
    public static final BooleanSetting HIDE_HOME_MORE_FOR_YOU = new BooleanSetting("morphe_sc_hide_home_more_for_you", false);
    public static final BooleanSetting HIDE_HOME_MIXED_FOR_YOU = new BooleanSetting("morphe_sc_hide_home_mixed_for_you", false);
    public static final BooleanSetting HIDE_HOME_ARTIST_UPLOADS = new BooleanSetting("morphe_sc_hide_home_artist_uploads", false);
    public static final BooleanSetting HIDE_HOME_LIKED_BY_FOLLOWED = new BooleanSetting("morphe_sc_hide_home_liked_by_followed", false);
    public static final BooleanSetting HIDE_HOME_TRENDS_BY_GENRE = new BooleanSetting("morphe_sc_hide_home_trends_by_genre", false);
    public static final BooleanSetting HIDE_HOME_ALBUMS_FOR_YOU = new BooleanSetting("morphe_sc_hide_home_albums_for_you", false);
    public static final BooleanSetting HIDE_HOME_MADE_FOR_YOU = new BooleanSetting("morphe_sc_hide_home_made_for_you", false);
    public static final BooleanSetting HIDE_HOME_LIKED_BY = new BooleanSetting("morphe_sc_hide_home_liked_by", false);
    public static final BooleanSetting HIDE_HOME_STATIONS = new BooleanSetting("morphe_sc_hide_home_stations", false);
    public static final BooleanSetting HIDE_HOME_NEW_CREW = new BooleanSetting("morphe_sc_hide_home_new_crew", false);
    public static final BooleanSetting HIDE_HOME_REPOSTS = new BooleanSetting("morphe_sc_hide_home_reposts", false);
    public static final BooleanSetting HIDE_HOME_CURATED = new BooleanSetting("morphe_sc_hide_home_curated", false);
    public static final BooleanSetting HIDE_HOME_UPSELL = new BooleanSetting("morphe_sc_hide_home_upsell", false);
    public static final BooleanSetting HIDE_HOME_HOT_FOR_YOU = new BooleanSetting("morphe_sc_hide_home_hot_for_you", false);
    public static final BooleanSetting HIDE_HOME_BUZZING = new BooleanSetting("morphe_sc_hide_home_buzzing", false);
    public static final BooleanSetting HIDE_HOME_ALL_YOU_NO_ALGORITHM = new BooleanSetting("morphe_sc_hide_home_all_you_no_algorithm", false);
}

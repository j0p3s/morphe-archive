package hoodles.morphe.extension.soundcloud.home;

import hoodles.morphe.extension.soundcloud.settings.MorpheSettings;

import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public final class HomeSectionFilter {
    private HomeSectionFilter() {}

    public static List<?> filter(List<?> list) {
        if (list == null) return null;
        List<Object> filtered = new ArrayList<>(list.size());
        for (Object item : list) {
            if (!shouldHide(item)) {
                filtered.add(item);
            }
        }
        return filtered;
    }

    private static boolean shouldHide(Object section) {
        if (section == null) return false;
        try {
            Method getUrnMethod = section.getClass().getMethod("getUrn");
            Object urn = getUrnMethod.invoke(section);
            if (urn == null) return false;
            Method getContentMethod = urn.getClass().getMethod("getContent");
            String content = (String) getContentMethod.invoke(urn);
            if (content == null) return false;

            if (isOn(MorpheSettings.HIDE_HOME_SHORTCUTS) && "soundcloud:sections:home-get-back".equals(content)) return true;
            if (isOn(MorpheSettings.HIDE_HOME_MORE_FOR_YOU) && "soundcloud:sections:home-personalized-tracks".equals(content)) return true;
            if (isOn(MorpheSettings.HIDE_HOME_MIXED_FOR_YOU) && "soundcloud:sections:home-your-moods".equals(content)) return true;
            if (isOn(MorpheSettings.HIDE_HOME_ARTIST_UPLOADS) && 
                ("soundcloud:sections:home-artist-uploads-v1".equals(content) || "soundcloud:sections:home-artist-uploads".equals(content))) return true;
            if (isOn(MorpheSettings.HIDE_HOME_LIKED_BY_FOLLOWED) && "soundcloud:sections:home-liked-by-your-crew".equals(content)) return true;
            if (isOn(MorpheSettings.HIDE_HOME_TRENDS_BY_GENRE) && "soundcloud:sections:home-trending-by-genre".equals(content)) return true;
            if (isOn(MorpheSettings.HIDE_HOME_ALBUMS_FOR_YOU) && "soundcloud:sections:home-albums-for-you".equals(content)) return true;
            if (isOn(MorpheSettings.HIDE_HOME_MADE_FOR_YOU) && "soundcloud:sections:home-made-for-you".equals(content)) return true;
            if (isOn(MorpheSettings.HIDE_HOME_LIKED_BY) && "soundcloud:sections:home-because-you-liked".equals(content)) return true;
            if (isOn(MorpheSettings.HIDE_HOME_STATIONS) && "soundcloud:sections:home-stations".equals(content)) return true;
            if (isOn(MorpheSettings.HIDE_HOME_NEW_CREW) && "soundcloud:sections:home-new-crew".equals(content)) return true;
            if (isOn(MorpheSettings.HIDE_HOME_REPOSTS) && "soundcloud:sections:home-reposts".equals(content)) return true;
            if (isOn(MorpheSettings.HIDE_HOME_CURATED) && "soundcloud:sections:home-curated".equals(content)) return true;
            if (isOn(MorpheSettings.HIDE_HOME_UPSELL) && "soundcloud:sections:home-upsell".equals(content)) return true;
            if (isOn(MorpheSettings.HIDE_HOME_HOT_FOR_YOU) && "soundcloud:sections:home-hot-for-you".equals(content)) return true;
            if (isOn(MorpheSettings.HIDE_HOME_BUZZING) && "soundcloud:sections:home-buzzing".equals(content)) return true;
            if (isOn(MorpheSettings.HIDE_HOME_ALL_YOU_NO_ALGORITHM) && "soundcloud:sections:home-all-you-no-algorithm".equals(content)) return true;
        } catch (Exception ignored) {}
        return false;
    }

    private static boolean isOn(app.morphe.extension.shared.settings.BooleanSetting setting) {
        return Boolean.TRUE.equals(setting.get());
    }
}

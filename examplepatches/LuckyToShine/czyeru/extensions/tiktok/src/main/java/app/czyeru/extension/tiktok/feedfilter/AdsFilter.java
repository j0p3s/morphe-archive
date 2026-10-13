package app.czyeru.extension.tiktok.feedfilter;

import app.czyeru.extension.tiktok.settings.Settings;
import app.czyeru.extension.tiktok.settings.SettingsStatus;
import com.ss.android.ugc.aweme.feed.model.Aweme;
import com.ss.android.ugc.aweme.feed.model.AwemeExtKt;

public class AdsFilter implements IFilter {
    @Override
    public boolean getEnabled() {
        return SettingsStatus.feedFilterEnabled && Settings.REMOVE_ADS.get();
    }

    @Override
    public boolean getFiltered(Aweme item) {
        return item.isAd()
            || item.isSoftAd()
            || item.isWithPromotionalMusic()
            || item.getAwemeRawAd() != null
            || AwemeExtKt.isPseudoAd(item);
    }
}

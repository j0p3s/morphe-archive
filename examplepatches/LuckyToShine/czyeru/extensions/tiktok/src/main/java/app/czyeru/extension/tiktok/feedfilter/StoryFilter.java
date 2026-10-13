package app.czyeru.extension.tiktok.feedfilter;

import app.czyeru.extension.tiktok.settings.Settings;
import com.ss.android.ugc.aweme.feed.model.Aweme;

public class StoryFilter implements IFilter {
    @Override
    public boolean getEnabled() {
        return Settings.HIDE_STORY.get();
    }

    @Override
    public boolean getFiltered(Aweme item) {
        return item.getIsTikTokStory();
    }
}


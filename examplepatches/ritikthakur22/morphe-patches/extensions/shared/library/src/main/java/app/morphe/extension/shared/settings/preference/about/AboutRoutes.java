package app.morphe.extension.shared.settings.preference.about;

import androidx.annotation.Nullable;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.net.HttpURLConnection;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.List;

import app.morphe.extension.shared.Logger;
import app.morphe.extension.shared.Utils;
import app.morphe.extension.shared.requests.Requester;
import app.morphe.extension.shared.requests.Route;

class AboutRoutes {
    /**
     * Backup icon url if the API call fails.
     */
    public static volatile String aboutLogoUrl = "https://raw.githubusercontent.com/ritikthakur22/revanced-magisk-module/main/my_logo.png";

    /**
     * Links to use if fetch links api call fails.
     */
    private static final List<MorpheAboutPreference.WebLink> NO_CONNECTION_STATIC_LINKS = List.of(
            new MorpheAboutPreference.WebLink(true, "Website", null, "https://ritikthakur.com.np"),
            MorpheAboutPreference.CREDITS_LINK
    );

    private static final String API_URL = "https://api.morphe.software/v2";
    private static final Route.CompiledRoute API_ROUTE_ABOUT = new Route(Route.Method.GET, "/about").compile();

    private static final String GITHUB_URL = "https://raw.githubusercontent.com";
    private static final Route.CompiledRoute GITHUB_ROUTE_PATCHES = new Route(Route.Method.GET,
            (Utils.isPreReleasePatches()
                    ? "/MorpheApp/morphe-patches/refs/heads/dev/patches-bundle.json"
                    : "/MorpheApp/morphe-patches/refs/heads/main/patches-bundle.json")
    ).compile();

    @Nullable
    private static volatile String latestPatchesVersion;
    private static volatile long latestPatchesVersionLastCheckedTime;

    static boolean hasFetchedPatchersVersion() {
        final long updateCheckFrequency = 5 * 60 * 1000; // 5 minutes.
        final long now = System.currentTimeMillis();

        return latestPatchesVersion != null && (now - latestPatchesVersionLastCheckedTime) < updateCheckFrequency;
    }
    @Nullable
    static String getLatestPatchesVersion() {
        return null;
    }

    @Nullable
    private static volatile List<MorpheAboutPreference.WebLink> fetchedLinks;

    static boolean hasFetchedLinks() {
        return fetchedLinks != null;
    }

    static List<MorpheAboutPreference.WebLink> fetchAboutLinks() {
        return NO_CONNECTION_STATIC_LINKS;
    }
}

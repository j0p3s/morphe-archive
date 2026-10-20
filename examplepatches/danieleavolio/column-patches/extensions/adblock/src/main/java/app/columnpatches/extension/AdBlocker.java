package app.columnpatches.extension;

import android.webkit.WebResourceRequest;
import android.webkit.WebResourceResponse;

import java.io.ByteArrayInputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

/**
 * Host based ad / tracker blocker for the Column Capacitor web view.
 *
 * Column is a WebView application, so every ad or tracker request made by the
 * web layer is routed through android.webkit.WebView, which asks
 * com.getcapacitor.BridgeWebViewClient.shouldInterceptRequest() for each
 * resource. Returning a WebResourceResponse from that method tells the WebView
 * "here is the answer, do not go to the network", which neutralises the request.
 *
 * The host list below is a conservative set of well known ad delivery,
 * programmatic and measurement endpoints. Matching is done on the host and its
 * subdomains only, so unrelated traffic (column.news, revenuecat, stripe,
 * firebase, gstatic, ...) is never affected.
 */
public final class AdBlocker {

    /** Maximum entries is not enforced, the set is small and constant. */
    private static final Set<String> BLOCKED_HOSTS = Collections.unmodifiableSet(
            new HashSet<String>(Arrays.asList(
                    // --- Google ad serving: AdSense, Ad Manager / DoubleClick, GPT ---
                    "doubleclick.net",
                    "2mdn.net",
                    "googlesyndication.com",
                    "googleadservices.com",
                    "googletagservices.com",
                    "adservice.google.com",
                    "adservice.google.it",
                    "adsensecustomsearchads.com",
                    "imasdk.googleapis.com",

                    // --- Large programmatic marketplaces / ad networks ---
                    "adnxs.com",
                    "adsrvr.org",
                    "amazon-adsystem.com",
                    "casalemedia.com",
                    "criteo.com",
                    "criteo.net",
                    "flashtalking.com",
                    "gumgum.com",
                    "improvedigital.com",
                    "indexww.com",
                    "inmobi.com",
                    "loopme.me",
                    "openx.net",
                    "outbrain.com",
                    "pubmatic.com",
                    "rubiconproject.com",
                    "sharethrough.com",
                    "smartadserver.com",
                    "smaato.net",
                    "spotxchange.com",
                    "taboola.com",
                    "teads.tv",
                    "thetradedesk.com",
                    "tremorhub.com",
                    "triplelift.com",
                    "yieldmo.com",
                    "zedo.com",
                    "an.facebook.com",

                    // --- Ad verification / audience measurement beacons ---
                    "adscore.com",
                    "doubleverify.com",
                    "integralads.com",
                    "moatads.com",
                    "adsafeprotected.com",
                    "scorecardresearch.com",
                    "quantserve.com",

                    // --- Product analytics loaded by the web layer ---
                    "app.posthog.com",
                    "us.i.posthog.com",
                    "us.posthog.com"))
    );

    private AdBlocker() {
    }

    /**
     * Called from the patched shouldInterceptRequest(). Must never throw, because
     * it runs on a WebView networking thread.
     */
    public static boolean isBlockedRequest(WebResourceRequest request) {
        if (request == null || request.getUrl() == null) {
            return false;
        }
        String host = request.getUrl().getHost();
        return host != null && isBlockedHost(host.toLowerCase(Locale.ROOT));
    }

    /** True for an exact host match or any subdomain of a blocked host. */
    public static boolean isBlockedHost(String host) {
        if (BLOCKED_HOSTS.contains(host)) {
            return true;
        }
        for (String blocked : BLOCKED_HOSTS) {
            if (host.endsWith("." + blocked)) {
                return true;
            }
        }
        return false;
    }

    /** An empty 204 response, i.e. "this resource is intentionally blank". */
    public static WebResourceResponse blockedResponse() {
        return new WebResourceResponse(
                "text/plain",
                "utf-8",
                204,
                "No Content",
                Collections.<String, String>emptyMap(),
                new ByteArrayInputStream(new byte[0]));
    }
}

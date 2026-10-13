package hoodles.morphe.extension.soundcloud.download;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.util.LruCache;
import android.view.View;
import android.widget.Toast;

public final class DownloadButtonHook {
    private static final LruCache<String, String> URL_CACHE = new LruCache<>(128);

    private DownloadButtonHook() {}

    public static boolean isEnabled() {
        return Boolean.TRUE.equals(DownloadHookSettings.ENABLED.get());
    }

    private static boolean isPublicHttpUrl(String url) {
        if (url == null) return false;
        url = url.trim();
        if (url.isEmpty()) return false;
        Uri uri = Uri.parse(url);
        String scheme = uri.getScheme();
        if (scheme == null) return false;
        if (!scheme.equalsIgnoreCase("https") && !scheme.equalsIgnoreCase("http")) return false;
        String host = uri.getHost();
        if (host == null) return false;
        return host.equalsIgnoreCase("soundcloud.com") || host.equalsIgnoreCase("www.soundcloud.com");
    }

    public static void rememberUrl(Object obj, String url) {
        if (obj != null && isPublicHttpUrl(url)) {
            URL_CACHE.put(String.valueOf(obj), url);
        }
    }

    public static String getRememberedUrl(Object obj) {
        if (obj == null) return null;
        return URL_CACHE.get(String.valueOf(obj));
    }

    public static boolean tryShareRemembered(Context context, Object obj) {
        return tryShare(context, getRememberedUrl(obj));
    }

    public static boolean tryShare(Context context, String url) {
        if (!isEnabled()) return false;
        if (context == null) return true;
        if (!isPublicHttpUrl(url)) {
            toast(context, "SoundCloud-Link nicht verfuegbar");
            return true;
        }

        String targetPackage = DownloadHookSettings.TARGET_PACKAGE.get();
        if (targetPackage != null) {
            targetPackage = targetPackage.trim();
        }

        Intent intent = new Intent(Intent.ACTION_SEND);
        intent.setType("text/plain");
        intent.putExtra(Intent.EXTRA_TEXT, url);

        if (targetPackage != null && !targetPackage.isEmpty()) {
            intent.setPackage(targetPackage);
        } else {
            intent = Intent.createChooser(intent, "Download with");
        }

        if (!(context instanceof Activity)) {
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        }

        try {
            context.startActivity(intent);
        } catch (Exception e) {
            toast(context, "Externe Download-App nicht gefunden oder ACTION_SEND nicht akzeptiert");
        }
        return true;
    }

    public static Object normalizeDownloadState(Object state) {
        return state;
    }

    public static boolean filterNativeDownloadUiVisibility(boolean visible) {
        if (isEnabled()) return false;
        return visible;
    }

    public static void applyLibraryDownloadsVisibility(View view) {
        if (view != null && isEnabled()) {
            view.setVisibility(View.GONE);
        }
    }

    private static void toast(Context context, String text) {
        Toast.makeText(context.getApplicationContext(), text, Toast.LENGTH_SHORT).show();
    }
}

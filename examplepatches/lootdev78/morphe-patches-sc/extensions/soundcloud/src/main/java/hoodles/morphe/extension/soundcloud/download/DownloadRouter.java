package hoodles.morphe.extension.soundcloud.download;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;

@SuppressWarnings("unused")
public final class DownloadRouter {
    private DownloadRouter() {}

    public static boolean open(Context context, String url, String packageName) {
        if (context == null || url == null || packageName == null) return false;
        Intent intent = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
        intent.setPackage(packageName);
        if (intent.resolveActivity(context.getPackageManager()) == null) return false;
        context.startActivity(intent);
        return true;
    }
}

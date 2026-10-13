package app.czyeru.extension.simpletimetracker;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.RectF;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.util.LruCache;
import android.util.Log;
import android.util.Xml;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import org.xmlpull.v1.XmlPullParser;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

import androidx.core.graphics.PathParser;

/**
 * Lets Simple Time Tracker use your own Android vector drawables (.xml) as activity icons.
 * <p>
 * Put the files in {@code <external files dir>/custom_icons/}, which on a phone is
 * {@code Android/data/com.razeeman.util.simpletimetracker/files/custom_icons/}. They are added to
 * the end of the icon picker under the name {@code xml:<file name>}. The app saves that name like
 * any other icon name. Because it does not start with {@code ic_}, the app treats it as text, so
 * {@code IconView} hands it to {@link #showIconText(ImageView, TextView, String)}.
 * <p>
 * Only the {@code pathData} of each {@code <path>} is drawn, filled white, so the app's own tint
 * gives it the activity color. Strokes, gradients, groups and transforms are not supported.
 * Anything that goes wrong (missing file, bad XML, bad path) falls back to the original behavior.
 */
@SuppressWarnings("unused")
public final class CustomIcons {
    private static final String TAG = "CustomIcons";
    private static final String DIR_NAME = "custom_icons";
    private static final String PREFIX = "xml:";
    private static final String EXTENSION = ".xml";

    /** Pixel size the vectors are rasterized to. */
    private static final int RENDER_SIZE = 96;

    /** Rasterized icons by file, so list scrolling does not parse files again. */
    private static final LruCache<String, Bitmap> CACHE = new LruCache<>(64);

    /**
     * Set by {@link #beginImages(int, int)} and read by {@link #endImages(Context, Map, int)} on
     * the same thread: tells the end of {@code IconImageRepo.getImages} which icon group it built.
     */
    private static final ThreadLocal<Boolean> COLLECTING = new ThreadLocal<>();

    private CustomIcons() {
    }

    /**
     * Called first in {@code IconImageRepo.getImages(arrayId)}.
     *
     * @param targetArrayId the one icon group that gets the custom icons.
     */
    public static void beginImages(int arrayId, int targetArrayId) {
        COLLECTING.set(arrayId == targetArrayId);
    }

    /**
     * Called last in {@code IconImageRepo.getImages}. Appends the custom icons, so the position
     * of the built-in icons (used to look up their search words) does not change.
     *
     * @param placeholder drawable id stored for each custom icon. It is only used by screens that
     *                    do not know about custom icons.
     */
    public static void endImages(Context context, Map<String, Integer> images, int placeholder) {
        Boolean collecting = COLLECTING.get();
        COLLECTING.remove();
        if (collecting == null || !collecting) return;

        try {
            File dir = iconsDir(context);
            if (dir == null) return;
            // Creating it lets the user find the folder.
            if (!dir.exists() && !dir.mkdirs()) return;

            File[] files = dir.listFiles();
            if (files == null) return;
            Arrays.sort(files);
            for (File file : files) {
                if (file.isFile() && file.getName().toLowerCase().endsWith(EXTENSION)) {
                    images.put(PREFIX + file.getName(), placeholder);
                }
            }
        } catch (Exception e) {
            Log.w(TAG, "Could not list custom icons", e);
        }
    }

    /**
     * Called first in {@code IconView.setTextIcon(text)}.
     *
     * @return true if the text was a custom icon and it is now shown, so the original code must
     * be skipped.
     */
    public static boolean showIconText(ImageView image, TextView textView, String text) {
        Drawable drawable = loadFor(image.getContext(), text);
        if (drawable == null) return false;

        image.setBackground(drawable);
        image.setVisibility(View.VISIBLE);
        textView.setVisibility(View.GONE);
        return true;
    }

    /**
     * Called in the icon picker after a cell got its placeholder image.
     */
    public static void showPickerIcon(ImageView image, String iconName) {
        Drawable drawable = loadFor(image.getContext(), iconName);
        if (drawable != null) image.setBackground(drawable);
    }

    private static Drawable loadFor(Context context, String iconName) {
        if (iconName == null || !iconName.startsWith(PREFIX)) return null;
        String fileName = iconName.substring(PREFIX.length());
        // Names come from the picker, but they are also stored in backups.
        if (fileName.isEmpty() || fileName.contains("/") || fileName.contains("\\")) return null;

        try {
            File dir = iconsDir(context);
            if (dir == null) return null;
            File file = new File(dir, fileName);
            if (!file.isFile()) return null;

            String key = fileName + "|" + file.lastModified() + "|" + file.length();
            Bitmap bitmap = CACHE.get(key);
            if (bitmap == null) {
                bitmap = rasterize(file);
                if (bitmap == null) return null;
                CACHE.put(key, bitmap);
            }
            // One drawable per view: tinting a shared instance would tint every view using it.
            return new BitmapDrawable(context.getResources(), bitmap);
        } catch (Exception e) {
            Log.w(TAG, "Could not load custom icon " + iconName, e);
            return null;
        }
    }

    private static File iconsDir(Context context) {
        File base = context.getExternalFilesDir(null);
        return base == null ? null : new File(base, DIR_NAME);
    }

    private static Bitmap rasterize(File file) throws Exception {
        float viewportWidth = 24f;
        float viewportHeight = 24f;
        List<String> pathData = new ArrayList<>();

        try (InputStream stream = new FileInputStream(file)) {
            XmlPullParser parser = Xml.newPullParser();
            parser.setInput(stream, null);

            int event = parser.getEventType();
            while (event != XmlPullParser.END_DOCUMENT) {
                if (event == XmlPullParser.START_TAG) {
                    String tag = parser.getName();
                    if ("vector".equals(tag)) {
                        for (int i = 0; i < parser.getAttributeCount(); i++) {
                            String name = localName(parser.getAttributeName(i));
                            if ("viewportWidth".equals(name)) {
                                viewportWidth = Float.parseFloat(parser.getAttributeValue(i));
                            } else if ("viewportHeight".equals(name)) {
                                viewportHeight = Float.parseFloat(parser.getAttributeValue(i));
                            }
                        }
                    } else if ("path".equals(tag)) {
                        for (int i = 0; i < parser.getAttributeCount(); i++) {
                            if ("pathData".equals(localName(parser.getAttributeName(i)))) {
                                pathData.add(parser.getAttributeValue(i));
                            }
                        }
                    }
                }
                event = parser.next();
            }
        }

        if (viewportWidth <= 0f || viewportHeight <= 0f || pathData.isEmpty()) return null;

        // Fit the viewport inside the square and center it.
        float scale = Math.min(RENDER_SIZE / viewportWidth, RENDER_SIZE / viewportHeight);
        float dx = (RENDER_SIZE - viewportWidth * scale) / 2f;
        float dy = (RENDER_SIZE - viewportHeight * scale) / 2f;

        Bitmap bitmap = Bitmap.createBitmap(RENDER_SIZE, RENDER_SIZE, Bitmap.Config.ARGB_8888);
        Canvas canvas = new Canvas(bitmap);
        canvas.translate(dx, dy);
        canvas.scale(scale, scale);

        // White, so the tint set by the app decides the color.
        Paint paint = new Paint(Paint.ANTI_ALIAS_FLAG);
        paint.setColor(Color.WHITE);
        paint.setStyle(Paint.Style.FILL);

        RectF bounds = new RectF();
        boolean drewAnything = false;
        for (String data : pathData) {
            Path path = PathParser.createPathFromPathData(data);
            if (path == null) continue;
            // Nothing to fill, for example when the data holds only commands without points.
            path.computeBounds(bounds, true);
            if (bounds.isEmpty()) continue;
            canvas.drawPath(path, paint);
            drewAnything = true;
        }
        return drewAnything ? bitmap : null;
    }

    /** "android:pathData" -> "pathData". */
    private static String localName(String attributeName) {
        int colon = attributeName.indexOf(':');
        return colon < 0 ? attributeName : attributeName.substring(colon + 1);
    }
}

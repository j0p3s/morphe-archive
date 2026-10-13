package app.czyeru.extension.cinevi;

import android.content.Context;
import android.content.res.AssetManager;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.SurfaceTexture;
import android.graphics.drawable.Drawable;
import android.media.MediaCodecList;
import android.media.MediaExtractor;
import android.media.MediaFormat;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Surface;
import android.view.TextureView;
import android.view.View;
import android.view.ViewGroup;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Properties;

/**
 * Replaces the animated "first load" screen of Cinevi's video player with a custom video.
 * <p>
 * The patch embeds the video as {@code assets/custom_loading_video} and the user's settings as
 * {@code assets/custom_loading_video.properties}. The player calls
 * {@link #onLoadFirstVisibility(View, int)} whenever it shows or hides that screen.
 * <p>
 * Anything that goes wrong (unreadable file, no decoder for the codec, player error) restores the
 * original loading screen instead of leaving a blank one.
 */
@SuppressWarnings("unused")
public final class CustomLoadingVideo {
    private static final String TAG = "CustomLoadingVideo";
    private static final String ASSET_VIDEO = "custom_loading_video";
    private static final String ASSET_CONFIG = "custom_loading_video.properties";
    private static final String CACHE_FILE = "custom_loading_video.bin";

    private static final Handler MAIN = new Handler(Looper.getMainLooper());

    /** Written once by the background preparation thread, read on the main thread. */
    private static volatile Prepared prepared;
    private static volatile boolean preparing;
    private static volatile boolean unusable;

    /** Main thread only. */
    private static Session session;

    private CustomLoadingVideo() {
    }

    /**
     * Called from the player's {@code BaseView.A(int)} before the original code runs.
     *
     * @param layer      root view of the first-load screen, may be null.
     * @param visibility {@link View#VISIBLE}, {@link View#INVISIBLE} or {@link View#GONE}.
     */
    public static void onLoadFirstVisibility(final View layer, final int visibility) {
        if (Looper.myLooper() != Looper.getMainLooper()) {
            MAIN.post(new Runnable() {
                @Override
                public void run() {
                    onLoadFirstVisibility(layer, visibility);
                }
            });
            return;
        }
        try {
            if (visibility == View.VISIBLE) {
                show(layer);
            } else {
                hide();
            }
        } catch (Throwable t) {
            Log.w(TAG, "Custom loading video failed, using the original loading screen", t);
            hide();
        }
    }

    private static void show(View layer) {
        if (!(layer instanceof ViewGroup) || unusable) return;
        if (session != null && session.layer == layer) return;
        hide();

        Prepared ready = prepared;
        if (ready == null) {
            prepare(layer);
            return;
        }
        session = new Session((ViewGroup) layer, ready);
        session.start();
    }

    private static void hide() {
        if (session != null) {
            session.stop();
            session = null;
        }
    }

    /** Copies the video to the cache, reads the settings and checks a decoder exists. */
    private static void prepare(final View layer) {
        if (preparing) return;
        preparing = true;
        final Context context = layer.getContext().getApplicationContext();
        new Thread(new Runnable() {
            @Override
            public void run() {
                Prepared result = null;
                try {
                    result = load(context);
                } catch (Throwable t) {
                    Log.w(TAG, "Could not prepare the custom loading video", t);
                }
                final Prepared finished = result;
                MAIN.post(new Runnable() {
                    @Override
                    public void run() {
                        preparing = false;
                        if (finished == null) {
                            unusable = true;
                            return;
                        }
                        prepared = finished;
                        // The loading screen may already be on screen. Start the video now.
                        if (layer.getVisibility() == View.VISIBLE && layer.isAttachedToWindow()) {
                            show(layer);
                        }
                    }
                });
            }
        }, "CustomLoadingVideoPrepare").start();
    }

    private static Prepared load(Context context) throws Exception {
        AssetManager assets = context.getAssets();

        Properties props = new Properties();
        InputStream config = assets.open(ASSET_CONFIG);
        try {
            props.load(config);
        } finally {
            config.close();
        }
        Config cfg = new Config(props);

        File cached = new File(context.getCacheDir(), CACHE_FILE);
        if (!cached.isFile() || cached.length() != cfg.videoBytes) {
            File tmp = new File(context.getCacheDir(), CACHE_FILE + ".tmp");
            InputStream in = assets.open(ASSET_VIDEO);
            try {
                OutputStream out = new FileOutputStream(tmp);
                try {
                    byte[] buffer = new byte[64 * 1024];
                    int n;
                    while ((n = in.read(buffer)) > 0) out.write(buffer, 0, n);
                } finally {
                    out.close();
                }
            } finally {
                in.close();
            }
            if (!tmp.renameTo(cached)) throw new IllegalStateException("Could not cache the video");
        }

        if (!hasDecoder(cached)) return null;
        return new Prepared(cached, cfg);
    }

    /** True if this device can decode the video track (VP9, AV1 and HEVC are not on every device). */
    private static boolean hasDecoder(File file) throws Exception {
        MediaExtractor extractor = new MediaExtractor();
        try {
            extractor.setDataSource(file.getAbsolutePath());
            for (int i = 0; i < extractor.getTrackCount(); i++) {
                MediaFormat format = extractor.getTrackFormat(i);
                String mime = format.getString(MediaFormat.KEY_MIME);
                if (mime == null || !mime.startsWith("video/")) continue;
                // findDecoderForFormat() rejects formats that carry a frame rate on older releases.
                format.setString(MediaFormat.KEY_FRAME_RATE, null);
                boolean found = new MediaCodecList(MediaCodecList.REGULAR_CODECS)
                        .findDecoderForFormat(format) != null;
                if (!found) Log.w(TAG, "No decoder for " + mime + " on this device");
                return found;
            }
            Log.w(TAG, "The file has no video track");
            return false;
        } finally {
            extractor.release();
        }
    }

    private enum Scale {FIT, FILL, STRETCH}

    private static final class Config {
        final long videoBytes;
        final Scale scale;
        final boolean loop;
        final boolean mute;
        final boolean hideOriginal;
        final Integer background;

        Config(Properties props) {
            videoBytes = Long.parseLong(props.getProperty("videoBytes", "-1"));
            String s = props.getProperty("scale", "fit").trim().toLowerCase();
            scale = s.equals("fill") ? Scale.FILL : s.equals("stretch") ? Scale.STRETCH : Scale.FIT;
            loop = Boolean.parseBoolean(props.getProperty("loop", "true"));
            mute = Boolean.parseBoolean(props.getProperty("mute", "true"));
            hideOriginal = Boolean.parseBoolean(props.getProperty("hideOriginal", "true"));
            Integer color = null;
            String bg = props.getProperty("background", "").trim();
            if (!bg.isEmpty()) {
                try {
                    color = Color.parseColor(bg);
                } catch (IllegalArgumentException e) {
                    Log.w(TAG, "Ignoring invalid background color " + bg);
                }
            }
            background = color;
        }
    }

    private static final class Prepared {
        final File file;
        final Config config;

        Prepared(File file, Config config) {
            this.file = file;
            this.config = config;
        }
    }

    /** One showing of the loading screen. Owns the TextureView and the MediaPlayer. */
    private static final class Session implements TextureView.SurfaceTextureListener,
            View.OnLayoutChangeListener {
        final ViewGroup layer;
        private final Prepared prepared;
        private final PlayerTextureView textureView;
        private final ArrayList<View> hiddenChildren = new ArrayList<View>();
        private final ArrayList<Integer> hiddenVisibility = new ArrayList<Integer>();
        private Drawable savedBackground;
        private boolean backgroundChanged;
        private MediaPlayer player;
        private Surface surface;
        private boolean prepared_;
        private boolean paused;
        private int videoWidth;
        private int videoHeight;
        private boolean stopped;

        Session(ViewGroup layer, Prepared prepared) {
            this.layer = layer;
            this.prepared = prepared;
            this.textureView = new PlayerTextureView(layer.getContext(), this);
        }

        void start() {
            Config cfg = prepared.config;
            if (cfg.hideOriginal) {
                for (int i = 0; i < layer.getChildCount(); i++) {
                    View child = layer.getChildAt(i);
                    hiddenChildren.add(child);
                    hiddenVisibility.add(child.getVisibility());
                    child.setVisibility(View.INVISIBLE);
                }
            }
            if (cfg.background != null) {
                savedBackground = layer.getBackground();
                layer.setBackgroundColor(cfg.background);
                backgroundChanged = true;
            }
            textureView.setSurfaceTextureListener(this);
            textureView.addOnLayoutChangeListener(this);
            layer.addView(textureView, 0,
                    new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
                            ViewGroup.LayoutParams.MATCH_PARENT));
        }

        void stop() {
            if (stopped) return;
            stopped = true;
            releasePlayer();
            textureView.setSurfaceTextureListener(null);
            textureView.removeOnLayoutChangeListener(this);
            layer.removeView(textureView);
            for (int i = 0; i < hiddenChildren.size(); i++) {
                hiddenChildren.get(i).setVisibility(hiddenVisibility.get(i));
            }
            hiddenChildren.clear();
            hiddenVisibility.clear();
            if (backgroundChanged) {
                layer.setBackground(savedBackground);
                backgroundChanged = false;
            }
        }

        /** Gives the original loading screen back after an error. */
        private void fail(String reason) {
            Log.w(TAG, reason + ", using the original loading screen");
            stop();
            if (session == this) session = null;
        }

        private void releasePlayer() {
            if (player != null) {
                try {
                    player.setOnPreparedListener(null);
                    player.setOnErrorListener(null);
                    player.setOnVideoSizeChangedListener(null);
                    player.release();
                } catch (Throwable ignored) {
                }
                player = null;
            }
            prepared_ = false;
            if (surface != null) {
                surface.release();
                surface = null;
            }
        }

        private void startPlayer(SurfaceTexture texture) {
            if (stopped || player != null) return;
            try {
                surface = new Surface(texture);
                MediaPlayer mp = new MediaPlayer();
                player = mp;
                mp.setDataSource(prepared.file.getAbsolutePath());
                mp.setSurface(surface);
                mp.setLooping(prepared.config.loop);
                if (prepared.config.mute) mp.setVolume(0f, 0f);
                mp.setOnVideoSizeChangedListener(new MediaPlayer.OnVideoSizeChangedListener() {
                    @Override
                    public void onVideoSizeChanged(MediaPlayer m, int width, int height) {
                        videoWidth = width;
                        videoHeight = height;
                        applyScale();
                    }
                });
                mp.setOnPreparedListener(new MediaPlayer.OnPreparedListener() {
                    @Override
                    public void onPrepared(MediaPlayer m) {
                        prepared_ = true;
                        if (!paused && !stopped) m.start();
                    }
                });
                mp.setOnErrorListener(new MediaPlayer.OnErrorListener() {
                    @Override
                    public boolean onError(MediaPlayer m, int what, int extra) {
                        fail("Video player error " + what + "/" + extra);
                        return true;
                    }
                });
                mp.prepareAsync();
            } catch (Throwable t) {
                fail("Could not start the video: " + t);
            }
        }

        void onWindowVisible(boolean visible) {
            paused = !visible;
            if (player == null || !prepared_) return;
            try {
                if (visible) {
                    player.start();
                } else if (player.isPlaying()) {
                    player.pause();
                }
            } catch (Throwable ignored) {
            }
        }

        private void applyScale() {
            int viewW = textureView.getWidth();
            int viewH = textureView.getHeight();
            if (viewW == 0 || viewH == 0 || videoWidth == 0 || videoHeight == 0) return;

            float viewAspect = (float) viewW / viewH;
            float videoAspect = (float) videoWidth / videoHeight;
            float sx = 1f;
            float sy = 1f;
            switch (prepared.config.scale) {
                case FIT:
                    if (videoAspect > viewAspect) sy = viewAspect / videoAspect;
                    else sx = videoAspect / viewAspect;
                    break;
                case FILL:
                    if (videoAspect > viewAspect) sx = videoAspect / viewAspect;
                    else sy = viewAspect / videoAspect;
                    break;
                case STRETCH:
                default:
                    break;
            }
            Matrix matrix = new Matrix();
            matrix.setScale(sx, sy, viewW / 2f, viewH / 2f);
            textureView.setTransform(matrix);
        }

        @Override
        public void onSurfaceTextureAvailable(SurfaceTexture texture, int width, int height) {
            startPlayer(texture);
        }

        @Override
        public void onSurfaceTextureSizeChanged(SurfaceTexture texture, int width, int height) {
            applyScale();
        }

        @Override
        public boolean onSurfaceTextureDestroyed(SurfaceTexture texture) {
            releasePlayer();
            return true;
        }

        @Override
        public void onSurfaceTextureUpdated(SurfaceTexture texture) {
        }

        @Override
        public void onLayoutChange(View v, int left, int top, int right, int bottom,
                                   int oldLeft, int oldTop, int oldRight, int oldBottom) {
            applyScale();
        }
    }

    /** TextureView that tells its session when the window goes to the background. */
    private static final class PlayerTextureView extends TextureView {
        private final Session session;

        PlayerTextureView(Context context, Session session) {
            super(context);
            this.session = session;
        }

        @Override
        protected void onWindowVisibilityChanged(int visibility) {
            super.onWindowVisibilityChanged(visibility);
            session.onWindowVisible(visibility == View.VISIBLE);
        }
    }
}

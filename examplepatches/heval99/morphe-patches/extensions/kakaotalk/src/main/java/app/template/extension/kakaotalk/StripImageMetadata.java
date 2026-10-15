// Based on AmpleReVanced/revanced-patches (GPL-3.0), extensions/kakaotalk StripImageMetadataPatch.
// Changes: framework ExifInterface instead of androidx, no settings toggle, and stale copies in the
// cache directory are cleaned up.

package app.template.extension.kakaotalk;

import android.media.ExifInterface;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.StandardCopyOption;

/**
 * Removes location and camera details from a photo before KakaoTalk uploads it. The patch runs
 * the file returned by ImageItemSendHelper's resize step through {@link #sanitize(File)}. The
 * original file is never changed: a cleaned copy is written to the cache directory instead.
 */
@SuppressWarnings({"unused", "deprecation"})
public final class StripImageMetadata {
    /** Tags whose presence means the file is worth cleaning. */
    private static final String[] TRIGGER_TAGS = {
            ExifInterface.TAG_GPS_LATITUDE,
            ExifInterface.TAG_MAKE,
            ExifInterface.TAG_MODEL,
            ExifInterface.TAG_DATETIME_ORIGINAL,
    };

    /**
     * Everything that is removed. Orientation is kept so the photo still displays upright. Tags
     * the framework knows but does not expose as constants are given by their EXIF names.
     */
    private static final String[] STRIPPED_TAGS = {
            ExifInterface.TAG_GPS_VERSION_ID,
            ExifInterface.TAG_GPS_LATITUDE_REF,
            ExifInterface.TAG_GPS_LATITUDE,
            ExifInterface.TAG_GPS_LONGITUDE_REF,
            ExifInterface.TAG_GPS_LONGITUDE,
            ExifInterface.TAG_GPS_ALTITUDE_REF,
            ExifInterface.TAG_GPS_ALTITUDE,
            ExifInterface.TAG_GPS_TIMESTAMP,
            ExifInterface.TAG_GPS_SATELLITES,
            ExifInterface.TAG_GPS_STATUS,
            ExifInterface.TAG_GPS_MEASURE_MODE,
            ExifInterface.TAG_GPS_DOP,
            ExifInterface.TAG_GPS_SPEED_REF,
            ExifInterface.TAG_GPS_SPEED,
            ExifInterface.TAG_GPS_TRACK_REF,
            ExifInterface.TAG_GPS_TRACK,
            ExifInterface.TAG_GPS_IMG_DIRECTION_REF,
            ExifInterface.TAG_GPS_IMG_DIRECTION,
            ExifInterface.TAG_GPS_MAP_DATUM,
            ExifInterface.TAG_GPS_DEST_LATITUDE_REF,
            ExifInterface.TAG_GPS_DEST_LATITUDE,
            ExifInterface.TAG_GPS_DEST_LONGITUDE_REF,
            ExifInterface.TAG_GPS_DEST_LONGITUDE,
            ExifInterface.TAG_GPS_DEST_BEARING_REF,
            ExifInterface.TAG_GPS_DEST_BEARING,
            ExifInterface.TAG_GPS_DEST_DISTANCE_REF,
            ExifInterface.TAG_GPS_DEST_DISTANCE,
            ExifInterface.TAG_GPS_PROCESSING_METHOD,
            ExifInterface.TAG_GPS_AREA_INFORMATION,
            ExifInterface.TAG_GPS_DATESTAMP,
            ExifInterface.TAG_GPS_DIFFERENTIAL,
            "GPSHPositioningError",
            ExifInterface.TAG_MAKE,
            ExifInterface.TAG_MODEL,
            ExifInterface.TAG_DATETIME,
            ExifInterface.TAG_DATETIME_ORIGINAL,
            ExifInterface.TAG_DATETIME_DIGITIZED,
            ExifInterface.TAG_OFFSET_TIME,
            ExifInterface.TAG_OFFSET_TIME_ORIGINAL,
            ExifInterface.TAG_OFFSET_TIME_DIGITIZED,
            ExifInterface.TAG_SUBSEC_TIME,
            ExifInterface.TAG_SUBSEC_TIME_ORIGINAL,
            ExifInterface.TAG_SUBSEC_TIME_DIGITIZED,
            ExifInterface.TAG_SOFTWARE,
            ExifInterface.TAG_ARTIST,
            ExifInterface.TAG_COPYRIGHT,
            ExifInterface.TAG_IMAGE_UNIQUE_ID,
            "CameraOwnerName",
            "BodySerialNumber",
            "LensMake",
            "LensModel",
            "LensSerialNumber",
            ExifInterface.TAG_USER_COMMENT,
            ExifInterface.TAG_IMAGE_DESCRIPTION,
    };

    private static final String COPY_PREFIX = "morphe_strip_";

    /** Cleaned copies older than this are deleted on the next send; uploads finish long before. */
    private static final long STALE_COPY_AGE_MS = 24L * 60 * 60 * 1000;

    private StripImageMetadata() {
    }

    private static void deleteStaleCopies(File dir) {
        try {
            File[] old = dir == null ? null : dir.listFiles();
            if (old == null) return;
            long cutoff = System.currentTimeMillis() - STALE_COPY_AGE_MS;
            for (File file : old) {
                if (file.getName().startsWith(COPY_PREFIX) && file.lastModified() < cutoff) {
                    //noinspection ResultOfMethodCallIgnored
                    file.delete();
                }
            }
        } catch (Throwable ignored) {
        }
    }

    /**
     * Returns a copy of {@code f} without location and camera tags, or {@code f} itself when it
     * has none of them or anything fails (unsupported format, I/O error).
     */
    public static File sanitize(File f) {
        File copy = null;
        try {
            if (f == null || !f.isFile()) return f;

            ExifInterface source = new ExifInterface(f.getPath());
            boolean hasMetadata = false;
            for (String tag : TRIGGER_TAGS) {
                if (source.getAttribute(tag) != null) {
                    hasMetadata = true;
                    break;
                }
            }
            if (!hasMetadata) return f;

            copy = File.createTempFile(COPY_PREFIX, "_" + f.getName());
            deleteStaleCopies(copy.getParentFile());
            Files.copy(f.toPath(), copy.toPath(), StandardCopyOption.REPLACE_EXISTING);

            ExifInterface exif = new ExifInterface(copy.getPath());
            for (String tag : STRIPPED_TAGS) {
                exif.setAttribute(tag, null);
            }
            exif.saveAttributes();
            return copy;
        } catch (Throwable t) {
            if (copy != null) {
                try {
                    //noinspection ResultOfMethodCallIgnored
                    copy.delete();
                } catch (Throwable ignored) {
                }
            }
            return f;
        }
    }
}

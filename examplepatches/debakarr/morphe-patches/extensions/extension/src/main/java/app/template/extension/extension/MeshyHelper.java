package app.template.extension.extension;

import android.util.Log;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Locale;
import java.util.Map;
import java.util.TimeZone;

/**
 * Entry point for the Meshy "unlock preview" patch.
 *
 * <p>Meshy gates AR preview, model decrypt/export and sharing behind a single
 * RevenueCat subscription check that the <em>Hermes JS layer</em> performs on
 * the customer info the native SDK serialises. The native Meshy modules
 * ({@code meshyar}, {@code meshydecrypt}, {@code meshysharetarget}) do not gate
 * anything themselves, so the whole app unlocks by making that customer info
 * report an active {@code pro} entitlement.
 *
 * <p>{@link #meshyInjectProEntitlement(Map)} runs on the output of
 * {@code EntitlementInfosMapperKt.map(EntitlementInfos)} — the single point
 * where entitlements are serialised for JS — and seeds a synthetic but
 * well-formed active {@code pro} entry into the {@code active} (and
 * {@code all}) sub-maps when the account has none. It touches only plain
 * {@code java.util.Map} values, so the extension needs no RevenueCat classes on
 * its compile classpath, and it never breaks the app's own parsing on failure.
 */
public final class MeshyHelper {

    private MeshyHelper() {}

    private static final String TAG = "MorpheMeshy";

    /**
     * Entitlement identifiers the JS may look up under
     * {@code customerInfo.entitlements.active}. {@code "pro"} is the primary —
     * the bundle carries the literal {@code "Charged but RevenueCat returned no
     * pro entitlement"} — and the rest are defensive candidates in case the
     * dashboard id differs. Extra keys are harmless for a plain
     * {@code active[id]} lookup.
     */
    private static final String[] PRO_KEYS = { "pro", "premium", "meshy_pro" };

    /**
     * Seeds an active {@code pro} entitlement into a mapped customer-info
     * object.
     *
     * @param result the {@code {"all":..., "active":..., "verification":...}}
     *               map produced by {@code EntitlementInfosMapperKt.map}
     * @return the same map, with {@code active} (and {@code all}) containing a
     *         synthetic active {@code pro} entry when none was active yet
     */
    @SuppressWarnings("unchecked")
    public static Map<String, Object> meshyInjectProEntitlement(Map<String, Object> result) {
        if (result == null) return null;
        try {
            Object activeObj = result.get("active");
            if (!(activeObj instanceof Map)) return result;
            Map<String, Object> active = (Map<String, Object>) activeObj;

            // Leave a genuinely active pro alone; otherwise seed one.
            if (containsAny(active)) return result;

            Map<String, Object> pro = proEntitlement();
            for (String key : PRO_KEYS) {
                active.put(key, pro);
            }
            Object allObj = result.get("all");
            if (allObj instanceof Map) {
                Map<String, Object> all = (Map<String, Object>) allObj;
                for (String key : PRO_KEYS) {
                    all.put(key, pro);
                }
            }
            Log.d(TAG, "seeded active pro entitlement (keys=" + PRO_KEYS.length + ")");
        } catch (Throwable t) {
            // Never let a rewrite failure break the app's own parsing.
            Log.d(TAG, "meshy inject failed: " + t);
        }
        return result;
    }

    /** True when the active map already carries one of our pro keys. */
    private static boolean containsAny(Map<String, Object> active) {
        for (String key : PRO_KEYS) {
            if (active.containsKey(key)) return true;
        }
        return false;
    }

    /**
     * A well-formed active {@code pro} entitlement in the exact shape
     * {@code EntitlementInfoMapperKt.map} produces. {@code expirationDate} is
     * null on purpose: JS treats a missing expiry as "never expires", so the
     * entitlement reads as active indefinitely.
     */
    private static Map<String, Object> proEntitlement() {
        long now = System.currentTimeMillis();
        Map<String, Object> m = new LinkedHashMap<>();
        m.put("identifier", "pro");
        m.put("isActive", Boolean.TRUE);
        m.put("willRenew", Boolean.TRUE);
        m.put("periodType", "NORMAL");
        m.put("latestPurchaseDateMillis", now);
        m.put("latestPurchaseDate", iso(now));
        m.put("originalPurchaseDateMillis", now);
        m.put("originalPurchaseDate", iso(now));
        m.put("expirationDateMillis", null);
        m.put("expirationDate", null);
        m.put("store", "PLAY_STORE");
        m.put("productIdentifier", "pro");
        m.put("productPlanIdentifier", null);
        m.put("isSandbox", Boolean.FALSE);
        m.put("unsubscribeDetectedAt", null);
        m.put("unsubscribeDetectedAtMillis", null);
        m.put("billingIssueDetectedAt", null);
        m.put("billingIssueDetectedAtMillis", null);
        m.put("ownershipType", "OWNED");
        m.put("verification", "NOT_REQUESTED");
        return m;
    }

    /** ISO-8601 UTC timestamp, matching {@code MappersHelpersKt.toIso8601}. */
    private static String iso(long millis) {
        SimpleDateFormat f = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US);
        f.setTimeZone(TimeZone.getTimeZone("UTC"));
        return f.format(new Date(millis));
    }
}

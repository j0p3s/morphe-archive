package app.morphe.patches.multiaccounts

import app.morphe.patcher.Fingerprint
import app.morphe.patcher.patch.bytecodePatch
import app.morphe.patcher.patch.resourcePatch
import app.morphe.patches.shared.Constants.COMPATIBILITY_MULTIACCOUNTS
import app.morphe.patches.shared.replaceWithReturnNull
import app.morphe.patches.shared.replaceWithReturnVoid
import org.w3c.dom.Element

/**
 * Privacy-controls patch suite for Multiple Accounts (com.excelliance.multiaccounts).
 *
 * Universal Classification Matrix:
 * - Remove Permissions: UNIVERSAL_OK (UniversalPrivacyPermissionsPatch handles manifest-level
 *   stripping; dedicated variant omitted because host container permissions are required by
 *   cloned guest apps).
 * - Go Fully Offline: NOT_APPLICABLE (Offline Gate fails: virtualization container requires
 *   INTERNET for guest app connectivity).
 * - Allow Screen Capture / Screenshots: UNIVERSAL_OK (App-own activities do not set FLAG_SECURE
 *   or SurfaceView.setSecure; window flag call sites exist solely inside ad SDKs which are
 *   already disabled in MultiaccountsTelemetryPatch).
 * - Lock Screen Brightness / Force Screen Timeout: UNIVERSAL_OK (Universal-only; no app-specific
 *   brightness or timeout overrides).
 * - Share-Link Tracking Stripping: UNIVERSAL_OK / TRANSITIVELY_DEAD (Single 'gclid=%s' hit is
 *   inside GMS measurement zzkx, neutralized by telemetry patch; AppealActivity email builder
 *   appends zero tracking params).
 * - Identifiers & Telephony: TRANSITIVELY_DEAD / PRESERVED (46 identifier and 93 telephony hits
 *   reside in ad SDKs already neutralized; app-own IdentifierUtils uses coroutine suspend lambdas
 *   and callback mechanisms; getInstalledPackages is virtualization-critical for clone target selection).
 * - Sensors, Contacts, Battery: TRANSITIVELY_DEAD (Listeners and receivers reside in ad SDKs and
 *   Picasso image loader).
 *
 * Dedicated Implementation Needed:
 * 1. Clipboard Neutralization: ByteDance AppLog LinkUtils.getParamFromClipboard reads system
 *    clipboard looking for 'datatracer:' attribution links. Stubbed with null.
 * 2. Certificate Pinning & User-CA Trust: Enables user CA certificate trust in
 *    network_security_config.xml while preserving cleartext traffic, and neutralizes OkHttp
 *    CertificatePinner check methods for MITM traffic analysis.
 */

// --- Resource Patch Contracts ---

@Suppress("unused")
val multiaccountsPrivacyConfigResourcePatch = resourcePatch(
    name = "Multiaccounts Privacy Setup",
    description = "Configures network_security_config.xml to trust user CA certificates for TLS inspection while preserving cleartext traffic.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_MULTIACCOUNTS)

    execute {
        val configFile = get("res/xml/network_security_config.xml")
        if (!configFile.exists()) {
            println("[Multiaccounts Privacy] res/xml/network_security_config.xml not found - skipping config patch.")
            return@execute
        }

        document(configFile.absolutePath).use { doc ->
            val baseConfigs = doc.getElementsByTagName("base-config")
            val baseConfig = if (baseConfigs.length > 0) {
                baseConfigs.item(0) as Element
            } else {
                val elem = doc.createElement("base-config")
                doc.documentElement.appendChild(elem)
                elem
            }

            val trustAnchorsList = baseConfig.getElementsByTagName("trust-anchors")
            val trustAnchors = if (trustAnchorsList.length > 0) {
                trustAnchorsList.item(0) as Element
            } else {
                val elem = doc.createElement("trust-anchors")
                baseConfig.appendChild(elem)
                elem
            }

            val certs = trustAnchors.getElementsByTagName("certificates")
            var hasSystem = false
            var hasUser = false
            for (i in 0 until certs.length) {
                val cert = certs.item(i) as? Element ?: continue
                val src = cert.getAttribute("src")
                if (src == "system") hasSystem = true
                if (src == "user") hasUser = true
            }
            if (!hasSystem) {
                val systemCertElem = doc.createElement("certificates")
                systemCertElem.setAttribute("src", "system")
                trustAnchors.appendChild(systemCertElem)
            }
            if (!hasUser) {
                val userCertElem = doc.createElement("certificates")
                userCertElem.setAttribute("src", "user")
                trustAnchors.appendChild(userCertElem)
            }
        }

        println("[Multiaccounts Privacy] Configured network_security_config.xml: user CA certificates trusted, cleartext preserved.")
    }
}

// --- Bytecode Patch Contracts ---

private val linkUtilsClipboardFingerprint = Fingerprint(
    definingClass = "Lcom/bytedance/applog/alink/util/LinkUtils;",
    name = "getParamFromClipboard",
    parameters = listOf("Landroid/content/Context;"),
    returnType = "Lorg/json/JSONObject;",
)

private val certificatePinnerCheckListFingerprint = Fingerprint(
    definingClass = "Lokhttp3/CertificatePinner;",
    name = "check",
    parameters = listOf("Ljava/lang/String;", "Ljava/util/List;"),
    returnType = "V",
)

private val certificatePinnerCheckArrayFingerprint = Fingerprint(
    definingClass = "Lokhttp3/CertificatePinner;",
    name = "check",
    parameters = listOf("Ljava/lang/String;", "[Ljava/security/cert/Certificate;"),
    returnType = "V",
)

private val certificatePinnerCheckOkHttpFingerprint = Fingerprint(
    definingClass = "Lokhttp3/CertificatePinner;",
    name = "check\$okhttp",
    parameters = listOf("Ljava/lang/String;", "Lie/a;"),
    returnType = "V",
)

@Suppress("unused")
val multiaccountsPrivacyPatch = bytecodePatch(
    name = "Privacy Controls",
    description = "Neutralizes clipboard snooping in ByteDance AppLog attribution and disables OkHttp certificate pinning for network traffic auditing.",
    default = false,
) {
    compatibleWith(COMPATIBILITY_MULTIACCOUNTS)
    dependsOn(multiaccountsPrivacyConfigResourcePatch)

    execute {
        var patched = 0

        // 1. Neutralize LinkUtils clipboard reading
        val linkUtilsMethod = linkUtilsClipboardFingerprint.method
        linkUtilsMethod.replaceWithReturnNull()
        patched++

        // 2. Neutralize OkHttp CertificatePinner check methods
        val pinnerFingerprints = listOf(
            certificatePinnerCheckListFingerprint,
            certificatePinnerCheckArrayFingerprint,
            certificatePinnerCheckOkHttpFingerprint,
        )

        for (fp in pinnerFingerprints) {
            fp.method.replaceWithReturnVoid()
            patched++
        }

        if (patched == 0) {
            error("[Multiaccounts Privacy] No privacy targets were patched - failing closed.")
        }

        println("[Multiaccounts Privacy] Neutralized clipboard snooper and 3 OkHttp certificate pinning checks across 2 classes ($patched hooks applied).")
    }
}

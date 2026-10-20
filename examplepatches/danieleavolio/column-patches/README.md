# Column Patches

Morphe patches for the **Column** news app (`com.columnapp.column`).

## Patches

| Patch | Default | Description |
|---|---|---|
| **Block ads and trackers** | ✅ | Refuses ad/tracker requests made by Column's WebView |
| **Disable AppsFlyer attribution** | ⬜ | Stops the bundled AppsFlyer SDK from initialising (optional, may break referral deep links) |

## Why only ads + privacy?

Column is a Capacitor (WebView) application. Its premium state is issued by the backend
(`api.column.news` + RevenueCat entitlement) and is not decided by any code inside the APK.

The app's own perk table (`assets/public/chunk-7JNNW44E.js`) is explicit:

```js
this.PERKS = {
  premium:  ["Riassunti AI illimitati", "Podcast quotidiani personalizzati", "Zero pubblicità"],
  freemium: ["Riassunti AI illimitati", "Podcast quotidiani personalizzati"]
}
```

The **only** premium-exclusive benefit is the removal of ads. AI summaries and podcasts are part of
the free tier; full articles / audio / Column AI are delivered and authorised server-side.

So the set of features that can be modified client-side is exactly: *the ads*. That is what this
repository patches. Subscription state is never read, written or spoofed.

## Build

Morphe's Gradle plugin is published to GitHub Packages, which requires authentication even for
public packages. Provide a token with the `read:packages` scope.

```bash
# ~/.gradle/gradle.properties  (or ./gradle.properties, which is gitignored)
gpr.user=YOUR_GITHUB_USERNAME
gpr.key=YOUR_GITHUB_TOKEN
```

Then:

```bash
./gradlew buildAndroid
```

Output: `patches/build/libs/patches-*.mpp` plus an updated `patches-list.json`.
See `BUILD.md` for the console (CLI) patching flow.

## Apply

1. Get the Column XAPK (`1.3.7` / versionCode `327`).
2. In Morphe Manager, add this repository as a patch source (or load the `.mpp` locally),
   select **Block ads and trackers**, and patch.
3. Install the result. The original app must be uninstalled first (different signature).

## Layout

```
patches/src/main/kotlin/app/columnpatches/
├── shared/Constants.kt          # app + version compatibility
└── column/
    ├── Fingerprints.kt          # methods to hook
    └── ColumnPatches.kt         # patch definitions
extensions/adblock/              # Java injected into the target APK
└── .../AdBlocker.java           # host list + empty 204 response
docs/ANALYSIS.md                 # what the decompiled APK actually contains
```

## Credits & license

Based on the [Morphe patches template](https://github.com/MorpheApp/morphe-patches-template),
which derives from ReVanced. Licensed under GPLv3 — see `LICENSE` and `NOTICE`.
This project uses its own branding and is not affiliated with the Morphe project.

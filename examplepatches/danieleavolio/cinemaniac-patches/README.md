# Cinemaniac Patches

Morphe patches for **Cinemaniac** (`it.papalillo.moviestowatch`), the movie and TV tracker
by Antonio Papalillo.

## Patches

| Patch | Default | Description |
|---|---|---|
| **Disable ads** | ✅ | Stops the bundled Google Mobile Ads SDK from requesting any ad (banner, interstitial, rewarded, app open, native) |
| **Disable Advertising ID** | ✅ | Replaces the Google advertising identifier (GAID) with a fixed, non-unique value and enables limit ad tracking |

## What this does not do

It does not unlock the PRO tier, and it does not touch the subscription state in any way.

Cinemaniac's PRO features are: remove ads, custom categories, favourites, streaming
providers, themes and random selection. The entitlement is **validated server side** —
the app calls `https://api.cinemaniac.app/v1/validate/user` and the backend decides.
`PRO_LAST_VALIDATED` is only a local cache with an expiry, not the source of truth.

So there is no client side flag to flip, and forging one would be circumventing a
paywall rather than fixing a bug. This repository only removes the ads and the device
level identifier.

## Why the ads are patched natively

Cinemaniac is a **React Native** application. The whole UI and business logic live in
`assets/index.android.bundle`, which is Hermes bytecode — a dex patcher cannot touch it.
The Google Mobile Ads SDK is native though, and it is driven from JavaScript through
`io.invertase.googlemobileads`, so the patch hooks the SDK entry points themselves:

```
com.google.android.gms.ads.BaseAdView.loadAd(AdRequest)
com.google.android.gms.ads.admanager.AdManagerAdView.loadAd(AdManagerAdRequest)
com.google.android.gms.ads.interstitial.InterstitialAd.load(...)
com.google.android.gms.ads.admanager.AdManagerInterstitialAd.load(...)
com.google.android.gms.ads.rewarded.RewardedAd.load(...)            x2
com.google.android.gms.ads.rewardedinterstitial.RewardedInterstitialAd.load(...)  x2
com.google.android.gms.ads.appopen.AppOpenAd.load(...)               x3
com.google.android.gms.ads.AdLoader.loadAd(...)                      x2
```

All of them return `void` and none of them contains a `try/catch` block, so the patch is a
single `return-void` and cannot disturb any register or exception range.

`MobileAds.initialize()` is deliberately left alone: neutralising it would leave the
"ads initialised" promise of the React Native bridge pending forever and the app would
hang on start up.

## Build

Morphe's Gradle plugin is published on GitHub Packages, which requires authentication even
for public packages. Provide a token with the `read:packages` scope.

```bash
export GITHUB_ACTOR=your-username
export GITHUB_TOKEN=ghp_xxxxxxxxxxxxxxxxxxxx
./gradlew buildAndroid
```

Output: `patches/build/libs/patches-*.mpp`. See `BUILD.md` for the full setup.

## Apply

1. Get the Cinemaniac APK (5.0.7 / versionCode 172).
2. In Morphe Manager, add this repository as a patch source (or load the `.mpp` locally)
   and patch.
3. Install the result. The original app must be uninstalled first (different signature).

## Layout

```
patches/src/main/kotlin/app/cinemaniacpatches/
├── shared/Constants.kt          # app + version compatibility
└── cine/
    ├── Fingerprints.kt          # methods to hook
    └── CinemaniacPatches.kt     # patch definitions
docs/ANALYSIS.md                 # what the decompiled APK actually contains
```

## Credits & license

Based on the [Morphe patches template](https://github.com/MorpheApp/morphe-patches-template),
which derives from ReVanced. Licensed under GPLv3 — see `LICENSE` and `NOTICE`.
This project uses its own branding and is not affiliated with the Morphe project.

# Cinemaniac Patches

> **PATCHED BY MORPHE** 🛠️ — all patches in this repository are built for and applied
> with [Morphe](https://morphe.software) / Morphe Manager.
>
> ❤️ **Support the original developer:** Cinemaniac is an independent one-person app —
> see [Support the original developer](#support-the-original-developer) below.

Morphe patches for **Cinemaniac** (`it.papalillo.moviestowatch`), the movie and TV tracker
by Antonio Papalillo.

## Patches

<!-- PATCHES_START -->
> **[v1.2.0](https://github.com/danieleavolio/cinemaniac-patches/releases/tag/v1.2.0)**&nbsp;&nbsp;•&nbsp;&nbsp;`main`&nbsp;&nbsp;•&nbsp;&nbsp;1 patches total
<details open>
<summary>📦 Cinemaniac&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**🎯 Supported versions:**

| 5.0.7 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Unlock Pro](#unlock-pro) | Forces the Pro entitlement to always resolve as active, regardless of purchase or backend status. Support the original developer! |  |

</details>

<!-- PATCHES_END -->

## How Unlock Pro works

Cinemaniac is a **React Native** application: the whole UI and business logic live in
`assets/index.android.bundle` — 5.3 MB of Hermes bytecode that dex-level patchers cannot
touch. The Pro entitlement is therefore unlocked with two surgical byte patches directly
inside the Hermes v96 container (the patch is a `rawResourcePatch` and works on bundle
files, not on dex):

| File offset | Original bytes | Patched bytes | Effect |
|---|---|---|---|
| `0x003D132C` | `0F 03 03 02` | `78 03 62 62` | `_getIsPROfromDB`: the device-checksum comparison that decides `isPro` at startup is forced to always resolve `true` |
| `0x003CFA50` | `90 2F 04` | `62 8E 2E` | `_validateProStatus`: execution always enters the verified-PRO branch instead of requiring a successful backend purchase check |

Offsets are matched against exact expected bytes before any edit, so any other app version
fails cleanly instead of being corrupted.

The patch is defensive as well as surgical:

- refuses any file that is not a Hermes container or not bytecode version 96
- refuses to run if the bundle's embedded SHA-1 integrity footer does not match its
  content — it only patches the original, unmodified APK, never on top of foreign changes
- idempotent: an already-patched bundle is left untouched
- after editing, the SHA-1 footer is recomputed so the container stays well-formed

The release has been verified end-to-end: applying the `.mpp` to the original 5.0.7 APK
with the real Morphe patcher engine produces an `index.android.bundle` byte-identical
(SHA-256 `e30068a4264f2048e584a47d5d65b19e7913d05c1c60883ce91fd385081d187f`) to the
reference from the reverse-engineering analysis.

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

1. Get the original Cinemaniac APK (5.0.7 / versionCode 172).
2. In Morphe Manager, add this repository as a remote patch source:
   - direct link: `https://morphe.software/add-source?github=danieleavolio/cinemaniac-patches`
   - or manual endpoint:
     `https://raw.githubusercontent.com/danieleavolio/cinemaniac-patches/main/patches-bundle.json`
   - or download the `patches-*.mpp` asset from the latest release and import it as a
     local source.
3. Select the app, keep **Unlock Pro** enabled (it is on by default), patch and install.
   The original app must be uninstalled first (different signature).

## Layout

```
patches/src/main/kotlin/
├── app/daniele/patches/
│   ├── cinemaniac/UnlockPro.kt   # the Unlock Pro patch (Hermes byte patches)
│   └── shared/Constants.kt       # app signature + version compatibility
└── util/PatchListGenerator.kt    # generates patches-list.json for the release flow
docs/ANALYSIS.md                  # static analysis of the Cinemaniac 5.0.7 APK
```

## Support the original developer

Cinemaniac is developed and maintained by **Antonio Papalillo**, an independent
developer, and the **Unlock Pro** in-app purchase is his only source of revenue.

If you end up using Cinemaniac every day (spoiler: you will), consider buying the
PRO unlock directly inside the app — that's the best way to say thanks and keep the
project alive. Ratings and reviews help a lot too.

## Credits & license

Based on the [Morphe patches template](https://github.com/MorpheApp/morphe-patches-template),
which derives from ReVanced. Licensed under GPLv3 — see `LICENSE` and `NOTICE`.
This project uses its own branding and is not affiliated with the Morphe project.

# Tower offline patches (Morphe) - private

Targets The Tower v29.0.6 (build 1237), arm64-v8a only. Never published.

## Patches (patches/src/main/kotlin/app/tower/patches/tower)
- **Offline single-player** - verifies libil2cpp.so sha256, then applies the 32 byte patches in `NativePatches.kt`
  (direct-mode start, SDK NOPs, server clock, non-throwing Firestore/PlayFab/IAP stubs, SaveLoad id gates,
  shop gem amounts, gem-pack buttons grant gems).
- **Change package name** (options: `changePackageName`, `packageName`) - rewrites every
  `com.TechTreeGames.TheTower` in the manifest to `com.TechTreeGames.LocTower` so it installs next to the real game.

`NativePatches.kt` was generated from a byte diff of the original lib vs. the working offline build
(`tower_extra_patches.py` output). Regenerate it the same way if the patches change.

## Build (needs a GitHub token - yours to create)
The Morphe Gradle plugin is served from GitHub Packages, which needs a classic PAT with only `read:packages`.
Put it in `%USERPROFILE%\.gradle\gradle.properties` (not in this repo):

    gpr.user=<your github username>
    gpr.key=<your token>

Then:

    set JAVA_HOME=C:\Program Files\Eclipse Adoptium\jdk-21.0.12.101-hotspot
    gradlew.bat buildAndroid

Output: `patches/build/libs/patches-*.mpp`. Load it in Morphe Manager (Patches -> add local bundle), pick the
original split APK set (base + split_config.arm64_v8a, as an APKM/XAPK/merged APK), keep both patches enabled.

## Unverified (not compiled/run yet)
- Kotlin has not been compiled: the plugin could not be resolved without the token.
- `get("lib/arm64-v8a/libil2cpp.so")` writing back into the rebuilt APK is expected from the API docs
  but untested.
- Whether the rebuilt resources.arsc carries the new package name (Unity needs it). If the clone
  throws Resources$NotFoundException at start, the arsc package header must be renamed too.

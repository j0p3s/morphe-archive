# SoundCloud Morphe port report

## Components separated

### Existing patches
- Go+ / Premium: subscription and feature checks.
- AMOLED: resources and theme colors.
- Telemetry: analytics suppression.

### Reconstructed extension

Download routing extension:
- intercepts track, set and playlist URLs
- stores resolved SoundCloud URLs
- redirects actions to configured downloader package
- modifies native download visibility/state
- supports Morphe icon aliases

## Decompiled evidence

Classes found in modified APK:

- app.morphe.extension.soundcloud.DownloadButtonHook
- app.morphe.extension.soundcloud.DownloadHookSettings
- app.morphe.extension.soundcloud.MorpheUiVisibility
- com.soundcloud.android.launcher.AppIcon

Hook call sites:

- TrackDetailsPagePresenter
- PlaylistBottomSheetViewModel
- Playlist menu items
- Library links
- Settings download UI

## Verification status

APK decompile: OK
Smali extraction: OK
Manifest extraction: OK
Runtime payload: 165/165 classes, valid DEX header/checksum/signature
Target fingerprints: 21/21 hook points resolved for `2026.08.26-release`
Inline Smali: no comment-only `addInstructions` blocks remain
Patched reference Smali assembly: OK

Remaining validation requires building the Morphe patch repository with the exact Morphe SDK version used by the target build.

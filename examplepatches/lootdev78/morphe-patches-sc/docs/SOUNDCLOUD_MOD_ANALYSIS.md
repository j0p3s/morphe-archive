# SoundCloud Morphe reconstruction

## Identified custom components

From `SoundCloud_Morphe_Adaptive_Full_Black.apk`:

- `app.morphe.extension.soundcloud.DownloadButtonHook`
- `app.morphe.extension.soundcloud.DownloadHookSettings`
- `app.morphe.extension.soundcloud.MorpheUiVisibility`
- `app.morphe.extension.soundcloud.MorpheSettings*`
- `LauncherActivityMorphe01..09IconAlias`

## Separation

| Component | Area |
|---|---|
| Go+ | Feature/Premium checks |
| AMOLED | Resources/theme |
| Telemetry | Analytics hooks |
| Download hook | Intent/deeplink/UI routing |
| Icon switcher | Android activity aliases |

## Icon switcher

The adaptive APK manifest contains Morphe icon aliases:

`LauncherActivityMorphe01IconAlias` ... `LauncherActivityMorphe09IconAlias`

These should remain a separate icon extension because they are manifest/resource based.

# Support

This bundle is a hobby project with one maintainer. Issues are the only channel, and each one tracks one thing.

| You want | Open |
| --- | --- |
| Patches for an app not supported yet | [App request](https://github.com/hxreborn/morphe-patches/issues/new?template=app_request.yml) |
| A supported app has a newer version than the README lists | [App update](https://github.com/hxreborn/morphe-patches/issues/new?template=app_update.yml) |
| Patching stopped with an error, or the APK would not install | [Patching or install failed](https://github.com/hxreborn/morphe-patches/issues/new?template=bug_report.yml) |
| The patched app opens but misbehaves | [Patched app does not work](https://github.com/hxreborn/morphe-patches/issues/new?template=not_working.yml) |
| A new feature for a supported app | [Feature request](https://github.com/hxreborn/morphe-patches/issues/new?template=feature_request.yml) |

How issues run here:

- A request ends with the version it shipped. The README lists the supported versions under each app. A newer version is a new app update request, one per version.
- Closed issues lock 7 days after closing, unless labelled `discussion`. A request posted on a closed issue gets one pointer to the right form.
- A bot reads the form fields when an issue is opened or edited. It closes reports it can't act on and reopens them once an edit fixes the gap. The `bot-wrong` label marks a misfire and stops it.
- Issues waiting on the reporter are marked stale after 14 days and close a week later. Other open issues: 30 days, then a week. App requests and app updates don't go stale.
- Features the server enforces can't be patched. Other bundles' patches belong to their own trackers: find the bundle at [morphe-patches.software](https://morphe-patches.software/#apps).

# 🧩 Vanta Surf patches for Morphe

Morphe patch bundle for **Vanta Surf** (`com.vvv.velvetreel.treel`), a lightweight
WebView browser that ships a short drama player with ads.

## How to use these patches

Add this repository as a patch source in Morphe:

```
https://github.com/vitorhubdev/vanta-surf-morphe-patches
```

Or import the bundle file directly in the Morphe Manager (`Import` → `VantaSurf-morphe-patches-1.0.0.mpp`).

## 🩹 Patches

| Patch | Description |
|---|---|
| Remove ads | Removes the interstitial ads shown between episodes. |
| Remove native ads | Removes the native ads injected into the feed. |
| Unlock episodes | Plays episodes without waiting for the watch an ad popup. |
| Disable telemetry | Disables Adjust, AppsFlyer, Facebook App Events and advertising ID collection. |

Supported app version: **5.0.2** (version code 50002).

## How it works

The app keeps its ads in `SemProduceStep`, the ad manager of the short drama player.

* `SemProduceStep.interceptBeforePlay(I)Z` is the single entry point the player uses to ask
  whether playback should be interrupted by an ad. Returning `false` removes the interstitials.
* `SemProtocolPartial.showNativeAd(View)V` injects the native ads into the feed.
* `SemProtocolPartial` also implements the "watch an ad to keep watching" popup
  (`n1(II)Z`) and the gate that refuses to resume the playback while that popup is pending
  (`V(Ljava/lang/String;)Z`). Both are disabled, so the episodes play straight away.
* `SemLangMap` initializes Adjust, the Facebook App Events logger, the advertising ID
  collection and AppsFlyer in one block at the end of `Application.onCreate`. The patch
  returns before that block, which stops all of them while keeping the rest of the app intact.

Every target is matched with a [fingerprint](https://github.com/MorpheApp/morphe-patcher/blob/main/docs/2_2_1_fingerprinting.md)
that anchors on the method name, the signature and the instructions of the method,
so the patches keep working as long as the ad flow itself does not change.

## 🛠️ Building locally

```bash
./gradlew :patches:buildAndroid
```

The bundle is written to `patches/build/libs/patches-<version>.mpp`.

## 📜 License

GPLv3. See [LICENSE](LICENSE).

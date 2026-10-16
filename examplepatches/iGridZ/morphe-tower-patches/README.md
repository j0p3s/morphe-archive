# 👋🧩 Morphe Patches template

Template repository for Morphe Patches.

## ❓ About

Patches for apps I like.

<!-- TODO: Update this about section with a brief introduction/summary about this repo and what it offers. -->

### How to use these patches

Click here to add these patches to Morphe: https://morphe.software/add-source?github=iGridZ/morphe-tower-patches

## 🩹 Patches list

<!-- PATCHES_START EXPANDED -->
> **[v1.2.2](https://github.com/iGridZ/morphe-tower-patches/releases/tag/v1.2.2)**&nbsp;&nbsp;•&nbsp;&nbsp;`main`&nbsp;&nbsp;•&nbsp;&nbsp;3 patches total
<details open>
<summary>📦 The Tower&nbsp;&nbsp;•&nbsp;&nbsp;3 patches</summary>
<br>

**🎯 Supported versions:**

| 29.0.6 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Change package name](#change-package-name) | Renames the package so the patched game installs beside the original. Unity resolves its string resources by package name, so the resource table is renamed together with the manifest. | • Change package name<br>• New package name |
| [Offline single-player](#offline-single-player) | Patches libil2cpp.so so The Tower starts and saves without Firebase/PlayFab/Play login, skips the ad/analytics SDK startup calls, fixes the server clock, makes gem packs grant their gems when tapped, and shows the real gem amounts in the shop. Only for v29.0.6 (build 1237). |  |
| [Seed Remote Config cache](#seed-remote-config-cache) | Copies the bundled Firebase Remote Config cache into the app on first start. Without it SaveLoad.Load fails offline (no shop/mini boss config), so the game never saves. |  |

</details>

<!-- PATCHES_END -->

### 🛠️ Building locally

- Run `./gradlew buildAndroid`
- The built patches .mpp file is found in `patches/build/libs/patches-*.mpp`
- Patch the mpp file using [Morphe-Desktop](https://github.com/MorpheApp/morphe-desktop)
  like any other patch bundle.

See the [Morphe documentation](https://github.com/MorpheApp/morphe-documentation) for more information.

## 📜 License

UserXYZ Patches are licensed under the [GNU General Public License v3.0](LICENSE)

# 🧩 SailHighSea Morphe Patches

Patches for [Morphe](https://morphe.software), made by SailHighSea.

## ❓ About

Patches for apps I use. Currently includes an ad remover for Clear Scanner (PDF scanner app).

### How to use these patches

Click here to add these patches to Morphe: https://morphe.software/add-source?github=SailHighSea/sailhighsea-patches

## 🩹 Patches list

<!-- PATCHES_START EXPANDED -->
> **[v1.0.0](https://github.com/SailHighSea/sailhighsea-patches/releases/tag/v1.0.0)**&nbsp;&nbsp;•&nbsp;&nbsp;`main`&nbsp;&nbsp;•&nbsp;&nbsp;3 patches total
<details open>
<summary>📦 Clear Scanner&nbsp;&nbsp;•&nbsp;&nbsp;2 patches</summary>
<br>

**🎯 Supported versions:**

| 10.2.18 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Local auto backup](#local-auto-backup) | Replaces cloud sync with a local backup. The Sync button opens the backup screen the first time. After a backup file has been chosen, Sync overwrites that file automatically. |  |
| [Remove ads](#remove-ads) | Removes banner, interstitial and app open ads, and the "get Pro" ad banners. |  |

</details>

<details open>
<summary>📦 Renamer&nbsp;&nbsp;•&nbsp;&nbsp;1 patch</summary>
<br>

**🎯 Supported versions:**

| 18.0 |
| :---: |

| 💊&nbsp;Patch | 📜&nbsp;Description | ⚙️&nbsp;Options |
|----------|----------------|-----------|
| [Remove ads](#remove-ads) | Removes the banner ad and the interstitial ads. |  |

</details>

<!-- PATCHES_END -->

### 🛠️ Building locally

- Run `./gradlew buildAndroid`
- The built patches .mpp file is found in `patches/build/libs/patches-*.mpp`
- Patch the mpp file using [Morphe-Desktop](https://github.com/MorpheApp/morphe-desktop)
  like any other patch bundle.

See the [Morphe documentation](https://github.com/MorpheApp/morphe-documentation) for more information.

## 📜 License

SailHighSea Patches are licensed under the [GNU General Public License v3.0](LICENSE)

Patches are built automatically with GitHub Actions on every release.

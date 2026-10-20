# Build requirements

## 1. JDK 17 or newer

```powershell
$env:JAVA_HOME = 'C:\Program Files\Eclipse Adoptium\jdk-25.0.2.10-hotspot'
```

## 2. Android SDK

Needs `build-tools` and at least one `platforms/android-XX`.

```powershell
$env:ANDROID_HOME = "$env:LOCALAPPDATA\Android\Sdk"
$env:ANDROID_SDK_ROOT = $env:ANDROID_HOME
```

## 3. GitHub token (mandatory)

Morphe's Gradle plugin `app.morphe.patches` is published on
`https://maven.pkg.github.com/MorpheApp/registry`. GitHub Packages requires authentication for all
reads, including public packages, so the build fails with

```
Plugin [id: 'app.morphe.patches', version: '1.3.4'] was not found
```

until a token is configured.

Create a **classic** Personal Access Token with the `read:packages` scope
(Settings → Developer settings → Personal access tokens → Tokens (classic)), then either:

```powershell
# environment variables
$env:GITHUB_ACTOR = 'your-username'
$env:GITHUB_TOKEN = 'ghp_xxxxxxxxxxxxxxxxxxxx'
```

or configure it in your **user-level** Gradle properties (`~/.gradle/gradle.properties`), which is
outside the repository:

```properties
gpr.user=your-username
gpr.key=ghp_xxxxxxxxxxxxxxxxxxxx
```

> ⚠️ Do **not** put the token in the project `gradle.properties`: that file is already
> tracked by git in this repository, and a `.gitignore` rule has no effect on tracked files.
> If you really want it there, run `git rm --cached gradle.properties` first.

## 4. Build

```powershell
.\gradlew.bat buildAndroid
```

`build` compiles and runs tests; `buildAndroid` produces the deployable patch bundle
(`patches/build/libs/patches-*.mpp`).

`generatePatchesList` refreshes `patches-list.json`, which is what Morphe Manager reads to
list the available patches.

## 5. Patch an APK from the console (optional)

The official CLI lives in [morphe-desktop](https://github.com/MorpheApp/morphe-desktop):

```bash
git clone -b dev https://github.com/MorpheApp/morphe-desktop
cd morphe-desktop && ./gradlew build && cd ..

java -jar morphe-desktop/build/libs/morphe-desktop*-all.jar patch \
  --patches cinemaniac-patches/patches/build/libs/patches-*.mpp \
  --out cinemaniac-patched.apk \
  cinemaniac-5.0.7.apk \
  --install
```

## 6. CI release

The template's `Release` workflow is currently **disabled** (`disabled_manually`). It runs
`semantic-release`, which invokes Gradle, and Gradle cannot resolve `app.morphe.patches` with the
automatic `GITHUB_TOKEN` because that token cannot read the MorpheApp registry.

To re-enable it:

1. Create a classic PAT with `repo` + `read:packages`.
2. Add it as a repository secret named `GPR_TOKEN`
   (`gh secret set GPR_TOKEN`).
3. Run `gh workflow enable Release`.

Releases published that way attach `patches-<version>.mpp` and update `patches-bundle.json`.
Until then, publish releases manually:

```powershell
gh release create v1.0.0 patches/build/libs/patches-1.0.0.mpp --title v1.0.0
```

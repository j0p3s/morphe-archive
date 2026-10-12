#!/usr/bin/env bash
set -euo pipefail
cd "$(dirname "$0")"
sdk=${ANDROID_HOME:-$HOME/Android/Sdk}
android_jar=$sdk/platforms/android-37.0/android.jar
d8=$sdk/build-tools/37.0.0/d8
out=$(mktemp -d)
trap 'rm -rf "$out"' EXIT
javac -nowarn -source 8 -target 8 -d "$out/classes" -cp "$android_jar" stub/*.java DevCheckRestored.java
"$d8" --release --min-api 23 --lib "$android_jar" --classpath "$out/classes" --output "$out" "$out"/classes/DevCheckRestored*.class
baksmali d --sequential-labels -o "$out/smali" "$out/classes.dex"
for smali in "$out"/smali/DevCheckRestored*.smali; do
  sed -E '/^\s*\.(source|line) /d' "$smali" >"../../src/main/resources/devcheck/restored/$(basename "$smali")"
done

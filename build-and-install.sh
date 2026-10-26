#!/usr/bin/env bash
# Build and install script for QuizTV on Xiaomi Mi TV Stick (MiTV-AESP0)
set -euo pipefail

PROJECT_DIR="$(cd "$(dirname "$0")" && pwd)"
ADB="${ADB:-$HOME/Library/Android/sdk/platform-tools/adb}"
TV_SERIAL="${TV_HOST:-192.168.0.121}:${TV_PORT:-5555}"

export JAVA_HOME="${JAVA_HOME:-/Library/Java/JavaVirtualMachines/temurin-21.jdk/Contents/Home}"
export ANDROID_HOME="${ANDROID_HOME:-$HOME/Library/Android/sdk}"

echo "=== Building QuizTV Debug APK ==="
cd "$PROJECT_DIR"
./gradlew assembleDebug

APK_PATH="$PROJECT_DIR/app/build/outputs/apk/debug/app-debug.apk"
if [ ! -f "$APK_PATH" ]; then
  echo "Error: APK not found at $APK_PATH" >&2
  exit 1
fi

echo "=== Connecting to TV at $TV_SERIAL ==="
"$ADB" connect "$TV_SERIAL" || true

echo "=== Pushing and installing QuizTV APK ==="
"$ADB" -s "$TV_SERIAL" push "$APK_PATH" /data/local/tmp/quiztv.apk
"$ADB" -s "$TV_SERIAL" shell "pm install -r /data/local/tmp/quiztv.apk"
"$ADB" -s "$TV_SERIAL" shell "rm -f /data/local/tmp/quiztv.apk"

echo "=== AOT compiling to speed (-m speed) ==="
"$ADB" -s "$TV_SERIAL" shell "cmd package compile -m speed -f com.clean.quiztv" || true

echo "=== Launching QuizTV on TV ==="
"$ADB" -s "$TV_SERIAL" shell "am start -n com.clean.quiztv/.MainActivity"

echo "=== Done! QuizTV is running on TV ==="

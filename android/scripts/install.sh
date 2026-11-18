#!/bin/bash
# Installs build/VPUNote-dev.apk on the running emulator or phone, grants what a person would
# (microphone, notifications, 显示在其他应用上层) and opens it.
set -euo pipefail
cd "$(dirname "$0")/.."
adb="$HOME/.vpunote-android/sdk/platform-tools/adb"; pkg=com.vpupts.vpunote.dev
"$adb" install -r build/VPUNote-dev.apk >/dev/null
"$adb" shell pm grant $pkg android.permission.RECORD_AUDIO
# Android 13+ only; older systems show notifications without asking.
[ "$("$adb" shell getprop ro.build.version.sdk | tr -d "\r")" -ge 33 ] && "$adb" shell pm grant $pkg android.permission.POST_NOTIFICATIONS
"$adb" shell appops set $pkg SYSTEM_ALERT_WINDOW allow
"$adb" shell am start -n $pkg/com.vpupts.vpunote.MainActivity >/dev/null
echo "installed $pkg"

#!/bin/bash
# Drives the dev build in the emulator or a phone (adb): scripts/vpunotectl.sh state | start mic |
# feed en.wav | sample | view subtitles | capture on | stop | … (see DebugReceiver.kt). Prints the
# app's one-line JSON reply. `push <file>` copies a file into the app's files/ for feed and import;
# `key` copies the Mac app's Ling key in (never printed), `aikey <file>` another service's; `log`
# prints the debug log.
set -euo pipefail
adb="$HOME/.vpunote-android/sdk/platform-tools/adb"
pkg=com.vpupts.vpunote.dev
case "${1:-state}" in
  push)
    "$adb" push "$2" /data/local/tmp/vpunote-push >/dev/null
    "$adb" shell "run-as $pkg sh -c 'cat /data/local/tmp/vpunote-push > files/$(basename "$2")'; rm /data/local/tmp/vpunote-push"
    echo "files/$(basename "$2")"; exit 0 ;;
  key)
    f="${VPUNOTE_KEY_FILE:-$HOME/Library/Application Support/LiveSubs Dev/ai-key-gemini}"
    [ -f "$f" ] || f="$HOME/Library/Application Support/LiveSubs/ai-key-gemini"
    "$adb" shell "run-as $pkg sh -c 'mkdir -p files && cat > files/ai-key-gemini && chmod 600 files/ai-key-gemini'" < "$f"
    set -- key reload ;;
  aikey)
    # Another service's key from the Mac app (`aikey ai-key-zenmux`, after `provider zenmux`): the
    # app seals it as the current service's key and deletes the plain copy. Never printed.
    f="${VPUNOTE_KEY_FILE:-$HOME/Library/Application Support/LiveSubs Dev/$2}"
    "$adb" shell "run-as $pkg sh -c 'cat > files/key-import'" < "$f"
    set -- key import key-import ;;
  log) exec "$adb" shell "run-as $pkg cat cache/vpunote-dev-debug.log" ;;
esac
send() { "$adb" shell am broadcast -a com.vpupts.vpunote.dev.DEBUG -p "$pkg" -f 0x20 --es cmd "'$*'" 2>&1; }
out=$(send "$@"); printf "%s" "$out" | grep -q "data=" || { sleep 2; out=$(send "$@"); }
data=$(printf '%s' "$out" | sed -n 's/.*data="\(.*\)"$/\1/p' | tr -d '\r')
[ -n "$data" ] && echo "$data" || { echo "$out" >&2; exit 1; }

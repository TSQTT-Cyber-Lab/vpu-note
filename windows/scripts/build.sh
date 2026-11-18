#!/bin/bash
# Builds the Windows app on the Mac: build/VPUNote-win-x64/ and build/VPUNote-win-arm64/, each a
# self-contained VPUNote.exe with everything inside it. RELEASE=1 for a release build, which also
# writes build/VPUNote-<v>-Windows-x64.exe and -ARM64.exe (the same exe, for people to download);
# else a dev build, which keeps its records under "VPUNote Dev" and opens the debug pipe.
# The icon is committed (src/VPUNote/Assets/vpunote.ico); the onboarding sample is made from Resources/sample.
set -euo pipefail
cd "$(dirname "$0")/.."
root=..
mkdir -p build/icon src/VPUNote/Assets

# The icon is committed (src/VPUNote/Assets/vpunote.ico, the university's shield): not regenerated here.

# The onboarding sample as 16 kHz WAV: SoundPlayer and the file source both read WAV only.
# afconvert writes WAVE_FORMAT_EXTENSIBLE plus a FLLR chunk, which SoundPlayer refuses
# (InvalidOperationException), so rewrite it as a plain PCM WAV.
afconvert -f WAVE -d LEI16@16000 -c 1 "$root/Resources/sample/sample.m4a" build/icon/sample.wav
python3 - build/icon/sample.wav src/VPUNote/Assets/sample.wav <<'PY'
import struct, sys
d = open(sys.argv[1], "rb").read(); i = 12; pcm = b""
while i < len(d):
    n = int.from_bytes(d[i+4:i+8], "little")
    if d[i:i+4] == b"data": pcm = d[i+8:i+8+n]
    i += 8 + n + (n & 1)
fmt = struct.pack("<HHIIHH", 1, 1, 16000, 32000, 2, 16)
open(sys.argv[2], "wb").write(b"RIFF" + struct.pack("<I", 4 + 8 + len(fmt) + 8 + len(pcm)) + b"WAVE" + b"fmt " + struct.pack("<I", len(fmt)) + fmt + b"data" + struct.pack("<I", len(pcm)) + pcm)
PY

config=Debug; [ "${RELEASE:-}" = 1 ] && config=Release
for rid in win-x64 win-arm64; do
  out="build/VPUNote-$rid"
  rm -rf "$out"
  scripts/dotnet.sh publish src/VPUNote -c "$config" -r "$rid" -o "$out" -p:DebugType=embedded -v quiet -nologo 2>&1 | grep -E ' error |warning CS' | sort -u || true
  [ -f "$out/VPUNote.exe" ] || { echo "build failed for $rid" >&2; exit 1; }
  # The exe is the whole app (the UI text and the sample are inside it): the installer is a copy.
  [ "$(ls "$out" | grep -v '^VPUNote.exe$' | grep -v '^test$' | grep -v '^VPUNote Dev.cmd$' || true)" = "" ] || { echo "unexpected files beside VPUNote.exe in $out: $(ls "$out")" >&2; exit 1; }
  if [ "$config" = Release ]; then
    arch=x64; [ "$rid" = win-arm64 ] && arch=ARM64
    setup="build/VPUNote-$(cat "$root/VERSION")-Windows-$arch.exe"
    cp "$out/VPUNote.exe" "$setup"; echo "$setup"
  fi
  if [ "$config" = Debug ]; then
    printf '@echo off\r\nstart "" "%%~dp0VPUNote.exe" --dev\r\n' > "$out/VPUNote Dev.cmd"
  fi
  echo "$out/VPUNote.exe ($(du -h "$out/VPUNote.exe" | cut -f1))"
done

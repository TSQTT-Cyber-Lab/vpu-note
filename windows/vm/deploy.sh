#!/bin/bash
# Quits VPUNote in the test VM and copies in fresh builds (run scripts/build.sh first).
set -euo pipefail
here="$(cd "$(dirname "$0")" && pwd)"
"$here/vmctl.sh" ssh 'Get-Process VPUNote -ErrorAction SilentlyContinue | Stop-Process -Force; Start-Sleep 1; Remove-Item C:\vpunote-test\VPUNote-win-arm64, C:\vpunote-test\VPUNote-win-x64 -Recurse -Force -ErrorAction SilentlyContinue; exit 0'
for rid in win-arm64 win-x64; do "$here/vmctl.sh" put "$here/../build/VPUNote-$rid" C:/vpunote-test/; done
"$here/vmctl.sh" put "$here/guest.ps1" C:/vpunote-test/
echo deployed

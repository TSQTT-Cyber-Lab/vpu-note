#!/bin/bash
# Runs vpunote-cli (the engines on the Mac): scripts/cli.sh test | listen <wav> | hear | segments | roundtrip.
# Builds it first; a proxy in the environment is passed to Java, which ignores http(s)_proxy.
set -euo pipefail
cd "$(dirname "$0")/.."
scripts/gradle.sh :cli:installDist >/dev/null
props="-Dvpunote.resources=$PWD/../Resources -Dvpunote.androidStrings=$PWD/app/src/main/strings -Dfile.encoding=UTF-8 -Dstdout.encoding=UTF-8"
p="${https_proxy:-${HTTPS_PROXY:-}}"
if [ -n "$p" ]; then p="${p#*://}"; p="${p%/}"; props="$props -Dhttps.proxyHost=${p%:*} -Dhttps.proxyPort=${p##*:}"; fi
JAVA_HOME="$HOME/.vpunote-android/jdk/Contents/Home" JAVA_OPTS="$props" exec cli/build/install/vpunote-cli/bin/vpunote-cli "$@"

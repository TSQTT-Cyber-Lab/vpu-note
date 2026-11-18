#!/bin/bash
# Makes the release signing key, once ever: ~/.vpunote-android/release.jks (PKCS12, RSA 4096, 30
# years), its random password in the login Keychain (vpunote-android-release). Every release must be
# signed with this same key, or phones can't update to it: back up the file and the password.
# --github also puts both in the repo's Actions secrets, for the release workflow.
set -euo pipefail
ks="${VPUNOTE_KEYSTORE:-$HOME/.vpunote-android/release.jks}"
keytool="$HOME/.vpunote-android/jdk/Contents/Home/bin/keytool"
if [ -e "$ks" ]; then
  echo "$ks already exists: never replace it (installed copies could no longer update)" >&2
else
  pw=$(openssl rand -hex 24)
  mkdir -p "$(dirname "$ks")"
  "$keytool" -genkeypair -keystore "$ks" -storetype PKCS12 -alias vpunote -keyalg RSA -keysize 4096 \
    -validity 10950 -dname "CN=VPUNote, O=wquguru" -storepass "$pw" -keypass "$pw" >/dev/null
  chmod 600 "$ks"
  security add-generic-password -a "$USER" -s vpunote-android-release -w "$pw" -U
  echo "made $ks; the password is in the Keychain (security find-generic-password -s vpunote-android-release -w)"
fi
pw=$(security find-generic-password -s vpunote-android-release -w)
"$keytool" -list -v -keystore "$ks" -storepass "$pw" -alias vpunote | grep -E "Owner|SHA256:"
if [ "${1:-}" = --github ]; then
  base64 -i "$ks" | gh secret set ANDROID_KEYSTORE_BASE64
  printf %s "$pw" | gh secret set ANDROID_KEYSTORE_PASSWORD
  echo "set ANDROID_KEYSTORE_BASE64 and ANDROID_KEYSTORE_PASSWORD"
fi
echo "back up $ks and its password somewhere safe (a password manager): losing either means a new app id"

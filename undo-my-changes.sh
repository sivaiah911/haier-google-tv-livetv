#!/bin/sh
# Turns back on every app this session turned off, and restores the settings that were changed.
# Usage: sh undo-my-changes.sh <TV_IP:PORT>     e.g. sh undo-my-changes.sh <TV_IP:PORT>
# Needs Wireless debugging (or USB/Network debugging) on, and adb (tools/platform-tools/adb).
cd "$(dirname "$0")"
export PATH="$PWD/tools/platform-tools:$PATH"
[ -n "$1" ] || { echo "Usage: sh undo-my-changes.sh <TV_IP:PORT>" >&2; exit 1; }
adb connect "$1" >/dev/null 2>&1 || true
export ANDROID_SERIAL="$1"
[ "$(adb get-state 2>/dev/null)" = device ] || { echo "Can't reach $1" >&2; exit 1; }
# Apps turned off (one per line in changes-undo-list.txt, lines starting with APP)
grep '^APP ' changes-undo-list.txt | awk '{print $2}' | while read p; do
  adb shell pm enable "$p" >/dev/null 2>&1 && echo "Turned $p back on"
done
# Settings changed (lines: SETTING <namespace> <key> <old value>)
grep '^SETTING ' changes-undo-list.txt | while read _ ns key old; do
  if [ "$old" = "null" ]; then adb shell settings delete "$ns" "$key" >/dev/null
  else adb shell settings put "$ns" "$key" "$old"; fi
  echo "Restored $ns $key = $old"
done
echo "Done. To also remove the tv-detox home screen, run: sh tv-detox/undo.sh <TV_IP:PORT>"

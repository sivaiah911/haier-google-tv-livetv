#!/bin/sh
# Adds a channel tile to the Live TV row of the home screen, from the Mac.
#   sh add-channel.sh <TV_IP:PORT> "Channel name" "link"
# Links: a Zee5 page link (with the 0-9-... id), a Sun NXT page link (sunnxt.com/live/.../<id>/...),
# a sunnxt:// link, a hotstar.com link, or app:<package> to just open an app.
# Example:
#   sh add-channel.sh <TV_IP:PORT> "Zee Telugu" "https://www.zee5.com/channels/details/zee-telugu/0-9-9z5383485"
#   sh add-channel.sh <TV_IP:PORT> reset        # back to the original channel list
cd "$(dirname "$0")"
export PATH="$PWD/tools/platform-tools:$PATH"
[ -n "$1" ] && [ -n "$2" ] || { echo "Usage: sh add-channel.sh <TV_IP:PORT> \"Name\" \"link\"   (or: reset)" >&2; exit 1; }
adb connect "$1" >/dev/null 2>&1 || true
export ANDROID_SERIAL="$1"
[ "$(adb get-state 2>/dev/null)" = device ] || { echo "Can't reach $1" >&2; exit 1; }
if [ "$2" = reset ]; then
  adb shell am start -n com.ansub.tvhome/.MainActivity --es stamp "$(date +%s)" --es channels reset >/dev/null
  echo "Channel list reset."; exit 0
fi
[ -n "$3" ] || { echo "Missing the link." >&2; exit 1; }
# Single quotes keep & and ? in the link intact on the TV's shell.
adb shell "am start -n com.ansub.tvhome/.MainActivity --es stamp $(date +%s) --es addchannel '$2|$3'" >/dev/null
echo "Added \"$2\". It's at the end of the Live TV row."

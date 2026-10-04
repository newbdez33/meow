#!/bin/bash
# shots.sh <device-key> <locale> <lang-key>: capture the two soundboard screens on an emulator.
# e.g. shots.sh android-phone zh-CN zh-Hans
set -euo pipefail
REPO=$(cd "$(dirname "$0")/../.." && pwd)
export PATH="$HOME/Library/Android/sdk/platform-tools:$PATH"
DEV=$1; LOCALE=$2; LANG_KEY=$3
case $LANG_KEY in en) CAT="I'm cute";; zh-Hans) CAT="可爱撒娇..";; ja) CAT="かわいい";; *) exit 2;; esac
OUT="$REPO/android/build/store-captures/$DEV"; mkdir -p "$OUT"
[[ $(adb get-serialno) == emulator-* ]] || { echo "Use a disposable emulator." >&2; exit 2; }
adb shell cmd locale set-app-locales jp.jacky.meow --locales "$LOCALE"
adb shell am force-stop jp.jacky.meow
adb shell am start -W -n jp.jacky.meow/.MainActivity --ez meowNoAds true --es meowStore preview >/dev/null
sleep 5
adb exec-out screencap -p > "$OUT/$LANG_KEY-1-grid.png"
python3 "$REPO/android/tool/tap.py" "$CAT" || { sleep 2; python3 "$REPO/android/tool/tap.py" "$CAT"; }
sleep 1.5
adb exec-out screencap -p > "$OUT/$LANG_KEY-2-selected.png"
file "$OUT/$LANG_KEY-1-grid.png" "$OUT/$LANG_KEY-2-selected.png"

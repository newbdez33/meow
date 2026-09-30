#!/bin/bash
# shots.sh <device-key> <locale> <lang-key>: capture the three store screens in one language on the running emulator
# e.g. shots.sh android-phone zh-CN zh-Hans  ->  android/build/store-captures/android-phone/zh-Hans-{1-grid,2-selected,3-remove-ads}.png
set -e
REPO=/Volumes/shit/orca/workspaces/meow/fiddler
export PATH="$HOME/Library/Android/sdk/platform-tools:$PATH"
DEV=$1; LOCALE=$2; LANG_KEY=$3
case $LANG_KEY in en) CAT="I'm cute"; CAN="Remove the ads";; zh-Hans) CAT="可爱撒娇.."; CAN="去掉广告";; ja) CAT="かわいい"; CAN="広告を消す";; esac
OUT="$REPO/android/build/store-captures/$DEV"; mkdir -p "$OUT"
adb root >/dev/null 2>&1 || true
adb shell "setprop persist.sys.locale $LOCALE; stop; start"
sleep 4
for i in $(seq 1 60); do [ "$(adb shell getprop sys.boot_completed 2>/dev/null | tr -d '\r')" = "1" ] && break; sleep 2; done
sleep 4
adb shell settings put global window_animation_scale 0 >/dev/null
adb shell settings put global transition_animation_scale 0 >/dev/null
adb shell settings put global animator_duration_scale 0 >/dev/null
adb shell am force-stop jp.jacky.meow
adb shell am start -W -n jp.jacky.meow/.MainActivity --ez meowNoAds true --es meowStore preview >/dev/null
sleep 5
adb exec-out screencap -p > "$OUT/$LANG_KEY-1-grid.png"
python3 "$REPO/android/tool/tap.py" "$CAT" || { sleep 2; python3 "$REPO/android/tool/tap.py" "$CAT"; }
sleep 1.5
adb exec-out screencap -p > "$OUT/$LANG_KEY-2-selected.png"
python3 "$REPO/android/tool/tap.py" "$CAN" || { sleep 2; python3 "$REPO/android/tool/tap.py" "$CAN"; }
sleep 3
adb exec-out screencap -p > "$OUT/$LANG_KEY-3-remove-ads.png"
adb shell input keyevent KEYCODE_BACK
python3 -c "
from PIL import Image
for n in ['1-grid','2-selected','3-remove-ads']:
    print('$LANG_KEY', n, Image.open('$OUT/$LANG_KEY-'+n+'.png').size)"

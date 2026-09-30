#!/usr/bin/env python3
"""Tap the centre of the on-screen element whose content description equals the argument.

    python3 android/tool/tap.py "I'm cute"

Reads `adb shell uiautomator dump`, so it works on any emulator or attached device.
"""
import html
import re
import subprocess
import sys


def main():
    wanted = sys.argv[1]
    subprocess.run(["adb", "shell", "uiautomator", "dump", "/sdcard/meow-ui.xml"], check=True, capture_output=True)
    xml = subprocess.run(["adb", "shell", "cat", "/sdcard/meow-ui.xml"], check=True, capture_output=True, text=True).stdout
    for node in re.finditer(r"<node [^>]*?/?>", xml):
        attrs = {key: html.unescape(value) for key, value in re.findall(r'(\S+?)="([^"]*)"', node.group(0))}
        if attrs.get("content-desc") == wanted:
            x1, y1, x2, y2 = map(int, re.findall(r"\d+", attrs["bounds"]))
            x, y = (x1 + x2) // 2, (y1 + y2) // 2
            subprocess.run(["adb", "shell", "input", "tap", str(x), str(y)], check=True)
            print("tapped", wanted, x, y)
            return
    sys.exit(f"no element with content description {wanted!r}")


if __name__ == "__main__":
    main()

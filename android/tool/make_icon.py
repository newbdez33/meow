#!/usr/bin/env python3
"""Build the launcher icons from the iOS app icon (a flat white cat on plain green).

    python3 android/tool/make_icon.py

Adaptive icon (API 26+): the green as the background layer, the keyed-out cat
as the foreground layer. Legacy icons (API 24-25): the square icon as is, plus
a circle-masked round variant.
"""
import pathlib

from PIL import Image, ImageDraw

ROOT = pathlib.Path(__file__).resolve().parents[2]
SRC = ROOT / "meow" / "Assets.xcassets" / "AppIcon.appiconset" / "ItunesArtwork@2x.png"
RES = ROOT / "android" / "app" / "src" / "main" / "res"
DENSITIES = {"mdpi": 1, "hdpi": 1.5, "xhdpi": 2, "xxhdpi": 3, "xxxhdpi": 4}
KEY_FULL = 120  # colour distance from the green at which a pixel is fully opaque


def keyed_cat(icon, green):
    """The icon with the flat green faded to transparent; anti-aliased edges get partial alpha."""
    out = icon.convert("RGBA").copy()
    pixels = out.load()
    for y in range(out.height):
        for x in range(out.width):
            r, g, b, _ = pixels[x, y]
            distance = abs(r - green[0]) + abs(g - green[1]) + abs(b - green[2])
            pixels[x, y] = (r, g, b, min(255, distance * 255 // KEY_FULL))
    return out


def foreground(cat, canvas_px):
    """The cat 79% wide, anchored to the bottom of the 108 dp canvas so the mask hides its cut bottom edge."""
    size = int(canvas_px * 0.79)
    layer = Image.new("RGBA", (canvas_px, canvas_px), (0, 0, 0, 0))
    layer.paste(cat.resize((size, size), Image.LANCZOS), ((canvas_px - size) // 2, canvas_px - size))
    return layer


def round_mask(image):
    """The image with everything outside the inscribed circle made transparent."""
    image = image.convert("RGBA")
    mask = Image.new("L", image.size, 0)
    ImageDraw.Draw(mask).ellipse((0, 0, image.width - 1, image.height - 1), fill=255)
    out = image.copy()
    out.putalpha(mask)
    return out


def main():
    icon = Image.open(SRC).convert("RGBA")
    green = icon.getpixel((8, 8))[:3]
    cat = keyed_cat(icon, green)
    for name, scale in DENSITIES.items():
        folder = RES / f"mipmap-{name}"
        folder.mkdir(parents=True, exist_ok=True)
        legacy = int(48 * scale)
        square = icon.resize((legacy, legacy), Image.LANCZOS)
        square.save(folder / "ic_launcher.png", optimize=True)
        round_mask(square).save(folder / "ic_launcher_round.png", optimize=True)
        foreground(cat, int(108 * scale)).save(folder / "ic_launcher_foreground.png", optimize=True)
    anydpi = RES / "mipmap-anydpi-v26"
    anydpi.mkdir(parents=True, exist_ok=True)
    adaptive = (
        '<?xml version="1.0" encoding="utf-8"?>\n'
        '<adaptive-icon xmlns:android="http://schemas.android.com/apk/res/android">\n'
        '    <background android:drawable="@color/ic_launcher_background"/>\n'
        '    <foreground android:drawable="@mipmap/ic_launcher_foreground"/>\n'
        '</adaptive-icon>\n'
    )
    (anydpi / "ic_launcher.xml").write_text(adaptive)
    (anydpi / "ic_launcher_round.xml").write_text(adaptive)
    (RES / "values" / "ic_launcher_background.xml").write_text(
        '<?xml version="1.0" encoding="utf-8"?>\n'
        '<resources>\n'
        f'    <color name="ic_launcher_background">#{green[0]:02X}{green[1]:02X}{green[2]:02X}</color>\n'
        '</resources>\n'
    )
    print("background", green)


if __name__ == "__main__":
    main()

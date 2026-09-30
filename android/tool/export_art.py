#!/usr/bin/env python3
"""Export the app's drawables from the design sources into drawable-nodpi.

    python3 android/tool/export_art.py            # cats from design/imagegen/cats/cNN.png when present, else the iOS 128 px files
    python3 android/tool/export_art.py --ios-only # always the iOS files (the fallback look)

Cats keep their size relative to the canvas (the iOS drawings are sized on
purpose) and become 512x512. The toolbar and tip art is trimmed to its content
first, like the iOS exports, then padded to a square.
"""
import argparse
import pathlib

from PIL import Image

ROOT = pathlib.Path(__file__).resolve().parents[2]
IOS_CATS = ROOT / "meow" / "Assets.xcassets" / "cats"
REDRAWS = ROOT / "design" / "imagegen" / "cats"
ORIGINALS = ROOT / "design" / "imagegen"
OUT = ROOT / "android" / "app" / "src" / "main" / "res" / "drawable-nodpi"
CAT_SIZE, NAV_SIZE, TIP_SIZE = 512, 128, 512
# The remove-ads toolbar button is the can (iOS 2.1 build 5 replaced the cup); the mug lives on the sheet.
ICONS = [
    ("nav_cat", "nav-cat.png", NAV_SIZE),
    ("nav_can", "tip-can.png", NAV_SIZE),
    ("tip_mug", "nav-cup.png", TIP_SIZE),
    ("tip_can", "tip-can.png", TIP_SIZE),
]


def key_white(image):
    """Make a pure white background transparent when an image has no transparency at all."""
    image = image.convert("RGBA")
    if image.getchannel("A").getextrema() != (255, 255):
        return image
    pixels = image.load()
    for y in range(image.height):
        for x in range(image.width):
            r, g, b, _ = pixels[x, y]
            if r > 245 and g > 245 and b > 245:
                pixels[x, y] = (r, g, b, 0)
    return image


def padded_square(image, size):
    """Pad to a square without trimming, then resize; relative subject size is preserved."""
    image = key_white(image)
    side = max(image.size)
    canvas = Image.new("RGBA", (side, side), (0, 0, 0, 0))
    canvas.paste(image, ((side - image.width) // 2, (side - image.height) // 2))
    return canvas.resize((size, size), Image.LANCZOS)


def trimmed_square(image, size):
    """Trim transparent margins, pad to a square with 4% breathing room on each side, resize."""
    image = key_white(image)
    box = image.getbbox()
    if box:
        image = image.crop(box)
    side = int(max(image.size) * 1.08)
    canvas = Image.new("RGBA", (side, side), (0, 0, 0, 0))
    canvas.paste(image, ((side - image.width) // 2, (side - image.height) // 2))
    return canvas.resize((size, size), Image.LANCZOS)


def cat_source(n, ios_only):
    redraw = REDRAWS / f"c{n:02d}.png"
    if redraw.is_file() and not ios_only:
        return redraw
    return IOS_CATS / f"c{n:02d}.imageset" / f"c{n:02d}@2x.png"


def main():
    parser = argparse.ArgumentParser()
    parser.add_argument("--ios-only", action="store_true")
    args = parser.parse_args()
    OUT.mkdir(parents=True, exist_ok=True)
    for n in range(1, 28):
        source = cat_source(n, args.ios_only)
        padded_square(Image.open(source), CAT_SIZE).save(OUT / f"c{n:02d}.png", optimize=True)
        print(f"c{n:02d}.png <- {source.relative_to(ROOT)}")
    for name, source, size in ICONS:
        trimmed_square(Image.open(ORIGINALS / source), size).save(OUT / f"{name}.png", optimize=True)
        print(f"{name}.png <- design/imagegen/{source}")


if __name__ == "__main__":
    main()

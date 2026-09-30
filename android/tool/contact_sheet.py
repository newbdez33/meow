#!/usr/bin/env python3
"""Tile the 27 iOS cats next to their redraws for the owner's review.

    python3 android/tool/contact_sheet.py  ->  android/build/cats-contact-sheet.png
"""
import pathlib

from PIL import Image, ImageDraw

import export_art as art

ROOT = pathlib.Path(__file__).resolve().parents[2]
OUT = ROOT / "android" / "build" / "cats-contact-sheet.png"
CELL, COLS = 160, 6


def main():
    rows = (27 + COLS - 1) // COLS
    sheet = Image.new("RGBA", (COLS * CELL * 2, rows * (CELL + 20)), (255, 246, 247, 255))
    draw = ImageDraw.Draw(sheet)
    for n in range(1, 28):
        col, row = (n - 1) % COLS, (n - 1) // COLS
        x, y = col * CELL * 2, row * (CELL + 20)
        old = art.padded_square(Image.open(art.cat_source(n, ios_only=True)), CELL)
        new = art.padded_square(Image.open(art.cat_source(n, ios_only=False)), CELL)
        sheet.paste(old, (x, y + 20), old)
        sheet.paste(new, (x + CELL, y + 20), new)
        draw.text((x + 4, y + 4), f"c{n:02d}  old | new", fill=(231, 106, 102, 255))
    OUT.parent.mkdir(parents=True, exist_ok=True)
    sheet.save(OUT)
    print(OUT)


if __name__ == "__main__":
    main()

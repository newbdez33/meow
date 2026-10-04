#!/usr/bin/env python3
"""Export approved widget art. Run with: uv run --with pillow --with cairosvg tool/export_widget_art.py"""
import io
import json
from pathlib import Path

import cairosvg
from PIL import Image, ImageOps

ROOT = Path(__file__).resolve().parents[1]
SOURCE = ROOT / "design/imagegen/widget-characters"
IOS = ROOT / "WidgetShared/Assets.xcassets"
ANDROID = ROOT / "android/app/src/main/res/drawable-nodpi"
SIZE = 600


def main():
    IOS.mkdir(parents=True, exist_ok=True)
    ANDROID.mkdir(parents=True, exist_ok=True)
    (IOS / "Contents.json").write_text(json.dumps({"info": {"author": "xcode", "version": 1}}, indent=2) + "\n")
    for name in ["ginger", "black", "gray", "sleepy", "pumpkin", "ghost", "classic"]:
        if name == "classic":
            svg = (ROOT / "design/widget-cat.svg").read_text()
            svg = svg.replace('viewBox="0 0 240 210"', 'viewBox="0 0 280 280"')
            svg = svg.replace("<title>", '<rect width="280" height="280" fill="#7cc58b"/><g transform="translate(20 28)"><title>')
            svg = svg.replace("</svg>", "</g></svg>")
            pixels = cairosvg.svg2png(bytestring=svg.encode(), output_width=SIZE, output_height=SIZE)
            art = Image.open(io.BytesIO(pixels)).convert("RGB")
        else:
            art = Image.open(SOURCE / f"{name}.png").convert("RGB").resize((SIZE, SIZE), Image.Resampling.LANCZOS)
        folder = IOS / f"widget_{name}.imageset"
        folder.mkdir(exist_ok=True)
        art.save(folder / "art.png", optimize=True)
        (folder / "Contents.json").write_text(json.dumps({
            "images": [{"filename": "art.png", "idiom": "universal"}],
            "info": {"author": "xcode", "version": 1},
        }, indent=2) + "\n")
        art.save(ANDROID / f"widget_{name}.png", optimize=True)
        # A template mask preserves eye and outline contrast without WidgetKit's
        # desaturated image mode, which can disable button hit testing.
        tinted = Image.new("RGBA", art.size, "white")
        tinted.putalpha(ImageOps.invert(ImageOps.grayscale(art)))
        tinted_folder = IOS / f"widget_{name}_tinted.imageset"
        tinted_folder.mkdir(exist_ok=True)
        tinted.save(tinted_folder / "art.png", optimize=True)
        (tinted_folder / "Contents.json").write_text(json.dumps({
            "images": [{"filename": "art.png", "idiom": "universal"}],
            "info": {"author": "xcode", "version": 1},
            "properties": {"template-rendering-intent": "template"},
        }, indent=2) + "\n")


if __name__ == "__main__":
    main()

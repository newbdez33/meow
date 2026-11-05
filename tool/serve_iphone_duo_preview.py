#!/usr/bin/env python3
"""Serve the design preview with only its required assets."""

import argparse
import functools
import json
from http.server import SimpleHTTPRequestHandler, ThreadingHTTPServer
from pathlib import Path
import shutil
import subprocess
import tempfile


class PreviewHandler(SimpleHTTPRequestHandler):
    def list_directory(self, path):
        self.send_error(404)
        return None

    def end_headers(self):
        self.send_header("Cache-Control", "no-cache")
        super().end_headers()


def export_preview(repo, destination):
    assets = destination / "assets"
    assets.mkdir()
    shutil.copy2(repo / "design/iphone-duo-preview.html", destination / "index.html")
    shutil.copy2(repo / "design/iphone-duo-preview.js", destination / "iphone-duo-preview.js")
    shutil.copy2(repo / "design/iphone-duo-cat.js", destination / "iphone-duo-cat.js")
    shutil.copy2(repo / "design/iphone-duo-assets/tuxedo-cat-belly-v1.png", assets / "tuxedo-cat-belly-v1.png")
    shutil.copy2(repo / "design/iphone-duo-play-lab.html", destination / "play-lab.html")

    def copy_image(name, category=""):
        source = repo / "meow/Assets.xcassets" / category / f"{name}.imageset"
        entries = json.loads((source / "Contents.json").read_text())["images"]
        image = max((entry for entry in entries if "filename" in entry),
                    key=lambda entry: int(entry.get("scale", "1x")[0]))
        shutil.copy2(source / image["filename"], assets / f"{name}.png")

    sounds = []
    for index in range(27):
        name = f"c{index + 1:02}"
        sound = f"m_{index + 1:03}"
        copy_image(name, "cats")
        shutil.copy2(repo / f"meow/resources/{sound}.mp3", assets / f"{sound}.mp3")
        sounds.append({"id": index, "image": f"assets/{name}.png",
                       "audio": f"assets/{sound}.mp3", "key": f"t{index + 1:02}"})

    for name in ["navCan", "navCat", "tipCan", "tipMug"]:
        copy_image(name)

    strings = {}
    for locale in ["en", "zh-Hans", "ja"]:
        source = repo / f"meow/{locale}.lproj/Localizable.strings"
        strings[locale] = json.loads(subprocess.check_output(
            ["plutil", "-convert", "json", "-o", "-", str(source)]))
    products = json.loads((repo / "meow/meow.storekit").read_text())["products"]
    prices = {product["productID"].split(".")[-1]: product["displayPrice"] for product in products}
    (destination / "iphone-duo-preview-data.json").write_text(
        json.dumps({"sounds": sounds, "strings": strings, "prices": prices}, ensure_ascii=False))
    shutil.copytree(repo / "design/iphone-duo-review", destination / "captures")


def main():
    parser = argparse.ArgumentParser(description=__doc__)
    parser.add_argument("--bind", default="127.0.0.1")
    parser.add_argument("--port", type=int, default=8801)
    args = parser.parse_args()
    repo = Path(__file__).resolve().parents[1]
    with tempfile.TemporaryDirectory(prefix="meow-duo-preview-") as temporary:
        export_preview(repo, Path(temporary))
        handler = functools.partial(PreviewHandler, directory=temporary)
        with ThreadingHTTPServer((args.bind, args.port), handler) as server:
            print(f"Preview: http://{args.bind}:{args.port}/", flush=True)
            try:
                server.serve_forever()
            except KeyboardInterrupt:
                pass


if __name__ == "__main__":
    main()

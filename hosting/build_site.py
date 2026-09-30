#!/usr/bin/env python3
"""Generate the landing pages of meow.jacky.jp.

The 27 cat captions come straight from the app's Localizable.strings, so the
website always matches what the app shows. Run from anywhere:

    python3 hosting/build_site.py
"""
import html
import pathlib
import re

HERE = pathlib.Path(__file__).resolve().parent
ROOT = HERE.parent
PUBLIC = HERE / "public"
APP_STORE = "https://apps.apple.com/app/id826362662"
GOOGLE_PLAY = "https://play.google.com/store/apps/details?id=jp.jacky.meow"
SITE = "https://meow.jacky.jp/"
MAIL = "newbdez33@gmail.com"
DEMO_CATS = 9

PAGES = {
    "zh-Hans": {
        "file": "zh.html",
        "href": "zh.html",
        "lproj": "zh-Hans",
        "badge": "zh",
        "privacy": "privacy/zh.html",
        "title": "猫叫模拟器 · 27 种猫叫，点一下就喵",
        "description": "猫叫模拟器是 iPhone、iPad 和 Android 上的猫叫音效应用：27 只猫，27 种心情，点一下就喵给你听。逗猫、逗娃，免费下载。",
        "nav_label": "语言",
        "h1": "猫叫模拟器",
        "tagline": "逗猫逗娃的神器",
        "lede": "27 只猫，27 种心情。点一下，它就喵给你听。逗家里的猫，哄怀里的娃，或者就是自己乐一乐。",
        "badge_alt": "在 App Store 下载",
        "play_alt": "在 Google Play 下载",
        "note": "免费下载，iPhone、iPad 和 Android 都能用。",
        "hint": "先在这里试试：点一只猫。",
        "demo_note": "这里有 9 只，App 里有全部 27 只。",
        "demo_label": "试玩",
        "facts": [
            ("每只猫一种心情", "从打呼噜到发怒，27 段猫叫各不一样。"),
            ("打开就能玩", "不用注册，不用联网，声音都在手机里。"),
            ("分享给爱猫的人", "右上角的 🐱 一键把 App 发给朋友。"),
        ],
        "gallery": "全部 27 只",
        "closing": "现在就去逗猫吧。",
        "privacy_label": "隐私政策",
        "contact_label": "联系作者",
        "legal": "Apple 和 Apple 标志是 Apple Inc. 在美国及其他国家和地区注册的商标。App Store 是 Apple Inc. 的服务标志。Google Play 和 Google Play 徽标是 Google LLC 的商标。",
    },
    "en": {
        "file": "index.html",
        "href": "./",
        "lproj": "en",
        "badge": "en",
        "privacy": "privacy/",
        "title": "Meow · 27 cat sounds, one tap away",
        "description": "Meow is a cat-sound app for iPhone, iPad and Android: 27 cats, 27 moods, each one meows when you tap it. Tease your cat or amuse the kids. Free download.",
        "nav_label": "Language",
        "h1": "Meow",
        "tagline": "Your kitty kit",
        "lede": "27 cats, 27 moods. Tap one and it meows back. Tease the cat, amuse the kid, or just make yourself laugh.",
        "badge_alt": "Download on the App Store",
        "play_alt": "Get it on Google Play",
        "note": "Free on iPhone, iPad and Android.",
        "hint": "Try it here: tap a cat.",
        "demo_note": "Nine cats here, all 27 in the app.",
        "demo_label": "Try it",
        "facts": [
            ("One mood per cat", "From snoring to hissing, no two of the 27 sounds are alike."),
            ("Nothing to set up", "No account, no connection. The sounds live on your phone."),
            ("Share it with a cat person", "The 🐱 button at the top right sends the app to a friend."),
        ],
        "gallery": "All 27 cats",
        "closing": "Go tease a cat.",
        "privacy_label": "Privacy policy",
        "contact_label": "Contact",
        "legal": "Apple and the Apple logo are trademarks of Apple Inc., registered in the U.S. and other countries. App Store is a service mark of Apple Inc. Google Play and the Google Play logo are trademarks of Google LLC.",
    },
    "ja": {
        "file": "ja.html",
        "href": "ja.html",
        "lproj": "ja",
        "badge": "ja",
        "privacy": "privacy/ja.html",
        "title": "ニャー · 27種類の猫の鳴き声、タップひとつで",
        "description": "ニャーは iPhone、iPad、Android の猫の鳴き声アプリ。27匹の猫、27通りの気分、タップすると鳴き返します。猫をからかったり、子どもをあやしたり。無料。",
        "nav_label": "言語",
        "h1": "ニャー",
        "tagline": "猫と遊ぶ",
        "lede": "27匹の猫、27通りの気分。タップすると、にゃあと鳴き返します。うちの猫をからかったり、子どもをあやしたり、ひとりで笑ったり。",
        "badge_alt": "App Store からダウンロード",
        "play_alt": "Google Play で手に入れよう",
        "note": "iPhone、iPad、Android で無料。",
        "hint": "ここで試せます。猫をタップ。",
        "demo_note": "ここには9匹、アプリには27匹すべて。",
        "demo_label": "おためし",
        "facts": [
            ("猫ごとにひとつの気分", "いびきから怒り声まで、27種類の鳴き声はどれも違います。"),
            ("開いてすぐ遊べる", "登録も通信も不要。音は端末の中にあります。"),
            ("猫好きに教える", "右上の 🐱 からアプリを友だちに送れます。"),
        ],
        "gallery": "27匹ぜんぶ",
        "closing": "さあ、猫と遊ぼう。",
        "privacy_label": "プライバシーポリシー",
        "contact_label": "お問い合わせ",
        "legal": "Apple、Appleのロゴは、米国およびその他の国で登録されたApple Inc.の商標です。App StoreはApple Inc.のサービスマークです。Google Play および Google Play ロゴは Google LLC の商標です。",
    },
}

LANG_NAMES = {"en": "EN", "zh-Hans": "中文", "ja": "日本語"}

SCRIPT = """<script>
(function () {
  var players = {};
  var current = null;
  function stop() {
    if (!current) return;
    current.audio.pause();
    current.audio.currentTime = 0;
    current.button.classList.remove('is-playing');
    current = null;
  }
  document.querySelectorAll('.demo .cat').forEach(function (button) {
    button.addEventListener('click', function () {
      var src = button.getAttribute('data-sound');
      var wasPlaying = current && current.button === button;
      stop();
      if (wasPlaying) return;
      var audio = players[src];
      if (!audio) {
        audio = new Audio(src);
        audio.addEventListener('ended', stop);
        players[src] = audio;
      }
      current = { audio: audio, button: button };
      button.classList.add('is-playing');
      audio.play().catch(stop);
    });
  });
})();
</script>"""


def captions(lproj):
    text = (ROOT / "meow" / f"{lproj}.lproj" / "Localizable.strings").read_text(encoding="utf-8")
    found = dict(re.findall(r'^"(t\d\d)" = "(.*)";', text, re.M))
    return [found[f"t{i:02d}"] for i in range(1, 28)]


def e(text):
    return html.escape(text, quote=True)


def lang_nav(lang, page):
    links = []
    for code, name in LANG_NAMES.items():
        current = ' aria-current="page"' if code == lang else ""
        links.append(f'<a href="{PAGES[code]["href"]}" lang="{code}"{current}>{name}</a>')
    return f'<nav class="langs" aria-label="{e(page["nav_label"])}">{"".join(links)}</nav>'


def badge(page):
    return (f'<a class="badge" href="{APP_STORE}">'
            f'<img src="assets/badges/app-store-{page["badge"]}.svg" alt="{e(page["badge_alt"])}" width="120" height="40"></a>'
            f'<a class="badge" href="{GOOGLE_PLAY}">'
            f'<img src="assets/badges/google-play-{page["badge"]}.png" alt="{e(page["play_alt"])}" width="134" height="40"></a>')


def render(lang, page):
    names = captions(page["lproj"])
    demo = "".join(
        f'<button class="cat" type="button" data-sound="assets/sounds/m_{i + 1:03d}.mp3">'
        f'<img src="assets/cats/c{i + 1:02d}.png" alt="" width="64" height="64">'
        f'<span class="pill">{e(names[i])}</span></button>'
        for i in range(DEMO_CATS))
    gallery = "".join(
        f'<li><img src="assets/cats/c{i + 1:02d}.png" alt="" width="88" height="88" loading="lazy">'
        f'<span class="pill">{e(names[i])}</span></li>'
        for i in range(27))
    facts = "".join(f"<div><h2>{e(h)}</h2><p>{e(p)}</p></div>" for h, p in page["facts"])
    alternates = "".join(
        f'<link rel="alternate" hreflang="{code}" href="{SITE}{PAGES[code]["href"].lstrip("./")}">'
        for code in PAGES)
    return f"""<!doctype html>
<html lang="{lang}">
<head>
<meta charset="utf-8">
<meta name="viewport" content="width=device-width, initial-scale=1">
<title>{e(page["title"])}</title>
<meta name="description" content="{e(page["description"])}">
<link rel="canonical" href="{SITE}{page["href"].lstrip("./")}">
{alternates}<link rel="alternate" hreflang="x-default" href="{SITE}">
<meta property="og:title" content="{e(page["title"])}">
<meta property="og:description" content="{e(page["description"])}">
<meta property="og:image" content="{SITE}assets/icon.png">
<link rel="icon" href="assets/icon.png">
<link rel="apple-touch-icon" href="assets/icon.png">
<link rel="stylesheet" href="style.css">
</head>
<body>
<header class="top wrap">
<a class="wordmark" href="{page["href"]}"><img src="assets/icon.png" alt="" width="32" height="32">Meow</a>
{lang_nav(lang, page)}
</header>
<main class="wrap">
<section class="hero">
<div class="pitch">
<h1>{e(page["h1"])}</h1>
<p class="tagline">{e(page["tagline"])}</p>
<p class="lede">{e(page["lede"])}</p>
{badge(page)}
<p class="note">{e(page["note"])}</p>
</div>
<div class="demo">
<p class="hint">{e(page["hint"])}</p>
<div class="phone" role="group" aria-label="{e(page["demo_label"])}">
<div class="bar"><span>{e(page["h1"])}</span><span aria-hidden="true">🐱</span></div>
<div class="screen">{demo}</div>
</div>
<p class="note">{e(page["demo_note"])}</p>
</div>
</section>
<section class="facts">{facts}</section>
<section class="gallery-section">
<h2>{e(page["gallery"])}</h2>
<ul class="gallery">{gallery}</ul>
</section>
<section class="closing">
<h2>{e(page["closing"])}</h2>
{badge(page)}
</section>
</main>
<footer class="foot wrap">
<div class="foot-links"><a href="{page["privacy"]}">{e(page["privacy_label"])}</a><a href="mailto:{MAIL}">{e(page["contact_label"])}</a></div>
<p class="legal">{e(page["legal"])}</p>
<p class="legal">© 2014–2026 Jacky</p>
</footer>
{SCRIPT}
</body>
</html>
"""


def main():
    for lang, page in PAGES.items():
        (PUBLIC / page["file"]).write_text(render(lang, page), encoding="utf-8")
        print("wrote", page["file"])


if __name__ == "__main__":
    main()

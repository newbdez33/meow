# meow.jacky.jp

Static app website served by Cloudflare Workers Static Assets from `public/`.
No Worker script, no analytics, no external fonts or scripts.

- `/` English, `/zh.html` Simplified Chinese, `/ja.html` Japanese.
- `/privacy/`, `/privacy/zh.html`, `/privacy/ja.html`: privacy policy.
- `/app-ads.txt`: the AdMob publisher record (the same line as
  `https://jacky.jp/app-ads.txt`).
- `assets/cats/`, `assets/sounds/` (nine demo sounds) and `assets/icon.png`
  are copied from the app; `assets/badges/` holds Apple's App Store badges
  and Google's Play badges.
- `_redirects` rewrites `/` and `/privacy/` to their index files with HTTP 200.
  HTML handling is `none`, so every page is served only at its `.html` path
  and the local preview (`python3 -m http.server`) behaves the same way.

The three landing pages are generated from the app's `Localizable.strings`:

```sh
python3 hosting/build_site.py
```

## Deploy

Cloudflare account `newbdez33` (`69b20790d259a1817f268a2c782ec7d1`), Worker
`meow-site`, wrangler profile `menkyo-newbdez33` bound to this directory:

```sh
cd hosting
node /Volumes/shit/projects/menkyo_practice/bank-hosting/node_modules/wrangler/bin/wrangler.js auth activate menkyo-newbdez33 "$PWD"   # once per checkout
node /Volumes/shit/projects/menkyo_practice/bank-hosting/node_modules/wrangler/bin/wrangler.js deploy
```

The custom domain `meow.jacky.jp` (zone `bad0a183259c863f56b9691468c3a756`)
was attached once on 2026-09-27 through the account-level Workers Domains API
(`PUT /accounts/{account_id}/workers/domains`), because the profile's OAuth
token has no zone-level route permission and `wrangler deploy` with a
`routes` entry fails on `/zones/{zone}/workers/routes`. Later deploys keep the
domain; the config deliberately has no `routes`.

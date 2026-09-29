# TODO

Working list for bringing Meow (`com.salmonapps.Meow`, App Store id 826362662)
back to a shippable, revenue-earning state. Items are done one at a time, in
order. Each item has a short design; the owner approves it before the work
starts. Check boxes are ticked only after verification.

Audit summary (2026-09-27): the live build 2.0.0 (2020-09-17) ships Google
Mobile Ads SDK 7.65.0, which Google sunset on 2023-06-30, so ad requests no
longer fill. There is no UMP consent flow, the vendored GoogleUtilities /
nanopb / PromisesObjC frameworks carry no privacy manifests, and Xcode 27
rejects the iOS 14.0 deployment target.

## 1. AdMob migration (approved 2026-09-27)

Goal: ads fill again on a current SDK, with the consent flow Google requires.

- [x] Replace the vendored `meow/vender/GoogleMobileAdsSdkiOS-7.65.0` with the
      Swift Package `swift-package-manager-google-mobile-ads` 13.10.0 (it pulls
      `GoogleUserMessagingPlatform` 3.1.0). The six framework references are
      gone from the Xcode project.
- [x] Raise `IPHONEOS_DEPLOYMENT_TARGET` to 15.0 (Xcode 27 minimum).
- [x] Rename to the v12+ Swift API: `MobileAds.shared`, `BannerView`,
      `AdSizeBanner`, `Request`.
- [x] Add the UMP consent flow in `AdsController`: `requestConsentInfoUpdate` →
      `loadAndPresentIfRequired` → ATT prompt → `MobileAds.shared.start` →
      the banner renders only while `canRequestAds`. A hand icon in the
      navigation bar opens the privacy options form when
      `privacyOptionsRequirementStatus == .required`.
- [x] Move the ATT request out of `App.init`; it now runs after consent from
      the first screen's `.task`.
- [x] Info.plist: Google's current 50 `SKAdNetworkItems`;
      `UIRequiredDeviceCapabilities` armv7 → arm64.
- [x] Bump `MARKETING_VERSION` 2.0 → 2.1, `CURRENT_PROJECT_VERSION` 3 → 4.
- [x] Verified 2026-09-27: Debug and Release device builds pass without
      signing; on the iPhone 18 Pro simulator (iOS 27) the consent update
      returns, tracking is requested, and the 320×50 banner fills in test mode
      with a single request ("Banner loaded"). The banner requests its ad in
      `didMoveToWindow`, because SwiftUI's first transient instance produced
      an "Invalid ad width or height" request when loading in `makeUIView`.
- [x] AdMob console, 2026-09-27: "Meow - European regulations" published
      under Privacy & messaging for app ~6286907117 with privacy policy URL
      https://meow.jacky.jp/privacy/, English plus Chinese (zh-CN) and
      Japanese, Consent / Manage options / Do not consent all on. AdMob says
      the message can take up to an hour to reach the app.

## 2. App website at `https://meow.jacky.jp/` (approved and deployed 2026-09-27)

Goal: a developer website AdMob and the App Store can point to, serving
`app-ads.txt` and a privacy policy.

Done 2026-09-27: `hosting/` holds the site (English at `/`, `/zh.html`,
`/ja.html`, privacy policy under `/privacy/`, `/app-ads.txt`, a playable
nine-cat demo). Deployed as Worker `meow-site` with the custom domain
attached through the Workers Domains API; see `hosting/README.md`. Live
checks: `/`, `/privacy/`, `/app-ads.txt` return 200 with the publisher line.

Facts that shape the design:

- AdMob reads the developer website from the App Store "marketing URL". Its
  crawler tries `meow.jacky.jp/app-ads.txt` first, then falls back one level
  to `jacky.jp/app-ads.txt`, which already serves the same publisher line
  (`google.com, pub-1295607594822275, DIRECT, f08c47fec0942fa0`, live since
  2026-09-26). Meow uses that same publisher, so the file just needs to be
  mirrored on the subdomain.
- `menkyo.jacky.jp` is a static site in `menkyo_practice/hosting/` served by
  Cloudflare Workers Static Assets with a custom domain route (account
  `69b20790d259a1817f268a2c782ec7d1`, zone `bad0a183…` for `jacky.jp`,
  wrangler profile `menkyo-newbdez33`). `meow.jacky.jp` has no DNS record
  yet; a custom-domain route creates it.

Design:

- `hosting/public/`: `index.html` (zh-Hans, the store's primary language),
  `en.html`, `ja.html`, `privacy/index.html` + `en.html` + `ja.html`,
  `app-ads.txt`, `style.css`, app icon and the cat artwork from
  `Assets.xcassets`. No JavaScript, no analytics, no external fonts. Store
  badge links to `https://apps.apple.com/app/id826362662`.
- `hosting/wrangler.json`: Worker `meow-site`, `workers_dev: false`, assets
  directory `public`, default HTML handling so `/` and `/privacy/` resolve,
  route `meow.jacky.jp` with `custom_domain: true`.
- Deploy: `npx wrangler deploy` from `hosting/` after binding the
  `menkyo-newbdez33` profile to that directory. Deploying touches the owner's
  Cloudflare account, so it runs only on the owner's go.
- Privacy policy content: sounds and taps stay on device; Google AdMob banner
  ads with the UMP consent choices and the iOS tracking prompt; the optional
  ad-removal purchase handled by the App Store; contact `newbdez33@gmail.com`.
- Verify: local preview over Tailscale (`~/showme`), then `curl` the live
  `/`, `/privacy/`, `/app-ads.txt` for HTTP 200 and the exact publisher line.
- Owner, App Store Connect: set marketing URL `https://meow.jacky.jp/` and
  privacy policy URL `https://meow.jacky.jp/privacy/` on the 2.1 version; then
  AdMob "Check for updates" for app-ads.txt (up to 24 h).

## 3. Treats that remove the ads (built 2026-09-27)

Goal: one-time purchases that hide the banner forever on the buyer's
Apple ID, with a cheaper and a dearer option.

Done 2026-09-27:

- StoreKit 2 `Store` with two non-consumable products, either of which
  removes the ads: `com.salmonapps.Meow.coffee` (a coffee for the author,
  planned $2.99) and `com.salmonapps.Meow.can` (a can for the author's cat,
  planned $5.99, shown first as the upsell). Entitlement comes from
  `Transaction.currentEntitlements` at every launch; no server, no
  UserDefaults.
- `TipSheet` behind the mug icon in the navigation bar: a hero composed at
  runtime from `tipMug`, one of five app cats (black c02, gray c01, orange
  c04, tabby c19, orange-with-bowl c21; a different one each time the sheet
  opens, tap to switch) and `tipCan` (vector sources in
  `design/tip-cat.html`), title, one paragraph, the can button (with
  `tipCanIcon` and the "cat's pick" note), the coffee button (with `tipMug`),
  restore. Half-height sheet on iOS 16+. Strings `tip_*` in en / zh-Hans / ja.
- The navigation bar's cup and cat buttons use `navCup` and `navCat`, made
  with the Codex CLI image generation tool; originals and prompt are in
  `design/imagegen/`.
- The banner and the consent/ATT flow skip when `isAdFree`; the cup icon
  disappears after a purchase.
- `meowTests/StoreTests` (StoreKitTest against `meow.storekit`): fresh install
  offers both treats, coffee removes ads, can removes ads, an earlier can
  purchase is honoured on relaunch, restore finds a coffee bought elsewhere.
  Five tests pass on the iOS 27 simulator. Debug simulator and Release device
  builds pass.
- Until the products exist in App Store Connect the sheet shows "Can't reach
  the App Store".

App Store Connect, 2026-09-27: both non-consumable products exist in
"Prepare for Submission": `com.salmonapps.Meow.can` (Apple ID 6816646536,
$5.99 base, all 175 countries) and `com.salmonapps.Meow.coffee` (Apple ID
6816647301, $2.99 base, all countries), each with English (U.S.), Chinese
(Simplified) and Japanese display names. Still needed before "Add for
Review": a review screenshot per product (take it from the 2.1 build's tip
sheet), then attach both to the 2.1 version submission.

## 4. Release

Rule from the owner (2026-09-27): every build goes to TestFlight first; the
App Store review submission is created only after the owner has tried the
TestFlight build and said it is fine.

- [x] README points to this file (done with item 1).
- [x] 2026-09-27: `ITSAppUsesNonExemptEncryption = NO` in Info.plist; the
      remove-ads sheet is now presented from the content view rather than the
      toolbar button (more reliable); `meowUITests/TipSheetScreenshotTests`
      opens the sheet against `meow.storekit` and, with
      `TEST_RUNNER_MEOW_SCREENSHOT_DIR` set, writes the review screenshot.
- [x] 2026-09-27: Release archive signed through the team API key
      (`xcodebuild archive -allowProvisioningUpdates` with
      `AuthKey_9LULAK77BN`), exported with `destination: upload` and uploaded
      as 2.1 (4). The symbol upload warns about missing dSYMs for Google's
      binary frameworks; that is expected and harmless.
- [x] 2026-09-27, App Store Connect through the API: version 2.1 created
      (`e5573e0d-7dec-4b20-8a7f-b85ebadb556f`, release after approval);
      en-US and zh-Hans marketing/support URLs point at meow.jacky.jp, What's
      New written in both; privacy policy URLs on the 2.1 app info point at
      `/privacy/` and `/privacy/zh.html`; both purchases have the tip-sheet
      review screenshot and are READY_TO_SUBMIT (the coffee's availability had
      to be set again through the API, the web form had not saved it).
- [x] 2026-09-27: App Privacy labels published (through the web form):
      Coarse Location (third-party advertising, not linked, no tracking);
      Device ID (third-party advertising + analytics, linked, tracking);
      Product Interaction (third-party advertising + analytics, not linked, no
      tracking); Advertising Data (third-party advertising, linked, tracking);
      Crash / Performance / Other Diagnostic Data (app functionality, not
      linked, no tracking). Age rating on the 2.1 app info gained the eight
      2025 answers through the API (`advertising: true`, everything else
      none/false).
- [x] 2026-09-27 20:05 JST: build 4 attached to 2.1 through the API; review
      submission `e2e10b1e-601b-4177-bc0d-916fe1742856` holds the version
      (added through the API) and both purchases (added with "Add for
      Review" on each purchase page, since `reviewSubmissionItems` has no
      purchase relationship); submitted through the API. Version and both
      purchases are WAITING_FOR_REVIEW, release type after approval.
- [x] 2026-09-27 20:15 JST, TestFlight: build 4 is in the new internal group
      "Internal" (`4ae4991f-19dc-4f75-b695-c9d0390eba4b`) with the owner's
      testers newbdez33jp@gmail.com and newbdez33@gmail.com (created with
      `POST /v1/betaTesters` inside the group; the existing tester records
      from other apps cannot be reused, and the legacy "App Store Connect
      Users" group rejects tester assignment). "What to Test" notes in en-US
      and zh-Hans (`betaBuildLocalizations.whatsNew`). No beta review needed
      for internal testing.
- [x] 2026-09-27 evening, after the owner tried build 4: the review
      submission was cancelled (build 4 had the toolbar the owner rejected;
      the version is back in an editable state). Toolbar buttons are separate
      glass circles on iOS 26 (`ToolbarSpacer`) and the remove-ads button is
      the can (`navCan`; `navCup` removed). What's New no longer mentions the
      ads library or the consent screen; descriptions rewritten in en-US and
      zh-Hans; a Japanese store listing added (name 「ニャー：猫の鳴き声」,
      subtitle, description, keywords, What's New, URLs to `/ja.html` and
      `/privacy/ja.html`). New screenshots from `meowUITests/StoreScreenshotTests`
      (launch argument `-MeowNoAds`, DEBUG only): iPhone 6.9" 1320x2868 and iPad
      13" 2064x2752, three per language (grid, a selected cat, the remove-ads
      sheet); the 2020 screenshots were deleted. Build 5 uploaded and put in the
      Internal TestFlight group with notes.
- [x] 2026-09-27: store screenshots composed the menkyo way and approved by
      the owner: `tool/render_store_screenshots.swift` + `design/store-copy.json`
      put each capture in a rounded frame under the app name, a title and a
      caption on blush / butter / coral backgrounds (system rounded, 圆体-简,
      Hiragino Maru Gothic). Regenerate with
      `swift tool/render_store_screenshots.swift design/store-copy.json <captures> <out>`
      after re-running `StoreScreenshotTests`; the composed PNGs replaced the
      raw captures on version 2.1 (three per language for iPhone 6.9" and
      iPad 13").
- [x] 2026-09-27 20:49 JST: the owner OK'd 2.1 (5) in TestFlight. Build 5
      attached to 2.1; review submission `642a51bc-e85a-4cd9-85fb-0f1148a04360`
      holds the version and both purchases and is WAITING_FOR_REVIEW. On the
      owner's request the purchases also got their 1024x1024 images
      (`design/iap-icons/`, the generated can and mug on the blush
      background, uploaded through `inAppPurchaseImages`); that needed the
      first submission of the evening cancelled and redone, because images
      cannot be added while a purchase is pending review.
- [ ] After approval: AdMob app-ads.txt "Check for updates" (the marketing
      URL only reaches the store with 2.1), ads filling on a real device, the
      EEA message showing in the EU, both purchases visible in the sheet.
- [ ] Optional: lower the AdMob payout threshold from $1,500 so the
      $1,463.81 balance pays out.

## 5. Android version (in progress, started 2026-09-27)

Goal: the same app on Google Play as `jp.jacky.meow`. Design in
`docs/specs/2026-09-27-android-design.md`, tasks in
`docs/plans/2026-09-27-android.md`, sources in `android/`.

- [x] 2026-09-27: Play Console app 4976190229557343886 created (zh-CN default,
      free, automatic protection off) with every App content declaration
      done; AdMob Android app `~8527399220` with banner unit `/4197186257`,
      added to the shared EEA message; upload key generated in
      `~/.android/meow-upload/` (see `android/README.md` for the ids and
      fingerprints).
- [x] 2026-09-27: 1.0 (1) release bundle built (`app-release.aab`, SHA-256
      `f5cee09beb7cb1e190d59338b9cb4db9de2ea43babc1cc748ed09373600b295d`), bundletool-validated, the
      one native library is 16 KB aligned, and the minified build cold-starts
      on the API 36 emulator after a Room keep rule for WorkManager.
- [x] 2026-09-28: privacy pages redeployed with the Android paragraphs
      (Worker version 74e08903); 1.0 (1) published to the Internal testing
      track at 00:08 (tester list "Menkyo internal testers" =
      newbdez33@gmail.com, join link
      https://play.google.com/apps/internaltest/4701236019639669858); one-time
      products `jp.jacky.meow.can` (USD 5.99) and `jp.jacky.meow.coffee`
      (USD 2.99) active in all 173 countries with zh/en/ja names; the owner's
      account was already a license tester (RESPOND_NORMALLY).
- [x] 2026-09-28: device acceptance on the owner's Galaxy S22 Ultra (Android
      16, driven over adb through the Windows box `jx`), evidence in
      `android/build/acceptance-1/` (ignored). Build 1: cold start, banner
      ("Test Ad" 4 s after launch), three cats play, sheet prices ¥940/¥470,
      can purchase with the always-approves test card removes the ads and the
      can, still gone after a force-stop, share sheet with the Play link,
      ja-JP/zh-CN per-app locales, light theme — all pass. The owner spotted
      that the banner sat under the grid instead of at its top as on iOS, so
      build 1.0 (2) moved it (commit 1d46ea9) and went to the internal track
      at 16:14. Build 2: after refunding the can order (entitlement removed)
      the banner is back as the grid's first row and does not reload when
      scrolled away; the coffee purchase then removes the ads for good and
      stays in place. Two findings, neither a code fault: the owner's home
      Wi-Fi DNS blocks Google's consent and ad hosts (the app correctly shows
      no ads then; the phone used dns.google for the test), and a per-app
      locale change needs a cold start to show (Settings offers none anyway
      because the app declares no `localeConfig`).
- [x] 2026-09-28 16:45 JST: after the owner's go, build 1.0 (2) was put on the
      Production track (all 176 countries / regions plus "rest of world") and
      the 13 pending changes (release, countries, three store listings, the
      app-content declarations, the category) were sent for review from the
      Publishing overview; Google says reviews usually finish within 7 days.
- [x] 2026-09-28: whole-branch review (fresh reviewer): 0 critical, 2
      important, 9 minor. Fixed: `Store.purchase` now re-reads the
      entitlement when the Play sheet does not open (`launchBillingFlow`
      answers ITEM_ALREADY_OWNED for a purchase made on another device), so
      the tap ends ad-free instead of "didn't go through" — not in build 2,
      ships with the next build (bump `versionCode` to 3); and the privacy
      policy's scope sentence now names Android (deployed). The nine minors
      are listed in the plan ledger for the owner to pick from.
- [x] 2026-09-29: live on Google Play — Google approved build 1.0 (2) within
      a day; https://play.google.com/store/apps/details?id=jp.jacky.meow
      answers 200 in en, ja and zh-CN with the listing text. AdMob: the Play
      listing is linked to the shipped app `~8527399220` and verified
      (ad-serving review "typically 2-3 days"; ads are limited until then).
      The owner had confirmed the auto-detected app as a *new* AdMob app,
      which made a second Android "Meow simulator" (`~1072875607`, no ad
      units) that held the store link; its store details were cleared so the
      real app could take the link. `app-ads.txt` on meow.jacky.jp already
      carries the publisher line.
- [ ] Owner, AdMob: confirm the ad-serving review finished and app-ads.txt
      shows verified for the Android app after the crawler runs; remove the
      empty duplicate app `~1072875607` if it bothers you (it serves nothing).
- [ ] Next Android build: bump `versionCode` to 3 (carries the `Store.purchase`
      fix) and consider the review's nine minors:
      1. `MediaPlayerSoundPlayer` exposes no `isPlaying`, so the fast-tap test
         proves only "never throws"; audible playback was checked by hand.
      2. `android/tool/import_strings.py` does not escape a leading `@`/`?`
         or quote leading/trailing whitespace (no current string affected).
      3. `TipSheet`: two simultaneous taps on the two buttons make the second
         purchase fail with "didn't go through" while the first Play sheet is
         open; a one-line `isPurchasing` guard in `buy`.
      4. Status-bar icons are dark on the coral bar in the light theme;
         `SystemBarStyle.dark(Color.TRANSPARENT)` would match iOS.
      5. With ads off the banner grid item is a zero-height row (4 dp top gap
         against 2 dp at the sides).
      6. `rememberBannerAdView` creates the `AdView` and calls `loadAd`
         inside `remember`; an abandoned composition would leak one request.
      7. A per-app locale change re-renders only after a cold start
         (unreachable without `localeConfig`).
      8. Doc drift: spec still says "Version 1.0 (1)", `CatCell` uses 11 sp
         where the spec says 12 sp, the reconnection wording predates
         `enableAutoServiceReconnection()`.
      9. (settled with the badge trim) the Play badge's width/height hint.
- [x] 2026-09-28 17:35 JST: the Google Play badges went live on
      https://meow.jacky.jp/ (en, zh, ja; hero and footer) on the owner's
      instruction, ahead of the store page; the badge links to
      https://play.google.com/store/apps/details?id=jp.jacky.meow, which
      answers 404 until Google publishes the app.

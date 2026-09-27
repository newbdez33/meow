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
- [ ] After approval: AdMob app-ads.txt "Check for updates" (the marketing
      URL only reaches the store with 2.1), ads filling on a real device, the
      EEA message showing in the EU, both purchases visible in the sheet.
- [ ] Optional: lower the AdMob payout threshold from $1,500 so the
      $1,463.81 balance pays out.

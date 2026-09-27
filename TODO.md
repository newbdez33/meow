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
- [ ] Owner, AdMob console: publish a GDPR (EEA/UK) privacy message for the
      Meow app under Privacy & messaging. The simulator run logged
      "no form(s) configured for the input app ID
      ca-app-pub-1295607594822275~6286907117"; non-EEA users still get ads,
      EEA/UK users get none until the message exists. The message needs the
      privacy policy URL from item 2, so this waits for item 2.

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

## 3. "Buy the author a coffee" removes ads (design, awaiting approval)

Goal: a one-time purchase that hides the banner forever on the buyer's
Apple ID.

Design:

- StoreKit 2, one non-consumable product `com.salmonapps.Meow.coffee`.
- `Store` observable: loads the product, `purchase()`, `restore()`
  (`AppStore.sync()`), listens to `Transaction.updates`, and derives
  `isAdFree` from `Transaction.currentEntitlements` at every launch. No
  server, no receipt storage, no UserDefaults.
- UI: a ☕ button beside the existing 🐱 share button opens a sheet: title,
  one-line thanks, the localized price, Buy, Restore purchases. After a
  purchase the banner disappears and the consent/ATT flow is skipped on later
  launches. Strings in en / zh-Hans / ja.
- Testing: a `meow.storekit` configuration for the simulator, plus a small
  `meowTests` target using StoreKitTest that covers purchase → `isAdFree`,
  restore, and the fresh-launch entitlement read.
- Owner, App Store Connect: create the non-consumable product (name, price
  tier, review screenshot, localizations) and attach it to the 2.1 version.
  This can be done through the App Store Connect API with the existing team
  key if preferred.

## 4. Release

- [ ] README: replace "Fix Admob" with a pointer to this file.
- [ ] Archive, upload 2.1 (4), fill App Store Connect (URLs above, IAP,
      what's new), submit.
- [ ] After approval: AdMob app-ads.txt status verified, ads filling on a
      real device, EEA message live.

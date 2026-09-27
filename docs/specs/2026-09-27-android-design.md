# Meow for Android — design

Approved in conversation on 2026-09-27. This is the design for the Android
version of Meow (猫叫模拟器), a port of the iOS 2.1 app in `meow/` to Google
Play.

## Goal

Ship the same app on Google Play: 27 cats in a grid, each meows when tapped,
a banner ad with Google's consent flow, and two one-time treats that remove
the ads for good. The owner wants to do as little by hand as possible: the
agent builds, sets up the store and ad accounts, and uploads; the owner looks
at the redrawn cats, signs in when Google asks and gives a one-line go before
anything public happens.

Success: the app is live on Google Play under `jp.jacky.meow`, the banner
fills on a real device, either treat removes the ads, and the three
localizations match the iOS app.

Not in scope: Android TV / Wear, a designed dark theme (Material's default
dark background is enough), networking of any kind, Firebase, Crashlytics,
analytics, Chinese app stores.

## Facts that shape the design

- The Google Play developer account is the personal account
  `newbdez33@gmail.com` (Salmonapps, developer id `4896965748454075126`),
  the one that publishes `jp.jacky.menkyo`. It predates the closed-testing
  requirement, so a first release can go straight to production, and its
  merchant profile is already set up (Menkyo sells in-app products).
- The AdMob publisher is `pub-1295607594822275`, shared with the iOS app and
  both Menkyo apps. `https://meow.jacky.jp/app-ads.txt` already serves its
  record, and the privacy policy lives at `https://meow.jacky.jp/privacy/`.
- A Google Play listing of 猫叫模拟器 with the same 27 sound names existed
  in 2014 as `com.cat.simulation` by "Pianyiwan Studio" and is gone; the
  owner has no claim on that package, so the new app takes a new package.
- The iOS art is small: the 27 cats exist only as 128×128 PNGs. The toolbar
  and tip-sheet art has 1254×1254 originals in `design/imagegen/`, made with
  the Codex CLI image generation tool; the owner approved redrawing the cats
  the same way.
- The Mac already has the Android SDK (platforms 34–36, build-tools 36.0.0,
  an API 36 emulator image and two AVDs), JDK 21 from Homebrew
  (`/opt/homebrew/opt/openjdk@21`, the JDK Menkyo's Flutter builds use) and
  `adb`, `sdkmanager` and `bundletool` on the path. No Android Studio.
- Google Play requires new apps to target API 36 from 2026-08-31, Play
  Billing Library 8 or later from the same date, and 16 KB page-size
  support for native code (this app has none).

## Project layout and build

Native Kotlin with Jetpack Compose, in a new top-level `android/` directory
next to `meow/` (iOS) and `hosting/` (website). One Gradle module, `app`.

```
android/
  settings.gradle.kts  build.gradle.kts  gradle.properties  gradlew  gradle/
  key.properties                     ignored; release signing values
  local.properties                   ignored; sdk.dir
  tool/import_strings.py             Localizable.strings → strings.xml
  app/
    build.gradle.kts  proguard-rules.pro
    src/main/AndroidManifest.xml
    src/main/kotlin/jp/jacky/meow/
      MeowApplication.kt             owns the AdsController and Store singletons
      MainActivity.kt                edge-to-edge, sets MeowApp()
      MeowApp.kt                     top bar, grid, banner slot
      CatCell.kt                     one cat with its caption
      Cats.kt                        the 27 (drawable, string, raw) triples
      SoundPlayer.kt                 interface + MediaPlayer implementation
      Share.kt                       system share sheet
      ui/Theme.kt                    colors
      ads/AdsController.kt           consent + SDK start state machine
      ads/ConsentGateway.kt          interface over UMP and MobileAds
      ads/Banner.kt                  AdView inside AndroidView
      billing/Store.kt               entitlement, products, purchase, restore
      billing/BillingGateway.kt      interface over BillingClient
      billing/PurchaseVerifier.kt    RSA-SHA1 check against the Play key
      billing/TipSheet.kt            the remove-ads bottom sheet
    src/main/res/raw/m_001.mp3 … m_027.mp3
    src/main/res/drawable-nodpi/c01.png … c27.png, nav_cat.png, nav_can.png,
                                tip_can.png, tip_mug.png
    src/main/res/mipmap-anydpi-v26/ic_launcher.xml (+ foreground/background)
    src/main/res/values/strings.xml, values-zh-rCN/, values-ja/   generated
    src/test/kotlin/…                JVM tests
    src/androidTest/kotlin/…         Compose UI tests
```

- `applicationId` and namespace `jp.jacky.meow`. `versionName "1.0"`,
  `versionCode 1`, independent of the iOS build numbers.
- Toolchain matches Menkyo: Android Gradle Plugin 9.1.0, Kotlin 2.4.0 with
  the Compose compiler plugin, JDK 21, `compileSdk`/`targetSdk` 36,
  `minSdk` 24. Compose BOM, Material 3, `activity-compose`,
  `lifecycle-runtime-compose 2.10.0` from Compose BOM 2026.06.01 (Material 3
  1.4.0, UI 1.11.4; the 2026.08+ BOMs and lifecycle 2.11 compile against
  API 37, which needs AGP 9.2 and a platform this SDK does not have);
  `play-services-ads 25.5.0` (2026-09-17, minimum API 24);
  `user-messaging-platform 4.0.0` (2025-10-31); `billing-ktx 9.1.0`
  (requires target 35 or later). Verified building on 2026-09-27.
- Build types: `debug` uses Google's sample AdMob app id
  `ca-app-pub-3940256099942544~3347511713` and sample banner unit
  `ca-app-pub-3940256099942544/6300978111`, and an empty Play licensing key.
  `release` carries the real AdMob ids and the app's Play licensing public
  key as `manifestPlaceholders` / `buildConfigField`s in `app/build.gradle.kts`;
  none of these are secrets (the iOS ids are committed the same way).
  Release builds are minified with R8 and the default Compose/ads/billing
  keep rules.
- Signing follows Menkyo: an upload keystore in `~/.android/meow-upload/`
  (alias `meow-upload`, RSA 4096, directory 0700, files 0600, never printed),
  read through ignored `android/key.properties`; the release build fails
  fast if any value or the keystore is missing. Play App Signing holds the
  app signing key.
- `.gitignore` gains `android/key.properties`, `android/local.properties`,
  `android/.gradle/`, `android/build/`, `android/app/build/`, `*.iml`,
  `.idea/`.

## UI

One activity, one screen, mirroring `ContentView.swift`:

- **Top bar**: Material 3 `TopAppBar`, container `#E76A66`, white title
  (`app_name`: "Meow" / "猫叫模拟器" / "ニャー"). Actions, right to
  left as on iOS: the cat (`nav_cat`) opens the system share sheet; the
  raised hand (Material icon) appears only while
  `isPrivacyOptionsRequired`; the can (`nav_can`, the button iOS 2.1 build 5
  settled on after the owner rejected the cup) opens the tip sheet and is
  hidden once `isAdFree`. Each action has a content description from the
  existing strings.
- **Grid**: `LazyVerticalGrid(GridCells.Adaptive(110.dp))`, 2 dp spacing,
  square cells (`aspectRatio(1f)`), so phones get three columns and tablets
  more, like `GridStack(minCellWidth: 110)`. The selected index is
  `rememberSaveable`, starts at −1, and tapping a cell plays its sound and
  selects it.
- **CatCell**: the cat image above a caption pill (12 sp, text `#E76A66`,
  background `lightPink`, 10 dp corners), the whole cell on a 30 dp rounded
  `lightYellow` background while selected and transparent otherwise, 2 dp
  outer padding. This is `Cat.swift` line for line.
- **Colors** (`ui/Theme.kt`) from the iOS color sets: coral `#E76A66`;
  `lightPink` `#FFF6F7` light / `#2C2323` dark; `lightYellow` `#FFFFE0`
  light / `#FFFFE0` at 19 % alpha dark. Background is Material's default
  surface for the light or dark system theme; nothing else changes in dark
  mode.
- **Layout**: `Scaffold` handles the status and navigation bar insets
  (edge-to-edge is mandatory at target 35+). Content is a column: the grid
  takes the remaining height, the banner slot sits below it, above the
  navigation bar inset.
- **Share** (`Share.kt`): `ACTION_SEND`, `text/plain`,
  `"<app_name> - <subtitle> https://play.google.com/store/apps/details?id=jp.jacky.meow"`,
  through `Intent.createChooser`.

## Audio

`SoundPlayer` is an interface with `play(index: Int)` and `release()`.
`MediaPlayerSoundPlayer` holds one `MediaPlayer`: each tap calls `reset()`,
sets `AudioAttributes(USAGE_MEDIA, CONTENT_TYPE_SONIFICATION)`, sets the
data source to `R.raw.m_NNN` through `openRawResourceFd`, then
`prepareAsync()` and `start()` in the prepared callback. Starting a new
sound stops the previous one, as `Sounds.playSounds` does. The app never
requests audio focus, so other apps' music keeps playing (the iOS
`.mixWithOthers` behaviour). No preloading and no `SoundPool`: the 27 files
are 14 MB of MP3 and decoding them all into memory buys nothing. The player
is released in `onDestroy`.

## Ads and consent

A translation of `AdsController.swift` and `Banner.swift` without the ATT
step, which has no Android equivalent.

- **Manifest**: `com.google.android.gms.ads.APPLICATION_ID` from the
  `admobAppId` placeholder. No `AD_ID` permission beyond what the SDK merges
  in.
- **AdsController** exposes `canRequestAds: StateFlow<Boolean>` and
  `isPrivacyOptionsRequired: StateFlow<Boolean>`, and two suspending
  functions:
  - `start(activity)`: ignored while a start is running. Calls
    `requestConsentInfoUpdate`, then `loadAndShowConsentFormIfRequired`; a
    failure in either keeps the consent state from the previous launch,
    which may still allow ads. Then reads
    `privacyOptionsRequirementStatus == REQUIRED` and, if `canRequestAds`,
    initializes the Mobile Ads SDK once, on `Dispatchers.IO`. Finally
    publishes `canRequestAds`.
  - `presentPrivacyOptions(activity)`: shows the privacy options form, then
    initializes the SDK if consent now allows it and republishes
    `canRequestAds`.
  UMP's callbacks are wrapped with `suspendCancellableCoroutine`. The
  controller talks to a `ConsentGateway` interface (request update, show
  form if required, show privacy options, `canRequestAds`,
  `privacyOptionsRequirementStatus`, `initializeSdk`) whose production
  implementation wraps `UserMessagingPlatform` and `MobileAds`; tests use a
  fake.
- **Where it runs**: `MainActivity` launches, once per process, `store.load()`
  and then `ads.start(activity)` only if the store did not report ad-free —
  the iOS `.task` in `ContentView`.
- **Banner**: a composable that renders nothing unless
  `canRequestAds && !isAdFree`; otherwise a centered 320×50 dp `AdView`
  (`AdSize.BANNER`, the iOS size) inside `AndroidView`. The `AdView` is
  created and `loadAd` is called once in the `factory`, `resume`/`pause`
  follow the lifecycle, and `destroy` runs in `DisposableEffect`. Load and
  failure callbacks log one line each.
- **Console setup** (release section): a new AdMob Android app "猫叫模拟器"
  with one banner unit under the shared publisher, added to the existing
  EEA/UK privacy message's app list so European users see the consent form.

## Purchases

A translation of `Store.swift` and `TipSheet.swift` onto Play Billing.

- **Products**, one-time non-consumable in-app products created in Play
  Console: `jp.jacky.meow.can` (a can for the author's cat, USD 5.99 base
  price, shown first) and `jp.jacky.meow.coffee` (a coffee for the author,
  USD 2.99). Google converts to local prices. Owning either removes the
  ads for good on that Google account.
- **Store** exposes `isAdFree`, `can`, `coffee` (each a `Treat` with id and
  display price, `null` until Play answers), and `isAvailable` (false while
  neither product loaded, which shows the "can't reach Google Play" text).
  - `load()`: connect, `queryProductDetailsAsync` for both ids, then
    `refreshEntitlement()`.
  - `refreshEntitlement()`: `queryPurchasesAsync(INAPP)`; a purchase counts
    when its product is one of the two, its state is `PURCHASED` and its
    signature verifies. Any counted purchase that is not yet acknowledged is
    acknowledged (Google refunds unacknowledged purchases after three
    days). `isAdFree` is true when at least one purchase counts. Nothing is
    written to disk: Play's own cache answers when offline, as StoreKit's
    `currentEntitlements` does on iOS.
  - `purchase(treat, activity)`: `launchBillingFlow`; results arrive on the
    `PurchasesUpdatedListener` and go through the same counting rule.
    `USER_CANCELED` is not an error; `PENDING` purchases are not counted
    until Play reports them purchased; any other failure surfaces as the
    sheet's "didn't go through" line.
  - `restore()`: `refreshEntitlement()` again. Play has no separate
    restore call, so the button keeps its iOS label and just re-queries.
  - Re-query on `load()` and on every return to the foreground
    (`ON_RESUME`), so a refund or a purchase on another device shows up.
  - The store talks to a `BillingGateway` interface (connect, query
    products, query purchases, launch flow, acknowledge, purchase updates as
    a flow) whose production implementation wraps `BillingClient` with
    `enableOneTimeProducts()` and reconnects once on `SERVICE_DISCONNECTED`.
    Tests use a fake.
- **Verification** (`PurchaseVerifier`): device-side, the approach the owner
  chose for Menkyo and confirmed here. `verify(originalJson, signature)`
  decodes the app's Base64 X.509 licensing public key from
  `BuildConfig.PLAY_LICENSE_KEY` and checks `SHA1withRSA`. An empty key
  (debug builds) verifies nothing, so debug builds are never ad-free —
  purchases are exercised only through Play-installed internal-test builds.
- **TipSheet**: Material 3 `ModalBottomSheet` behind the can. Content, top
  to bottom, as on iOS: a hero composed of `tip_mug`, one of five cats
  (c02, c01, c04, c19, c21; a different one each time the sheet opens, tap
  to cycle) and `tip_can`; `tip_title`; `tip_body`; then either
  `tip_thanks` in coral when `isAdFree`, or the can button (filled coral,
  with the `tip_can_note` line), the coffee button (outlined coral),
  `tip_unavailable` when neither product loaded, and `tip_restore`.
  Buttons disable while a purchase is in flight; a failure shows
  `tip_failed` in red.
- **Strings**: `tip_body` gets an Android wording in all three languages
  ("Google account" instead of "Apple ID"; "Google Play" instead of
  "App Store" in `tip_unavailable`).

## Localization and assets

- `android/tool/import_strings.py` reads the three `Localizable.strings`
  files and writes `values/strings.xml` (English), `values-zh-rCN/` and
  `values-ja/`. It maps `%@` to `%1$s`, escapes apostrophes and XML, adds
  `app_name` from `title`, and applies a small override table for the
  Android-specific `tip_body` and `tip_unavailable` wording. Re-running it
  after an iOS copy change keeps both apps in step. The `t01`–`t27` captions
  come across unchanged.
- **Sounds**: `meow/resources/m_001.mp3` … `m_027.mp3` copied to `res/raw/`
  unchanged (the names are valid resource names).
- **Cats**: 27 redraws with the Codex CLI image generation tool, following
  `design/imagegen/README.md`: one call per cat with the existing 128 px
  `cNN@2x.png` as the reference and the prompt style of
  `nav-icons-prompt.txt` (thick dark outline, flat pastel fills, transparent
  background, same pose and coloring as the reference so the caption still
  fits). Originals stay in `design/imagegen/cats/`; `sips` exports 512×512
  PNGs to `drawable-nodpi`, one file per cat for every density. The owner
  reviews the contact sheet; a cat the owner rejects is regenerated, and
  the fallback for any that cannot be matched is the existing 128 px file
  upscaled to 512 (soft, but the same cat).
- **Toolbar and tip art**: `nav_cat` (from `nav-cat.png`) and `nav_can` (from
  `tip-can.png`) exported at 128 px from the 1254 px originals in
  `design/imagegen/` (drawn at 26 dp);
  `tip_can` and `tip_mug` at 512 px (the sheet's hero).
- **App icon**: the iOS icon is a flat white cat on plain green
  (`meow/Assets.xcassets/AppIcon.appiconset/ItunesArtwork@2x.png`, 1024 px).
  The adaptive icon uses that green as the background layer and the cat,
  keyed out of the flat green with a script, as the foreground layer scaled
  into the 66 dp safe zone; a monochrome layer is not made. The 512 px
  `meow/appstore/android/playstore-icon.png` is the Play Store icon.
- **Store graphics**: a 1024×500 feature graphic from the image tool (cats
  on the app's coral), and phone and tablet screenshots from the API 36
  emulators in each language, composed the way the owner approved for the
  App Store on 2026-09-27: `tool/render_store_screenshots.swift` gains two
  Android canvases (1242×2208 and 1600×2560, because Google Play refuses a
  screenshot longer than twice its width) and a device-list argument, with
  the iPhone/iPad output unchanged; the captions come from
  `design/store/play/copy.json`, the iOS captions with the one line that
  mentions children reworded. The captures come from a debug-only
  screenshot mode, the Android form of the iOS `-MeowNoAds` launch argument
  and `meow.storekit`: `adb shell am start … --ez meowNoAds true --es
  meowStore preview` skips the consent flow and the banner and swaps in a
  `PreviewBillingGateway` that shows both prices; release builds ignore
  both extras.

## Testing

- **JVM unit tests** (no Android framework, fakes for the gateways):
  - `Store`: fresh install offers both treats; a verified coffee purchase
    removes ads; a verified can purchase removes ads; an earlier purchase
    is honoured on relaunch; restore finds a purchase made elsewhere; a
    pending purchase, a purchase with a bad signature and a purchase of an
    unknown product do not remove ads; an unacknowledged purchase gets
    acknowledged exactly once. These mirror `meowTests/StoreTests`.
  - `AdsController`: consent granted starts the SDK once and publishes
    `canRequestAds`; a failed update keeps the previous state; a required
    privacy option shows the hand; a second `start` while one runs is
    ignored; the privacy options form can turn ads on.
  - `PurchaseVerifier`: a signature made with a test key pair verifies, a
    tampered payload and an empty key do not.
  - `import_strings.py`: a doctest-style check that the three generated
    files contain 27 captions and the overrides.
- **Compose UI test** on the API 36 emulator: the grid shows 27 cells; a
  tap highlights that cell, clears the previous one and calls the fake
  `SoundPlayer` with the right index; the can is absent when ad-free.
- **Build checks**: `assembleDebug`, `testDebugUnitTest`, `bundleRelease`,
  `bundletool validate` and a debug-signed universal APK from the exact
  AAB installed and cold-started on the wiped emulator, as Menkyo does.
- **Acceptance**: the emulator is automatically an AdMob test device, so a
  debug build must show "Test Ad" after the consent form; an internal-test
  build on the owner's Galaxy S22 Ultra must show a real banner, complete a
  license-tester purchase of each treat, hide the banner and the can, and
  survive a relaunch; the three languages are checked on the emulator.

## Release and store setup

Play Console has no API for creating apps or listings, and the Play
Developer API would need a linked service account this account does not
have. The owner is already signed in to Play Console and AdMob in the
browser on this Mac, so the agent operates that browser through computer
use (screenshots, clicks and typing on the owner's session, the way
Menkyo's Play Console edits were made), never a separate automation
browser, and reads every change back on screen before moving on. The owner
is asked for only two kinds of things: signing in again or passing a 2-step
prompt when Google demands it, and a one-line "go" in chat before each
outward step marked **(go)** below.

1. **Play Console, app**: create "猫叫模拟器 / Meow", default language
   zh-CN, free, package `jp.jacky.meow`; answer the declarations (ads: yes;
   no news, no COVID, no government, no financial features); content rating
   questionnaire (entertainment, no user content); target audience 13 and
   over, so the Families policy does not apply, which means the Play copy
   must not present the app as being for children (the Chinese subtitle
   "逗猫逗娃的神器" becomes "逗猫神器" on Play; the in-app strings stay);
   data safety filled from Google's current AdMob disclosure (the
   advertising id and whatever else the SDK declares, collected for
   advertising) plus nothing of the app's own, since taps and sounds never
   leave the device and Play handles purchases; privacy policy
   `https://meow.jacky.jp/privacy/`.
2. **Play Console, store listing** in zh-CN, en-US and ja-JP: names
   猫叫模拟器 / Meow / ニャー：猫の鳴き声 (the App Store names), short
   descriptions, full descriptions adapted from the App Store 2.1 copy the
   owner rewrote (read through the App Store Connect API) with the store,
   device and children wording changed, icon, feature graphic, screenshots. Contact email `newbdez33@gmail.com`, website
   `https://meow.jacky.jp/`.
3. **Play Console, products**: the two in-app products above, with names,
   descriptions and prices in the three languages; copy the app's licensing
   public key from Monetization setup into `app/build.gradle.kts`. Add the
   owner's account to License testing so purchases on the internal track
   are free.
4. **AdMob**: create the Android app, one banner unit, note both ids into
   `app/build.gradle.kts`; add the app to the EEA/UK privacy message.
5. **Build**: generate the upload keystore, build `bundleRelease`, validate,
   record the AAB path and SHA-256 in `docs/` as Menkyo does.
6. **Internal testing (go)**: upload the AAB to the internal track with the
   owner's account as tester; run the device acceptance above.
7. **Production (go)**: promote the same AAB to production with release
   notes in three languages; Google reviews and publishes.
8. **After it is public**: link the Play listing in AdMob so the crawler
   verifies `app-ads.txt`; add the Google Play badge to the three pages in
   `hosting/public/` and redeploy; add an item to `TODO.md` recording the
   release.

## Decisions recorded

- Native Kotlin + Compose rather than Flutter (Menkyo's stack): the iOS app
  stays SwiftUI, the app is tiny, and Google's ads, consent and billing
  SDKs are used directly without a plugin layer.
- Same repository, `android/` beside `meow/` and `hosting/`.
- Package `jp.jacky.meow`, following `jp.jacky.menkyo`.
- Ads and both treats in the first version, matching iOS 2.1.
- Device-side purchase verification, no server.
- Cats redrawn at 512 px with the image tool; the 128 px originals are the
  references and the fallback.
- Version `1.0 (1)`.
- Store setup through the owner's signed-in browser with computer use; the
  owner's manual part is limited to sign-in prompts, looking at the cats and
  the two "go"s.

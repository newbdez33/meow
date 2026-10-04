# Meow

A cat soundboard for iPhone and iPad, built with SwiftUI. Tap one of 27 cats
to hear its sound: purring, snoring, hissing, a kitten, or even a lion.

[App Store](https://apps.apple.com/app/id826362662) ·
[Google Play](https://play.google.com/store/apps/details?id=jp.jacky.meow) ·
[Website and sound demo](https://meow.jacky.jp/) ·
[Privacy policy](https://meow.jacky.jp/privacy/)

The Android version, a native Kotlin + Jetpack Compose port, lives in
[android/](android/README.md).

Version **2.1 (build 5)** has passed App Review, confirmed on September 29,
2026. Release history and remaining checks are in [TODO.md](TODO.md).

Widget release status, checked on October 4, 2026 at 11:03 JST:
**Android 1.1 (3)** is live on Google Play in 178 countries and regions.
**iOS 2.2 (6)** is available in TestFlight and is waiting for App Review,
with automatic release after approval. See the [release and validation
record](docs/specs/2026-10-03-widget-design.md#production-review-submission-2026-10-04).

## Features

- 27 illustrated cats with bundled sounds. Playback works offline.
- Home-screen widgets with independent sound and character choices: iOS 17+
  System Small and resizable Android widgets. Live on Android and available
  in iOS TestFlight; remaining device checks are tracked in [TODO.md](TODO.md).
- English, Simplified Chinese, and Japanese.
- No account or sign-in required.
- Banner ads with Google UMP consent and App Tracking Transparency support.
- Two optional one-time purchases: a can for the cat or a coffee for the
  author. Either removes ads; purchases can be restored with the same
  Apple ID.

## Development

The app supports iOS and iPadOS 15 or later. The project was built and
verified with Xcode 27; test targets require iOS 17 or later.

1. Open `meow.xcodeproj` and select the `meow` scheme.
2. Let Xcode resolve the Swift package dependencies. The pinned versions are
   Google Mobile Ads 13.10.0 and Google User Messaging Platform 3.1.0.
3. Select a simulator and run. For a physical device, configure signing
   with your development team.

The scheme uses [meow.storekit](meow/meow.storekit) for local purchase
testing. These transactions do not charge an App Store account.

## Tests and review assets

List simulators with `xcrun simctl list devices available`. Replace
`<SIMULATOR_UDID>` with an available simulator ID, then run the StoreKit tests:

```sh
xcodebuild test \
  -project meow.xcodeproj \
  -scheme meow \
  -destination 'platform=iOS Simulator,id=<SIMULATOR_UDID>' \
  -only-testing:meowTests/StoreTests
```

The five tests cover a fresh install, both purchases, an existing purchase
on relaunch, and restore.

- [StoreScreenshotTests](meowUITests/StoreScreenshotTests.swift) captures
  the grid and a selected cat in all three languages. Run on a simulator
  with `TEST_RUNNER_MEOW_SCREENSHOT_DIR` set to the capture directory. The
  Debug-only `-MeowNoAds` argument keeps ads and prompts out of these images.
- [TipSheetScreenshotTests](meowUITests/TipSheetScreenshotTests.swift)
  checks the purchase sheet and can save the separate in-app purchase
  review screenshot.
- [TrackingReviewTests](meowUITests/TrackingReviewTests.swift) resets tracking
  authorization, captures ATT, declines tracking, and taps a cat. It is
  skipped unless `TEST_RUNNER_MEOW_TRACKING_REVIEW=1`. Install the app first
  and use an English device with network access and tracking requests
  enabled. Set `TEST_RUNNER_MEOW_REVIEW_MANUAL_LAUNCH=1` to tap the icon
  yourself during a physical-device recording.
- [WidgetTests](meowTests/WidgetTests.swift) covers the catalogue, bundled
  resources, stale IDs, intent playback, replacement, and cleanup.
- [WidgetLauncherTests](meowUITests/WidgetLauncherTests.swift) checks warm and
  cold widget taps without foregrounding the app. Opt in on an English
  disposable simulator with `TEST_RUNNER_MEOW_WIDGET_LAUNCHER_TEST=1` and
  keep simulator signing enabled. See the [widget design and validation
  record](docs/specs/2026-10-03-widget-design.md) for the command and limits.

Compose store screenshots with [render_store_screenshots.swift](tool/render_store_screenshots.swift)
and [store-copy.json](design/store-copy.json). For the command below, place
captures in `/tmp/meow-captures/iphone/` and `/tmp/meow-captures/ipad/`, with
the filenames produced by `StoreScreenshotTests`:

```sh
swift tool/render_store_screenshots.swift \
  design/store-copy.json /tmp/meow-captures /tmp/meow-store-screenshots
```

Each language/device set contains four images: the grid, a selected cat,
the Home Screen widget, and its configuration. Add native captures named
`<lang>-3-widget.png` and `<lang>-4-configuration.png` beside the two app
captures. The renderer puts a transparent cat sticker in each page's upper
left corner and checks text fit. Keep price references, including "free",
out of store screenshots.

The current set is in [design/store/screenshots](design/store/screenshots).
[Store preview](design/store-preview.html) switches between four device
sizes and three copy languages. The iOS widget scenes currently use English
system UI. Android widget captions and configuration use the selected app
language. The [sticker generation record](design/store/stickers/generation.json)
contains the built-in `image_gen` prompts and original file paths; the
transparent PNGs are stored beside it. The submitted widget listing copy is in
[widget-introduction.json](design/store/widget-introduction.json) and the
[Play listing](design/store/play/copy.md).

The [widget record](docs/specs/2026-10-03-widget-design.md#local-records-and-reproduction)
also lists the private release archive, raw capture location, and artwork
export command. These records are outside the feature worktree so its removal
does not remove the release evidence.

## Repository

| Path | Purpose |
| --- | --- |
| `meow/` | SwiftUI app, audio, ads, purchases, and localized resources |
| `MeowWidget/` and `WidgetShared/` | Widget extension, App Intents, catalogue, and shared artwork |
| `meowTests/` | StoreKit and widget tests |
| `meowUITests/` | Purchase UI, screenshot, ATT, and Home Screen widget tests |
| `design/` and `tool/` | Store copy, artwork, widget preview, and asset export tools |
| [`hosting/`](hosting/README.md) | App website, privacy policy, and `app-ads.txt` |
| [`TODO.md`](TODO.md) | Implementation history and release follow-up |

Every new build goes to TestFlight first. Submit it to App Review only after
the owner has tested and approved it.

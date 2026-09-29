# Meow

A cat soundboard for iPhone and iPad, built with SwiftUI. Tap one of 27 cats
to hear its sound: purring, snoring, hissing, a kitten, or even a lion.

[App Store](https://apps.apple.com/app/id826362662) ·
[Website and sound demo](https://meow.jacky.jp/) ·
[Privacy policy](https://meow.jacky.jp/privacy/)

Version **2.1 (build 5)** has passed App Review, confirmed on September 29,
2026. Release history and remaining checks are in [TODO.md](TODO.md).

## Features

- 27 illustrated cats with bundled sounds. Playback works offline.
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

Compose store screenshots with [render_store_screenshots.swift](tool/render_store_screenshots.swift)
and [store-copy.json](design/store-copy.json). For the command below, place
captures in `/tmp/meow-captures/iphone/` and `/tmp/meow-captures/ipad/`, with
the filenames produced by `StoreScreenshotTests`:

```sh
swift tool/render_store_screenshots.swift \
  design/store-copy.json /tmp/meow-captures /tmp/meow-store-screenshots
```

Each language/device set contains two images. Keep price references,
including "free", out of store screenshots.

## Repository

| Path | Purpose |
| --- | --- |
| `meow/` | SwiftUI app, audio, ads, purchases, and localized resources |
| `meowTests/` | StoreKit tests |
| `meowUITests/` | Purchase UI, screenshot, and ATT tests |
| `design/` and `tool/` | Store copy, artwork, and screenshot composition |
| [`hosting/`](hosting/README.md) | App website, privacy policy, and `app-ads.txt` |
| [`TODO.md`](TODO.md) | Implementation history and release follow-up |

Every new build goes to TestFlight first. Submit it to App Review only after
the owner has tested and approved it.

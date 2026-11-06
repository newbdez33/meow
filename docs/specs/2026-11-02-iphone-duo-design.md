# iPhone Duo soundboard

This is the historical record for TestFlight 2.3 (7). Build 2.3 (8) implements
the [three-state layout and hinge cat](2026-11-05-duo-hinge-motion.md), including
the corrected Duo geometry. Use that record for the current implementation
and validation; the checks below describe the earlier release.

## Design

Keep the one-tap soundboard on a compact display. On a regular-width display,
put the first 15 cats in the leading pane and the remaining 12 in the trailing
pane. Reserve the trailing pane's last row for the banner. The original 27
cats, audio files, coral accent, and pink and yellow surfaces are unchanged.

| Available space | Presentation |
| --- | --- |
| Compact width or compact height | One scrolling grid with a selected-sound replay control |
| Regular width and height, wide | Two grids with three columns and five rows; the trailing ad spans the last row |
| Regular width and height, tall | A five-column leading grid above a four-column trailing grid and a 50-point ad row |
| Active fold on iOS 27.1+ | System split arrangement places each pane outside the fold |
| Small panes or accessibility text | Scrolling grids with wrapping labels and a replay control |

At standard text size, both grids fit without scrolling when the panes have
sufficient space. Tapping any cat plays it immediately; tapping it again
replays it. Fitted layouts omit the separate replay strip. `ViewThatFits`
selects the scrolling layout when the content cannot fit, and accessibility
text sizes always use that layout. iOS 15 also uses scrolling grids.

`SoundboardView` owns the selection and each pane's visible item ID above
its layout branches. `Sounds.shared` remains the single audio owner. Layout
changes do not start or stop audio. On iOS 17+, `scrollPosition` tracks stable
cat IDs; temporary nil positions do not erase the anchor. When the window
becomes compact, the selected cat becomes the scroll anchor. iOS 15 and 16
keep selection but do not track scroll positions.

## Implementation

- Use `NavigationStack` on iOS 16+ and a stack-style `NavigationView` on
  iOS 15. The app deployment target stays at iOS 15; widgets require iOS 17.
- Use `ArrangementView` with the split style on iOS 27.1+, at regular width
  and noncompact height. Earlier systems choose horizontal or vertical stacks
  from the current view bounds. No device model or hinge angle drives layout.
- Use stable cat IDs, scaled minimum cell sizes, and VoiceOver selection
  traits. Fitted grids contain independent accessible buttons. Scrolling
  layouts use `LazyVGrid`.
- Respect safe areas and allow resizable windows. Toolbar labels and images
  support side controls and overflow; keep the separate glass buttons.
- Keep share and support in system sheets. Add a close action to support
  and allow purchase labels to wrap.
- Request inline adaptive ads at the pane's actual width, with a 50-point
  maximum height. Wait 300 ms after width changes before requesting another
  size. Consent and purchase checks still gate requests. Fitted layouts
  reserve the ad row while consent, ad loading, or purchase state changes.
- Localize labels in English, Simplified Chinese, and Japanese. Reuse the
  existing artwork and audio; no new asset dependency is required.

## SDK and sources

Built with **Xcode 27.2 (27B52), iOS 27.2 SDK**. This is the 27.1+ SDK tier
that supports full-display layouts and system vertical bars on iPhone Duo.
The arrangement API was checked against the local SwiftUICore interface and
Apple's documentation. No minimum OS bump is needed for older devices.

Applied skills from
[iphone-duo-skills at c3fa17c](https://github.com/mirzaaghazadeh/iphone-duo-skills/tree/c3fa17c363ff555c16e44e3e94f16929fcab9646):
readiness, adaptive layout, vertical bars, dual-pane patterns, and design
review. The skill source is a design reference and is not a build dependency.

Primary references:

- [Designing for iPhone Duo](https://developer.apple.com/design/human-interface-guidelines/designing-for-iphone-duo)
- [ArrangementView](https://developer.apple.com/documentation/swiftui/arrangementview)
- [SplitArrangementViewStyle](https://developer.apple.com/documentation/swiftui/splitarrangementviewstyle)

## Validation on November 4, 2026

Passed with Xcode 27.2 and the iOS 27.0 runtime:

- `SoundboardLayoutTests` on iPhone 17e and iPad mini (A17 Pro): four
  applicable tests passed on each device; one device-specific test was
  skipped on each. Checks cover rotation, selection and replay, the last
  cat at the largest accessibility size, compact scroll retention, and
  support sheet dismissal.
- The expanded-grid regression waits for rotation to finish, verifies
  both fitted containers, and checks that all 27 buttons are hittable and
  fully inside the window. A follow-up passed after reserving the empty ad
  row, including row alignment checks with ads disabled.
- All five `StoreTests` and six `WidgetTests` passed. An initial iPad
  purchase-test run stalled; the bounded retry on iPhone passed.
- Signed Release archive, app and extension versions **2.3 (7)**, signature
  verification, plist/localization syntax checks, and `git diff --check`.

The no-scroll test first failed against the previous spotlight layout.
Review then found that the iPad mini in landscape fell back to scrolling:
reducing the fitted row minimum to 96 points made five rows fit. A separate
clear container reserves the ad row even when `Banner` has no content.

The native images below use the iOS 27.0 fallback stacks. They are **not
Duo pose captures**. Ads are disabled for captures; blank space in the
trailing pane is reserved for the banner. The compact English capture is
from the November 2 review; the dual-grid captures and Japanese accessibility
capture were refreshed on November 4.

| Compact, English | Regular, English |
| --- | --- |
| ![Compact English soundboard](../../design/iphone-duo-review/compact-en.png) | ![Regular English soundboard](../../design/iphone-duo-review/regular-en.png) |

| Japanese, largest accessibility size | Chinese, dark appearance |
| --- | --- |
| ![Japanese accessibility layout](../../design/iphone-duo-review/compact-ja-accessibility.png) | ![Chinese dark layout](../../design/iphone-duo-review/regular-zh-dark.png) |

### TestFlight 2.3 (7)

The signed package was uploaded on November 4, 2026 at 21:26 JST. At
21:31 JST, App Store Connect confirmed `VALID` processing status and
`IN_BETA_TESTING` for the existing Internal group. English, Simplified
Chinese, and Japanese test notes were saved and verified. Build ID:
`b14a76cc-669f-4fcb-ad78-99f0e9ece24e`.

[Open TestFlight in App Store Connect](https://appstoreconnect.apple.com/apps/826362662/testflight/ios).
No production version was submitted for App Review. The public version
remains 2.2 (6).

The upload reported missing dSYMs for the prebuilt Google Mobile Ads and
User Messaging Platform frameworks. App and widget dSYMs are present;
the third-party symbol warnings did not block delivery.

### Runtime limits

No iPhone Duo / iOS 27.1+ runtime is installed. The native arrangement and
vertical bars compile, but fold transitions, side controls, Split View,
live adaptive ad fill, and the iOS 15 fallback still need device checks.
The earlier CoreSimulator mismatch no longer prevents XCTest execution.

### Reproduction

Run on an iPhone and an iPad simulator:

```sh
xcodebuild test \
  -project meow.xcodeproj -scheme meow \
  -destination 'platform=iOS Simulator,id=<SIMULATOR_UDID>' \
  -parallel-testing-enabled NO \
  -test-timeouts-enabled YES \
  -default-test-execution-time-allowance 90 \
  -maximum-test-execution-time-allowance 90 \
  -only-testing:meowTests \
  -only-testing:meowUITests/SoundboardLayoutTests
```

Private release records are in
`~/Library/Developer/Meow/releases/2026-11-04-duo/`. The upload archive is
`Meow-2.3-7.xcarchive`; `archive-upload.log` and `upload.log` record packaging
and delivery. Passing result bundles are `iphone-ui-final.xcresult`,
`ipad-fit-final.xcresult`, `ipad-ad-row.xcresult`, `unit-final.xcresult`, and
`store-final.xcresult`. Test results and signing files are not committed.

### Interactive HTML review

Run the preview on macOS with Python 3:

```sh
python3 tool/serve_iphone_duo_preview.py --bind 127.0.0.1 --port 8801
```

Open `http://127.0.0.1:8801/`. To review from another device on a Tailnet,
set `--bind` to the host's Tailscale IP address. The server exports only the
preview, its required app assets, and the native screenshots to a temporary
directory. Restart it after source changes.

The HTML review supports closed, open, book, tabletop, and left/right split
layouts. It uses all 27 app images and audio files, the three native string
tables, and prices from the local StoreKit configuration. Language, appearance,
text size, and selection are preserved in the URL. Purchase and restore
buttons show preview feedback only.

The preview defaults to `dual-grid`: the first 15 sounds
fill the leading pane, and the other 12 appear in the trailing pane. At standard
text size, open/book poses use three columns and five rows per pane. The trailing
pane's last row spans all three columns for an ad slot, aligned with the
leading pane's last row of cats. In tabletop, the leading pane uses five columns
and three rows; the trailing pane uses four columns and three rows, followed by
a 50 px ad row. All 27 cards and the ad slot fit without scrolling. The preview
does not request or display live ads. It shows a local 50 px sample banner
centered in the ad row, matching the native banner's height limit. The ad
preview control switches between the sample and a blank row; `ad=sample`
and `ad=empty` preserve that choice in the URL. Compact layouts show the
sample above the cat grid. The creative is marked as a sample in all three
languages, and its action only shows preview feedback.
Tapping the selected card replays its sound, so this layout omits the separate
replay strip. Both grids play audio through one player and keep one selection.
Large text retains scrolling and the replay strip. Closed and split windows
merge all 27 cards into one scrolling list. The design switch restores the original
`spotlight` option for comparison. The selected design is also stored in the
URL. The native app now implements the dual-grid design with content-fit fallback.

Browser checks confirmed that all 27 cards, their labels, and the sample banner
stay within the panes without scrolling in open, book, and tabletop poses in
all three languages at standard text size. The ad row aligns with the last
cat row in open and book poses. Banner height is 50 CSS pixels in all nine
pose/language combinations, with no text overflow. The blank state preserves
row alignment after reload. Dark appearance and a 390 px page viewport were
also checked. The smallest card was 110 by 108 CSS pixels
in book pose and 121 by 89 in tabletop. Large text still allows scrolling
and replay. These measurements describe the HTML mockup, not native hit targets.

System bars, folds, window sizes, and large text are design approximations.
This page does not replace native device validation. Browser checks covered
selection across layouts, audio playback requests and replay, sheet dismissal,
and the last cat at large text on a 390 px viewport. No console errors or
horizontal page overflow were observed.

### Remaining device review

- [ ] Closed, open flat, book, and tabletop: verify pane placement, side
  controls, safe areas, and all 27 cat hit targets.
- [ ] Select and scroll near the last cat, then fold and unfold without
  restarting the app. Verify selection, scroll anchor, and uninterrupted
  audio. Repeat with the support and share sheets open.
- [ ] Split View on the left and right, plus pinned Picture-in-Picture:
  verify that cats, replay, prices, close, and restore remain reachable.
- [ ] Repeat with the largest accessibility text size, dark appearance,
  VoiceOver, and all three app languages.
- [ ] With consent enabled and Google test ads configured, resize repeatedly
  and check that the banner stays within its pane and loads after settling.
- [ ] Run on an iOS 15 device to check the legacy navigation and sheet path.

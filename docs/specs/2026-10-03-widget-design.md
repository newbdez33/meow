# Meow home-screen widgets

Design approved by the owner on 2026-10-03 for iOS and Android. Native
implementation and simulator validation are recorded below.

Open [the interactive preview](../../design/widget-preview.html) in a browser.
Keep the file in this checkout: it uses the existing cat images and MP3 files.
No build, network connection, or package installation is needed. The preview
can change sounds and characters independently on each phone, play all 27
sounds, switch language and appearance, and compare Android sizes. Browser playback does
not prove that background playback works on either native platform.

## Approved design

The owner expanded the direction to a set of expressive characters, like
the personality-driven artwork used in Duolingo widgets. Use recognizable
cats from Meow, with larger faces, clear expressions, and their own background
colors. The Character setting chooses the widget artwork.

The [six new character images](../../design/imagegen/widget-characters/README.md)
were generated with the built-in image tool using existing app cats as
identity references. Keep [the original white cat](../../design/widget-cat.svg)
as a seventh choice. The owner approved all seven choices.

| Character ID | Name | Existing cat | Visual direction |
| --- | --- | --- | --- |
| `ginger` | Smug | `c04` | Orange tabby, half-closed eyes, blush background |
| `black` | Spicy | `c02` | Black cat, yellow eyes, pink tongue, mint background |
| `gray` | Chill | `c01` | Blue-gray tabby, content smile, butter background |
| `sleepy` | Sleepy | `c17` | Beige tabby, closed eyes, lavender background |
| `pumpkin` | Pumpkin | `c16` | Black cat in a pumpkin, startled eyes, sage background |
| `ghost` | Boo! | `c23` | Ghost sheet, yellow eyes, visible black tail, blue background |
| `classic` | Classic | App icon | Original white head on green |

The whole widget is one playback button. Each instance stores one sound
and one character. These choices are independent: a sleepy cat can play
the angry sound. Changing one field must not reset the other.
Show its localized name to distinguish widgets with different sounds.
Recommend **Whispering** (`m_004`) with **Smug** (`ginger`) as the initial
pair. The browser starts Android on Spicy to show two independent examples.

| Detail | iOS | Android |
| --- | --- | --- |
| Minimum version for this feature | iOS / iPadOS 17 | Android 7, API 24 |
| Initial size | System Small, a 2 × 2 home-screen footprint | Compact, a 1 × 1 cell request |
| Other sizes in v1 | None | Resize to 2 × 2 |
| Sound label | Inside the tile | Below the face at 1 × 1; inside at 2 × 2 |
| Add | System widget gallery, with a default sound | Launcher calls the configuration activity |
| Choose sound | Edit Widget → Sound → system entity list | Custom list with a separate audition button |
| Choose character | Edit Widget → Character → system list with image previews | A six-image grid plus Classic |
| Save | System configuration commits the pair | Save widget commits both fields together |
| Change later | Long-press → Edit Widget | Launcher reconfiguration where supported; app fallback |
| Tap | Play without navigating into Meow | Play without navigating into Meow |

The Android label is part of our widget, not a label supplied by the
launcher. Hide it if the available bounds cannot fit it safely. Keep a
minimum 48 dp tap target. Grid size and outer padding vary by launcher;
1 × 1 is a request, not a fixed pixel size. Use the supplied widget bounds.

Keep each character's full-color background in light and dark appearance.
The image fills the rounded widget; a light caption backing keeps the sound
name readable over the artwork. Android 1 × 1 keeps the label below the image.
For the iOS accented/tinted rendering mode, preserve the silhouette and eye
contrast. The native widget uses separate template masks for system tinting.
The HTML tint is an approximation; system rendering needs native previews
and device checks.
Use one line for names, tail truncation when needed, and the complete name
in the accessibility label. Keep existing English, Chinese, and Japanese
sound names. Do not add a title, play badge, volume slider, or decorative
status indicator inside the widget.

## Behavior and configuration

- A tap plays the chosen local MP3 once. A second tap replaces playback and
  starts the chosen sound from the beginning, matching the current app.
- All widget and in-app playback must use one player owner per platform.
  Do not allow overlapping sounds from separate instances or the app.
- Store stable `soundID` and `characterID` values, not array positions or
  translated names. A stale ID falls back only for its own field.
- Configuration changes affect only that widget. Cancelling Android setup
  leaves no configured instance; cancelling reconfiguration preserves the
  previous pair. The preview also offers an explicit discard path. Editing
  uses a draft; save commits both values and cancel discards both.
- iOS uses the system-owned configuration UI. The HTML is a flow model, not
  a promise of identical system chrome. Do not add custom audition controls
  to that system list. Use a separate character parameter with localized
  names and preview images. Android supports audition without changing
  either selection. Character choices stay fixed until the owner edits them.
- Adding or updating a widget must not start playback. No timed updates,
  network calls, repeating audio, or timeline polling are needed.
- Keep the widget visually stable during playback. Use normal system press
  feedback. A browser animation would not establish that a WidgetKit view
  can animate continuously or receive audio-completion updates.
- Audio follows device output and media volume. Stop and release resources
  at completion or interruption. Do not request the microphone.

## Native approach

### iOS

Keep the main app's iOS 15 minimum. Add an iOS 17 widget extension with
`AppIntentConfiguration`, a sound `AppEntity` catalogue, a character parameter,
and `.systemSmall`.
The initial release does not add an iOS 15–16 widget that unexpectedly opens
the app. The owner approved this version boundary.

Use `Button(intent:)` with an `AudioPlaybackIntent` and no foreground-opening
behavior. Apple runs this intent in the app process. Include its required
declarations in the correct targets, and keep audio resources and player
ownership in the app. Do not try to play audio in a timeline provider.
See [Apple's interactive widget guidance](https://developer.apple.com/documentation/widgetkit/adding-interactivity-to-widgets-and-live-activities)
and [configurable widgets](https://developer.apple.com/documentation/widgetkit/making-a-configurable-widget).

Refactor `Sounds.swift` into a retained player owner shared by app taps and
the playback intent. Configure a playback audio session with mixing, and
the audio background mode for the intent's background lifetime. Deactivate
the session at completion. Validate silent-switch behavior, interruption,
and cold launch on a device before promising this behavior. Apple's
[audio session guidance](https://developer.apple.com/documentation/avfoundation/configuring-your-app-for-media-playback)
describes the session and background capability.

The configuration already carries the sound and character IDs. Include the
approved artwork and its localized names in the widget extension. A new shared database,
App Group, or background refresh job is not needed for this static catalogue.
Keep the existing ad/consent startup attached to foreground UI; invoking
the audio intent must not present UI or request ads.

### Android

For this small static layout, use `AppWidgetProvider` and `RemoteViews`.
Use the existing Compose stack for the configuration activity. Adding
Glance only for one image, one label, and one action adds little value.
Export platform image resources from the approved PNG artwork and the
Classic SVG; `RemoteViews` does not render the SVG file directly. Keep the
1254 px generated originals as source assets, then size exports for the
widget's actual bounds.

Store both IDs by `appWidgetId`. Give each click action a unique immutable
`PendingIntent` identity; extras alone do not distinguish pending intents.
Clean up state on deletion, and remap IDs during widget restore. The config
activity starts with `RESULT_CANCELED`, then persists and updates the
widget before returning `RESULT_OK`.
See [Android widget configuration](https://developer.android.com/develop/ui/views/appwidgets/configuration)
and [flexible layouts](https://developer.android.com/develop/ui/views/appwidgets/layouts).

The tap starts a short-lived `mediaPlayback` foreground service, with a
media notification and a stop action. The service owns playback, enters
the foreground promptly, and removes its notification and stops on
completion or error. Do not keep an asynchronous `MediaPlayer` alive only
through a broadcast receiver. User interaction with a widget is an
[exemption to background service-start restrictions](https://developer.android.com/develop/background-work/services/fgs/restrictions-bg-start).
Declare the [media playback service type and permissions](https://developer.android.com/develop/background-work/services/fgs/service-types#media).

Unlike the current in-app player, this path should request transient audio
focus with ducking, then abandon it at completion. Starting the foreground
service first is required for focus on current target SDKs. Stop if focus
is denied or lost. This can briefly lower other audio, so verify the user
experience on Pixel and Samsung hardware. See
[Android audio focus](https://developer.android.com/media/optimize/audio-focus).
Do not start billing, consent, or ad requests solely for widget playback.

## Release validation matrix

1. Prove cold-start playback first: screen on, launcher visible, app process
   absent, then tap. Distinguish process eviction from an explicit force-stop.
   Check iOS 17 and the current supported iOS release; check Android API 24,
   31, 35, and 36, with Pixel and Samsung launchers where available.
2. Verify independent sound/artwork selection, atomic save, cancellation,
   two independent instances, stale-ID
   fallback, deletion, update, reboot, and Android restore/remapped IDs.
3. Rapid taps, app-to-widget overlap, interruption, Bluetooth route changes,
   silent mode, zero volume, focus denial, and media notification cleanup.
4. Check all 27 sounds, seven artwork choices, and all three languages. Verify small bounds, large
   fonts, screen-reader labels, dark appearance, and iOS system tinting.
5. Confirm that playback does not foreground the app, trigger ads or consent,
   loop audio, or leave a service/session active after completion.

Implementation checks and remaining device coverage are recorded in TODO.md.

## Implementation notes

- iOS uses `MeowWidget` and `WidgetShared`. The extension carries the catalogue,
  artwork, and localized strings. Only the app carries audio and owns playback.
  `Sounds` serializes session and player operations off the main thread. The
  foreground screen starts billing and consent only while its scene is active.
- Android uses a `RemoteViews` provider, a Compose configuration activity,
  atomic per-instance preferences, and a short-lived playback service. The
  app, auditions, and service share `AudioPlayback`. Releasing an old view
  cannot stop a new owner's sound. Store and ad objects are created lazily.
- Export artwork with `tool/export_widget_art.py`; keep generated originals in
  `design/imagegen/widget-characters`. The iOS tinted images use alpha masks.
  Avoid `.widgetAccentedRenderingMode(.desaturated)` on the playback label:
  it caused taps to open the app in the iOS 27 simulator. Apple's
  [confirmed rendering issue](https://developer.apple.com/forums/thread/763804)
  describes the same failure.
- Keep simulator signing enabled when checking App Intents. An unsigned build
  compiled and passed direct intent tests but the system rejected the widget's
  Shortcuts service connection because its application identity was missing.

Native screenshots: [iOS home screen](../../design/screenshots/widgets/ios-home.png),
[Android configuration](../../design/screenshots/widgets/android-configuration.png),
[character picker](../../design/screenshots/widgets/android-characters.png), and
[Android home screen](../../design/screenshots/widgets/android-home.png).

## Checks completed on 2026-10-03

| Environment | Result |
| --- | --- |
| iPhone 18 Pro simulator, iOS 27 | 11 unit tests and one Home Screen UI test passed |
| Generic iOS device, Release | Unsigned app and widget build passed |
| Pixel emulator, Android API 36 | 34 unit tests and 18 instrumented tests passed; debug APK and lint passed |
| HTML preview | Independent selection, all 27 audio files, three languages, save/cancel, audition, appearance, and responsive layouts checked |

On both simulators, the app process was absent before tapping the widget.
The tap started playback while the launcher stayed visible. iOS playback
logs and Android audio/service state showed completion cleanup. This checks
process termination, not a user's explicit force-stop. Android launcher
checks also covered compact and enlarged layouts, reconfiguration, and
rapid taps. Automated checks cover stale IDs, independent field fallback,
shared playback replacement, and Android save/cancel and overlapping restore IDs.

Run the iOS checks on an English disposable simulator. The UI test adds a
widget using the iOS 18+ icon menu, or uses an existing default Meow widget.
It changes the Home Screen. Keep signing enabled for App Intents:

```sh
TEST_RUNNER_MEOW_WIDGET_LAUNCHER_TEST=1 xcodebuild test \
  -project meow.xcodeproj -scheme meow \
  -destination 'platform=iOS Simulator,id=<SIMULATOR_UDID>' \
  -only-testing:meowTests \
  -only-testing:meowUITests/WidgetLauncherTests \
  CODE_SIGNING_ALLOWED=YES CODE_SIGN_IDENTITY=-
```

From `android/`, with JDK 21 and the Android SDK configured:

```sh
./gradlew :app:testDebugUnitTest :app:lintDebug :app:assembleDebug \
  :app:connectedDebugAndroidTest
```

The initial checks used simulators. Physical S22 coverage is recorded below.
iOS device behavior, older OS versions, native system tinting, iOS
configuration UI, screen readers, Bluetooth, and silent-mode behavior remain
release checks in [TODO.md](../../TODO.md).

## Galaxy S22 Ultra acceptance, 2026-10-03

Tested the owner's SM-S9080 through ADB on `jx`: Android 16 / API 36,
One UI 8, English, dark appearance, 1080 × 2316 px at 450 dpi. The test
used `jp.jacky.meow.widgettest` (`Meow Widget Test`, `1.0-widget-test`),
installed beside the Play version. The production package's version,
installer, and last-update time remained unchanged.

| Check | Observed result |
| --- | --- |
| Instrumented regression tests | All 18 passed after the fix; local 34 unit tests, lint, and debug build also passed |
| Samsung widget picker | Added and configured a 1 × 1 widget through the launcher |
| Independent instances | Widget 7 saved `m_004/black`; widget 8 saved `m_002/ginger` |
| Launcher resize | Resized widget 8 to 2 × 2; caption moved into the artwork; widget 7 stayed compact |
| Cold playback | Confirmed absent app PID before tapping; a new process played audio while One UI stayed foreground |
| Rapid taps | Alternated between both instances five times; only one player remained active |
| Completion | Player released, audio focus abandoned, and foreground service removed |
| Media controls | Notification Pause and a dispatched media Pause both stopped the active service |
| Zero media volume | Playback completed and cleaned up with volume at zero |
| Configuration and text | Dark appearance and 200% text remained usable; changing a character then cancelling preserved both saved pairs |
| In-app configuration | Editing from the app returned to its widget list after Cancel |

One UI exposed a navigation issue: reopening configuration from its widget
menu reused the main app task, so Save returned to the app. Setting an empty
`taskAffinity` and excluding the configuration task from Recents fixed it.
Save and Cancel now return to Home when launched there. The in-app path
still uses its caller's task. This follows Android's
[task-affinity rules](https://developer.android.com/guide/components/activities/tasks-and-back-stack).

The test APK SHA-256 is
`220c68d81aa798ee715bca0b166789c3d8fe12cb694eeba1016c9120504ca3b5`.
Raw logs, UI trees, screenshots, and both APKs are in
`android/build/widget-s22-20261003/` (ignored to keep personal device captures
out of source control). The original media volume of 8/15 and font scale of
0.8 were restored. The test runner was removed; the test app and its two
widgets remain on Home page 2 for owner review.

These checks cover a debug package on one physical device. They do not
establish release-signing, billing, real-ad, Bluetooth, call-interruption,
reboot, backup/restore, or screen-reader behavior. Playback evidence comes
from Android's audio state and service lifecycle; audible output was not
independently recorded. The phone stayed in its existing vibrate mode.

## Internal distribution, 2026-10-03 to 2026-10-04

- iOS 2.2 (6): signed archive and App Store Connect upload passed. Build
  `b9fe487a-2e20-448f-b352-52f515e690b2` completed processing and was added
  to the existing `Internal` TestFlight group. API state: `VALID` and
  `IN_BETA_TESTING`. Test notes are in en-US, zh-Hans, and ja. The app and
  extension both use version 2.2 (6), with minimum OS versions 15 and 17.
- Android 1.1 (3): available to the existing internal tester list from
  2026-10-03 23:54 JST. The signed AAB passed bundletool validation. Its
  minified universal APK passed app cold start and widget-list screen checks
  on API 36, with no fatal or Room construction errors. The emulator APK
  used the debug certificate; Google Play signs delivered APKs. The normal
  package is `jp.jacky.meow`, without the S22 test package suffix.
- Known upload warnings: Google Ads and UMP framework dSYMs are absent from
  the iOS archive; Play reports missing native debug symbols for bundled
  native code. Both stores accepted the builds. The Android ReTrace mapping
  file is attached to the AAB.

Both releases retain the existing tester access. The owner authorized
production review on 2026-10-04; the submission record is below. Remaining
device checks stay open in `TODO.md`.
Build logs, API confirmation, and the Android publishing screenshot are in
`android/build/internal-20261003/` (ignored).

## Intermittent iOS tap report, 2026-10-04

The owner reported a silent single tap and an app launch after a double tap
in TestFlight 2.2 (6), then confirmed that a later tap played sound. The
reported pair was `m_001` with the black character, in full-color appearance.
The physical phone was not connected for log collection, so the cause is
unconfirmed. This report does not establish a fix or a device acceptance pass.

The iPhone 18 Pro simulator on iOS 27 passed Release checks for the default
pair in normal and Clear appearance. The reported black / `m_001` pair also
passed three warm taps and three cold starts. Each tap produced a
`Playing m_001` log and the app stayed out of the foreground. The test now
waits up to five seconds for asynchronous cold startup before checking app
state; an immediate state assertion could run before the process started.
Audio output from the simulator speaker was not recorded. No playback code
or distributed build changed in this investigation.

`WidgetLauncherTests` accepts these optional test-runner variables:

- `MEOW_WIDGET_CONFIGURE=1`: select `I'm Good` and `Spicy` before playback.
- `MEOW_WIDGET_APPEARANCE`: select an English Home Screen appearance label.
- `MEOW_WIDGET_CAPTURE_DIR`: save the configuration and Home Screen PNGs.
- `MEOW_WIDGET_POINT=x,y`: use a normalized Home Screen point when simulator
  accessibility reports an invalid widget hit point. Check the visible widget
  position before setting this value.

Prefix these with `TEST_RUNNER_` when invoking `xcodebuild`. The tests require
an English disposable simulator. Logs from the reported-pair run are in
`android/build/widget-ios-20261004/` (ignored).

## Production review submission, 2026-10-04

The owner approved the new screenshots and requested submission of both
platforms. The submissions use the existing tested binaries without a rebuild.

- iOS 2.2 (6): `WAITING_FOR_REVIEW` from 08:32 JST. Version ID:
  `b83d5585-c93f-4563-85c5-d4ae8360f716`; review submission:
  `a84a06df-d477-4f76-8dec-d3658ceb0ac5`. All 24 screenshots are `COMPLETE`
  and ordered across iPhone, iPad, and en-US/zh-Hans/ja. Descriptions and
  release notes include widgets; review notes explain setup and background
  playback. Release type remains `AFTER_APPROVAL`.
- Android 1.1 (3): promoted from internal testing to Production using the
  same signed AAB. Google Play accepted 16 changes: the full rollout to all
  existing target countries plus five listing changes per language in
  zh-CN/en-US/ja-JP. Publishing overview showed `Changes in review` at
  08:42 JST, with automatic checks still running. Managed publishing was off,
  so approval released the update.
- Google Play uses 24 unique new screenshot files in 36 slots: four pages
  each for phone, 7-inch tablet, and 10-inch tablet in three languages. The
  7-inch and 10-inch sets share the tablet files. All 24 files were declared
  as created or edited with AI because they include generated cat artwork.
  The app icon and existing feature graphic were not changed.

API responses, screenshot checksums, submitted listing text, the Google Play
DOM record, and the submission screenshot are in
`android/build/review-20261004/` (ignored), preserved in the private archive
below. The intermittent iOS tap report above remains unresolved.

At 11:03 JST, Google Play confirmed Android 1.1 (3) as `Active` in Production
across 178 countries and regions. Publishing overview showed publication on
October 4; Test and release showed no unpublished changes. App Store Connect
still reported iOS 2.2 (6) as `WAITING_FOR_REVIEW`. These are status observations
at that time, not a guarantee of the current store state.

## Local records and reproduction

Before removing the feature worktree, 315 local files (about 176 MiB) were
copied to this private directory and verified against their sources with
SHA-256:

`/Users/jacky/Library/Developer/Meow/archives/2026-10-04-widgets/`

Its `manifest.json` records the source path, archive path, size, and checksum
of each copied file. It contains the signed Android AAB and ReTrace mapping,
internal distribution and production submission records, S22 and iOS test
logs, Android test and lint reports, native Android captures, and signing
configuration. Submission records can contain contacts and upload URLs; keep
this directory private and outside Git. Original submission responses retain
their original states; `production-status-20261004.json` records the later
store status separately.

The signed iOS archive is also outside the worktree:

`/Users/jacky/Library/Developer/Xcode/Archives/2026-10-03/Meow 2.2 (6) - Widgets.xcarchive`

Raw store captures are in
`/Users/jacky/showme/meow-store/widgets-20261004/captures/`. The final 48 store
screenshots, copy, original character art, transparent stickers, and generation
records are committed under `design/`. Absolute paths in the generation records
describe the original session; the checked-in PNGs are the inputs for future
exports. From any checkout, regenerate native widget assets with:

```sh
DYLD_FALLBACK_LIBRARY_PATH=/opt/homebrew/lib \
  uv run --with pillow --with cairosvg tool/export_widget_art.py
```

Use `tool/render_store_screenshots.swift` with the raw capture directory and
the checked-in copy to regenerate store screenshots; see the root README for
the command and required filenames. The private archive also contains the
Android widget capture helpers used for this release. They depend on specific
emulator coordinates and device IDs; inspect them before reuse. The committed
iOS UI test and `android/tool/shots.sh` document the supported capture paths.

The widget and store HTML previews work from a fresh checkout with their
relative assets. The session's Tailscale URL is temporary; it is not a permanent
asset location.

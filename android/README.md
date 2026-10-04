# Meow for Android

Native Kotlin + Jetpack Compose port of the iOS app in `../meow/`. Design:
`../docs/specs/2026-09-27-android-design.md`; plan: `../docs/plans/2026-09-27-android.md`.

## Build

```sh
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
./gradlew :app:assembleDebug            # debug: Google's sample ad ids, purchases never count
./gradlew :app:testDebugUnitTest        # JVM tests (store, ads controller, verifier, catalogs)
./gradlew :app:connectedDebugAndroidTest   # Compose tests on the meow_api_36 emulator
./gradlew :app:bundleRelease            # needs key.properties (see Signing)
```

Instrumented tests are filtered with
`-Pandroid.testInstrumentationRunnerArguments.class=<fully.qualified.TestClass>`.

Generated sources, rerun after changing the iOS originals:

```sh
python3 tool/import_strings.py   # res/values*/strings.xml from ../meow/*.lproj/Localizable.strings
python3 tool/export_art.py       # res/drawable-nodpi from ../design/imagegen and ../meow/Assets.xcassets
python3 tool/make_icon.py        # launcher icons from the iOS app icon
python3 tool/contact_sheet.py    # old-vs-new cat sheet for review (build/cats-contact-sheet.png)
```

Debug screenshot mode (no consent form, no banner, both prices shown by a preview store):

```sh
adb shell am start -n jp.jacky.meow/.MainActivity --ez meowNoAds true --es meowStore preview
```

## Home-screen widgets

Add Meow from the launcher widget picker. Choose one of 27 sounds and seven
characters independently; the audition button does not change selection.
The widget requests one cell and can resize to two by two. Launcher bounds
control its image and caption layout. Use the app's widget button to edit
existing instances when the launcher has no reconfiguration action.

Widget taps use a short-lived media playback foreground service and the
same player as the app. The notification and audio focus end with playback.
Widget startup does not initialize billing or ads. Instrumented tests cover
configuration save/cancel, restore IDs, pending intents, and player ownership.
See the [shared design and validation record](../docs/specs/2026-10-03-widget-design.md).

For device tests beside a Play install, use the optional init script. It
changes only the debug package ID and label; the Play app and its data stay
separate. Run without this script for normal builds:

```sh
./gradlew -I tool/widget_device_test.init.gradle \
  :app:assembleDebug :app:assembleDebugAndroidTest
adb -s <SERIAL> install -r app/build/outputs/apk/debug/app-debug.apk
adb -s <SERIAL> install -r app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
adb -s <SERIAL> shell am instrument -w -r \
  jp.jacky.meow.widgettest.test/androidx.test.runner.AndroidJUnitRunner
```

The installed app is **Meow Widget Test** (`jp.jacky.meow.widgettest`).
S22 acceptance on Android 16 / One UI 8 passed 18 instrumented tests and
manual launcher checks on 2026-10-03, including a fix for configuration
returning to the main app instead of Home.

Export native widget artwork from the repository root:

```sh
DYLD_FALLBACK_LIBRARY_PATH=/opt/homebrew/lib \
  uv run --with pillow --with cairosvg tool/export_widget_art.py
```

## Signing

The upload key is `~/.android/meow-upload/upload.jks` (alias `meow-upload`, RSA 4096, valid to
2054-02-12), read through the ignored `key.properties`; the password sits next to it in
`store-password.txt` and the public certificate in `upload-certificate.pem`, and a copy of
`key.properties` is kept there too (copy it into `android/` in a fresh checkout; `local.properties`
only needs `sdk.dir`). Play App Signing
holds the app signing key. Upload certificate fingerprints:
SHA-1 `BD:26:C0:F9:CC:0F:47:3F:17:8F:29:0B:52:44:0A:2A:4F:36:98:42`,
SHA-256 `90:A1:48:46:38:88:72:83:0F:1C:9B:04:DC:0F:8B:A6:B1:2C:C1:72:5D:ED:57:97:C6:4C:0C:66:1F:60:64:A6`.

Release validation: `java -jar ~/.local/share/bundletool/bundletool-all-1.18.3.jar validate --bundle=app/build/outputs/bundle/release/app-release.aab`,
then `build-apks --mode=universal` and a cold start of the resulting `universal.apk` on the
emulator (the minified build once died in WorkManager's Room database; `proguard-rules.pro`
keeps that constructor).

## Ids

| What | Value |
| --- | --- |
| Package | `jp.jacky.meow` |
| Play Console app | `https://play.google.com/console/u/0/developers/4896965748454075126/app/4976190229557343886/app-dashboard` (account Salmonapps, newbdez33@gmail.com) |
| AdMob app "Meow simulator" (Android) | `ca-app-pub-1295607594822275~8527399220` |
| AdMob banner unit `meow_banner` | `ca-app-pub-1295607594822275/4197186257` |
| EEA/UK consent message | "Meow - European regulations" (shared with the iOS app) |
| Products | `jp.jacky.meow.can` (USD 5.99), `jp.jacky.meow.coffee` (USD 2.99) |

## Releases

| Build | Track | State |
| --- | --- | --- |
| 1.0 (2), AAB SHA-256 `2e5088cebaa507c5039e553a0f05d34d596f59814bc2a11145b7b51feb740d16` | Production, live 2026-09-29 | built 2026-09-28: the banner is the grid's first row as on iOS (build 1 had it under the grid); bundletool-validated, cold start ok on the API 36 emulator; published to internal testers 2026-09-28 16:14 and accepted on the owner's Galaxy S22 Ultra (see `TODO.md` item 5); sent for review on the Production track (all countries) 2026-09-28 16:45 JST; approved and live on https://play.google.com/store/apps/details?id=jp.jacky.meow by 2026-09-29 |
| 1.1 (3), AAB SHA-256 `a25dac8dc7554ad4bcd96b60d355c493a85a7d7c6242f1c241819b2bbe0c7c7b` | Production, live 2026-10-04; internal testing remains available | Home-screen widgets with 27 sounds and seven characters; Samsung configuration navigation fix; includes the `Store.purchase` fix and Fragment 1.9.1. Signed with the existing upload key, bundletool-validated, and the minified universal APK passed app cold start and widget-list screen checks on API 36 with no fatal or Room construction errors. The emulator APK used the debug certificate; Play signs delivered APKs. Release notes: zh-CN, en-US, ja-JP. Existing tester list and [join link](https://play.google.com/apps/internaltest/4701236019639669858) retained. Promoted to Production on owner approval on 2026-10-04, with new screenshots and three localized listings. At 11:03 JST, Production was Active in 178 countries and regions; Publishing overview reported publication on October 4, with no unpublished changes. Full rollout; managed publishing remains off. Evidence: `build/review-20261004/`, preserved in the [private archive](../docs/specs/2026-10-03-widget-design.md#local-records-and-reproduction). |
| 1.0 (1), AAB SHA-256 `f5cee09beb7cb1e190d59338b9cb4db9de2ea43babc1cc748ed09373600b295d` | Internal testing | built 2026-09-27, bundletool-validated, cold start ok on the API 36 emulator; published to internal testers 2026-09-28 00:08 (tester list "Menkyo internal testers" = newbdez33@gmail.com), join link https://play.google.com/apps/internaltest/4701236019639669858 |

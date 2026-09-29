# Meow for Android

Native Kotlin + Jetpack Compose port of the iOS app in `../meow/`. Design:
`../docs/specs/2026-09-27-android-design.md`; plan: `../docs/plans/2026-09-27-android.md`.

## Build

```sh
export JAVA_HOME=/opt/homebrew/opt/openjdk@21/libexec/openjdk.jdk/Contents/Home
./gradlew :app:assembleDebug            # debug: Google's sample ad ids, purchases never count
./gradlew :app:testDebugUnitTest        # JVM tests (store, ads controller, verifier, catalog)
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

## Signing

The upload key is `~/.android/meow-upload/upload.jks` (alias `meow-upload`, RSA 4096, valid to
2054-02-12), read through the ignored `key.properties`; the password sits next to it in
`store-password.txt` and the public certificate in `upload-certificate.pem`. Play App Signing
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
| next: 1.0 (3) | not built | carries the `Store.purchase` fix from commit 5f61793 (a sheet that does not open re-reads the entitlement); bump `versionCode` to 3 before building |
| 1.0 (1), AAB SHA-256 `f5cee09beb7cb1e190d59338b9cb4db9de2ea43babc1cc748ed09373600b295d` | Internal testing | built 2026-09-27, bundletool-validated, cold start ok on the API 36 emulator; published to internal testers 2026-09-28 00:08 (tester list "Menkyo internal testers" = newbdez33@gmail.com), join link https://play.google.com/apps/internaltest/4701236019639669858 |

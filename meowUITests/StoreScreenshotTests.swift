//
//  StoreScreenshotTests.swift
//  meowUITests
//
//  Created by Jacky on 2026/09/27.
//

import StoreKitTest
import XCTest

/// Takes the App Store screenshots: the grid and the grid with a cat selected,
/// once per store language, without ads or prompts. Run it on the device whose size the store wants:
///
///     TEST_RUNNER_MEOW_SCREENSHOT_DIR=/path/iphone xcodebuild test -only-testing:meowUITests/StoreScreenshotTests ...
///
/// Files are written as `<locale>-<n>-<name>.png`. Without the directory the test is skipped.
final class StoreScreenshotTests: XCTestCase {
    private static let locales = [("en", "en_US"), ("zh-Hans", "zh_CN"), ("ja", "ja_JP")]

    func testCaptureStoreScreenshots() throws {
        guard let dir = ProcessInfo.processInfo.environment["MEOW_SCREENSHOT_DIR"], !dir.isEmpty else {
            throw XCTSkip("MEOW_SCREENSHOT_DIR is not set")
        }
        let session = try SKTestSession(configurationFileNamed: "meow")
        session.resetToDefaultState()
        session.disableDialogs = true
        session.clearTransactions()
        let folder = URL(fileURLWithPath: dir)
        try FileManager.default.createDirectory(at: folder, withIntermediateDirectories: true)

        for (language, locale) in Self.locales {
            let app = XCUIApplication()
            app.launchArguments = ["-MeowNoAds", "-AppleLanguages", "(\(language))", "-AppleLocale", locale]
            app.launch()

            let tipButton = app.buttons["tipButton"]
            XCTAssertTrue(tipButton.waitForExistence(timeout: 20), "\(language): the toolbar did not appear")
            sleep(2)
            try save(app, folder, "\(language)-1-grid")

            let cat = app.buttons["cat-4"]
            XCTAssertTrue(cat.waitForExistence(timeout: 10), "\(language): the grid did not appear")
            cat.tap()
            sleep(1)
            try save(app, folder, "\(language)-2-selected")

            app.terminate()
        }
    }

    private func save(_ app: XCUIApplication, _ folder: URL, _ name: String) throws {
        let data = XCUIScreen.main.screenshot().pngRepresentation
        try data.write(to: folder.appendingPathComponent("\(name).png"))
    }
}

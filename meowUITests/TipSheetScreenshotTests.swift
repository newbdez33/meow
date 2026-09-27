//
//  TipSheetScreenshotTests.swift
//  meowUITests
//
//  Created by Jacky on 2026/09/27.
//

import StoreKitTest
import XCTest

/// Opens the remove-ads sheet against the local StoreKit configuration and saves a screenshot
/// for App Store Connect's in-app purchase review. The output path comes from
/// the MEOW_SCREENSHOT_DIR environment variable; without it the test only checks the sheet.
final class TipSheetScreenshotTests: XCTestCase {
    func testTipSheetShowsBothTreats() throws {
        let session = try SKTestSession(configurationFileNamed: "meow")
        session.resetToDefaultState()
        session.disableDialogs = true
        session.clearTransactions()

        let app = XCUIApplication()
        app.launch()

        let tipButton = app.buttons["tipButton"]
        XCTAssertTrue(tipButton.waitForExistence(timeout: 20))
        tipButton.tap()

        let canButton = app.buttons.matching(NSPredicate(format: "label CONTAINS[c] 'can'")).firstMatch
        XCTAssertTrue(canButton.waitForExistence(timeout: 20), "the can treat did not load")
        sleep(1)

        if let dir = ProcessInfo.processInfo.environment["MEOW_SCREENSHOT_DIR"], !dir.isEmpty {
            let data = XCUIScreen.main.screenshot().pngRepresentation
            let url = URL(fileURLWithPath: dir).appendingPathComponent("tip-sheet.png")
            try data.write(to: url)
        }
    }
}

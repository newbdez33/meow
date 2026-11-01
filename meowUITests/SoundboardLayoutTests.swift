import StoreKitTest
import XCTest

final class SoundboardLayoutTests: XCTestCase {
    override func tearDown() {
        XCUIDevice.shared.orientation = .portrait
        super.tearDown()
    }

    func testSelectionAndReplaySurviveRotation() throws {
        let app = launch()
        let cat = app.buttons["cat-4"]
        XCTAssertTrue(cat.waitForExistence(timeout: 15))
        cat.tap()
        XCTAssertTrue(cat.isSelected)

        for orientation in [UIDeviceOrientation.landscapeLeft, .portrait] {
            XCUIDevice.shared.orientation = orientation
            let replay = app.buttons["replaySound"]
            XCTAssertTrue(replay.waitForExistence(timeout: 5))
            XCTAssertTrue(replay.isHittable)
            replay.tap()
            XCTAssertEqual(app.staticTexts["selectedSoundName"].label, "I'm cute")
        }
    }

    func testLastCatRemainsReachableWithLargeText() throws {
        let app = launch(largeText: true)
        let grid = app.scrollViews["soundGrid"]
        XCTAssertTrue(grid.waitForExistence(timeout: 15))
        let last = app.buttons["cat-26"]
        for _ in 0..<20 {
            if last.exists && last.isHittable { break }
            grid.swipeUp()
        }
        XCTAssertTrue(last.isHittable)
        last.tap()
        XCTAssertTrue(last.isSelected)

        XCUIDevice.shared.orientation = .landscapeLeft
        let replay = app.buttons["replaySound"]
        for _ in 0..<10 {
            if replay.exists && replay.isHittable { break }
            grid.swipeUp()
        }
        XCTAssertTrue(replay.isHittable)
        XCTAssertEqual(app.staticTexts["selectedSoundName"].label, "God bless you")
    }

    func testScrollPositionSurvivesRotation() throws {
        let app = launch()
        let grid = app.scrollViews["soundGrid"]
        XCTAssertTrue(grid.waitForExistence(timeout: 15))
        let last = app.buttons["cat-26"]
        for _ in 0..<12 {
            if last.exists && last.isHittable { break }
            grid.swipeUp()
        }
        XCTAssertTrue(last.isHittable)
        last.tap()

        let anchor = try XCTUnwrap(app.buttons.matching(NSPredicate(format: "identifier BEGINSWITH 'cat-'"))
            .allElementsBoundByIndex.first { $0.isHittable })
        let visibleCat = app.buttons[anchor.identifier]

        for orientation in [UIDeviceOrientation.landscapeLeft, .portrait] {
            XCUIDevice.shared.orientation = orientation
            XCTAssertTrue(visibleCat.waitForExistence(timeout: 5))
            XCTAssertTrue(visibleCat.isHittable, "The visible grid item must stay in view after resizing")
        }
    }

    func testSupportSheetKeepsSelectionAfterRotation() throws {
        let session = try SKTestSession(configurationFileNamed: "meow")
        session.resetToDefaultState()
        session.disableDialogs = true
        session.clearTransactions()

        let app = launch()
        let cat = app.buttons["cat-0"]
        XCTAssertTrue(cat.waitForExistence(timeout: 15))
        cat.tap()
        app.buttons["tipButton"].tap()
        let restore = app.buttons["Restore purchase"]
        XCTAssertTrue(restore.waitForExistence(timeout: 10))
        XCUIDevice.shared.orientation = .landscapeLeft
        XCTAssertTrue(restore.waitForExistence(timeout: 5))
        app.buttons["closeTip"].tap()
        XCTAssertEqual(app.staticTexts["selectedSoundName"].label, "I'm Good")
    }

    private func launch(largeText: Bool = false) -> XCUIApplication {
        continueAfterFailure = false
        XCUIDevice.shared.orientation = .portrait
        let app = XCUIApplication()
        app.launchArguments = ["-MeowNoAds", "-AppleLanguages", "(en)", "-AppleLocale", "en_US"]
        if largeText {
            app.launchArguments += ["-UIPreferredContentSizeCategoryName", "UICTContentSizeCategoryAccessibilityXXXL"]
        }
        app.launch()
        return app
    }
}

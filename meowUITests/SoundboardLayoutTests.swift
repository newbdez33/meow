import StoreKitTest
import XCTest

final class SoundboardLayoutTests: XCTestCase {
    override func tearDown() {
        XCUIDevice.shared.orientation = .portrait
        super.tearDown()
    }

    func testExpandedLayoutShowsEveryCatWithoutScrolling() throws {
        guard UIDevice.current.userInterfaceIdiom == .pad else {
            throw XCTSkip("The expanded layout requires a regular-width window")
        }
        let app = launch()
        XCTAssertTrue(app.buttons["cat-0"].waitForExistence(timeout: 15), app.debugDescription)
        for orientation in [UIDeviceOrientation.portrait, .landscapeLeft] {
            XCUIDevice.shared.orientation = orientation
            let ready = XCTNSPredicateExpectation(predicate: NSPredicate { _, _ in
                let frame = app.windows.firstMatch.frame
                return (frame.width > frame.height) == (orientation == .landscapeLeft)
                    && app.otherElements["fittedLeadingGrid"].exists
                    && app.otherElements["fittedTrailingGrid"].exists
            }, object: app)
            let result = XCTWaiter.wait(for: [ready], timeout: 5)
            let capture = XCTAttachment(screenshot: app.screenshot())
            capture.name = "Expanded soundboard \(orientation.rawValue)"
            capture.lifetime = .keepAlways
            add(capture)
            XCTAssertEqual(result, .completed,
                           "Both panes must use the fitted layout after rotation")
            for id in 0..<27 {
                let cat = app.buttons["cat-\(id)"]
                XCTAssertTrue(cat.isHittable, "Cat \(id) must fit without scrolling")
                XCTAssertTrue(app.windows.firstMatch.frame.contains(cat.frame),
                              "Cat \(id) must stay inside the window")
            }
            if orientation == .landscapeLeft {
                for row in 0..<4 {
                    let leading = app.buttons["cat-\(row * 3)"].frame
                    let trailing = app.buttons["cat-\(15 + row * 3)"].frame
                    XCTAssertEqual(leading.minY, trailing.minY, accuracy: 1)
                    XCTAssertEqual(leading.height, trailing.height, accuracy: 1,
                                   "The ad row must stay reserved when ads are disabled")
                }
            }
            for id in [14, 26] {
                let cat = app.buttons["cat-\(id)"]
                cat.tap()
                XCTAssertTrue(cat.isSelected)
                cat.tap()
                XCTAssertTrue(cat.isSelected)
            }
        }
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
            if replay.exists {
                XCTAssertTrue(waitForHittable(replay))
                replay.tap()
                XCTAssertEqual(app.staticTexts["selectedSoundName"].label, "I'm cute")
            } else {
                XCTAssertTrue(waitForHittable(cat))
                cat.tap()
            }
            XCTAssertTrue(cat.isSelected)
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
        guard UIDevice.current.userInterfaceIdiom != .pad else {
            throw XCTSkip("The expanded grid does not scroll at standard text size")
        }
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
        XCTAssertTrue(cat.isSelected)
    }

    private func waitForHittable(_ element: XCUIElement) -> Bool {
        let ready = XCTNSPredicateExpectation(predicate: NSPredicate(format: "hittable == true"), object: element)
        return XCTWaiter.wait(for: [ready], timeout: 5) == .completed
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

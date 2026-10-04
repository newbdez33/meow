import XCTest

/// Opt in on a disposable simulator because this test changes its Home Screen.
final class WidgetLauncherTests: XCTestCase {
    func testWidgetStartsTheAppInTheBackground() throws {
        guard ProcessInfo.processInfo.environment["MEOW_WIDGET_LAUNCHER_TEST"] == "1" else {
            throw XCTSkip("Set MEOW_WIDGET_LAUNCHER_TEST=1 on a disposable simulator")
        }
        continueAfterFailure = false
        let app = XCUIApplication()
        app.launchArguments = ["-MeowNoAds", "-AppleLanguages", "(en)", "-AppleLocale", "en_US"]
        app.launch()
        XCUIDevice.shared.press(.home)
        XCUIDevice.shared.press(.home)
        let home = XCUIApplication(bundleIdentifier: "com.apple.springboard")
        func homeIcon() -> XCUIElement? {
            home.icons.matching(identifier: "Meow").allElementsBoundByIndex
                .filter { $0.isHittable && $0.frame.maxY < home.frame.height * 0.8 }
                .min { $0.frame.minY < $1.frame.minY }
        }
        var play = home.buttons.matching(NSPredicate(format: "label IN %@", ["Play Whispering", "Play I'm Good"])).firstMatch
        func playPoint() -> XCUICoordinate {
            if let value = ProcessInfo.processInfo.environment["MEOW_WIDGET_POINT"] {
                let coordinates = value.split(separator: ",").compactMap { Double($0) }
                if coordinates.count == 2 {
                    return home.coordinate(withNormalizedOffset: CGVector(dx: coordinates[0], dy: coordinates[1]))
                }
            }
            return play.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.5))
        }
        for _ in 0..<3 {
            if play.exists || homeIcon() != nil { break }
            home.swipeLeft()
        }
        if !play.exists {
            let icon = try XCTUnwrap(homeIcon(), "Meow must be on a Home Screen page")
            icon.press(forDuration: 1.5)
            let small = home.buttons["Small widget"]
            XCTAssertTrue(small.waitForExistence(timeout: 10), home.debugDescription)
            small.tap()
            if home.buttons["Done"].waitForExistence(timeout: 3) { home.buttons["Done"].tap() }
        }
        XCTAssertTrue(play.waitForExistence(timeout: 20))
        if let appearance = ProcessInfo.processInfo.environment["MEOW_WIDGET_APPEARANCE"] {
            play.press(forDuration: 1.5)
            let editHome = home.buttons["Edit Home Screen"]
            XCTAssertTrue(editHome.waitForExistence(timeout: 5))
            editHome.tap()
            let edit = home.buttons["Edit"]
            XCTAssertTrue(edit.waitForExistence(timeout: 5))
            edit.tap()
            let customize = home.buttons["Customize"]
            XCTAssertTrue(customize.waitForExistence(timeout: 5))
            customize.tap()
            let mode = home.buttons[appearance]
            XCTAssertTrue(mode.waitForExistence(timeout: 5))
            mode.tap()
            home.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.15)).tap()
            if home.buttons["Done"].waitForExistence(timeout: 3) { home.buttons["Done"].tap() }
        }
        if ProcessInfo.processInfo.environment["MEOW_WIDGET_CONFIGURE"] == "1" {
            playPoint().press(forDuration: 1.5)
            let editWidget = home.buttons["Edit Widget"]
            XCTAssertTrue(editWidget.waitForExistence(timeout: 5))
            editWidget.tap()
            let sound = home.buttons.matching(NSPredicate(format: "label IN %@", ["Whispering", "I'm Good"])).firstMatch
            XCTAssertTrue(sound.waitForExistence(timeout: 30), home.debugDescription)
            sound.tap()
            let choice = home.buttons["I'm Good"].firstMatch
            XCTAssertTrue(choice.waitForExistence(timeout: 10), home.debugDescription)
            choice.tap()
            let character = home.buttons.matching(NSPredicate(format: "label IN %@", ["Smug", "Spicy"])).firstMatch
            XCTAssertTrue(character.waitForExistence(timeout: 10), home.debugDescription)
            character.tap()
            let spicy = home.buttons["Spicy"].firstMatch
            XCTAssertTrue(spicy.waitForExistence(timeout: 10), home.debugDescription)
            spicy.tap()
            if let path = ProcessInfo.processInfo.environment["MEOW_WIDGET_CAPTURE_DIR"] {
                sleep(2)
                try XCUIScreen.main.screenshot().pngRepresentation.write(to: URL(fileURLWithPath: path).appendingPathComponent("ios-configuration.png"))
            }
            home.coordinate(withNormalizedOffset: CGVector(dx: 0.5, dy: 0.75)).tap()
            play = home.buttons["Play I'm Good"]
            XCTAssertTrue(play.waitForExistence(timeout: 20))
        }
        for _ in 0..<3 {
            playPoint().tap()
            XCTAssertNotEqual(app.state, .runningForeground, "Warm widget playback must stay on Home Screen")
            sleep(2)
        }
        for _ in 0..<3 {
            app.terminate()
            XCTAssertEqual(app.state, .notRunning)
            playPoint().tap()
            let launched = XCTNSPredicateExpectation(predicate: NSPredicate { _, _ in
                app.state != .notRunning
            }, object: nil)
            XCTAssertEqual(XCTWaiter.wait(for: [launched], timeout: 5), .completed)
            XCTAssertNotEqual(app.state, .runningForeground)
            sleep(2)
        }
        if let path = ProcessInfo.processInfo.environment["MEOW_WIDGET_CAPTURE_DIR"] {
            try XCUIScreen.main.screenshot().pngRepresentation.write(to: URL(fileURLWithPath: path).appendingPathComponent("ios-home.png"))
        }
        let shot = XCTAttachment(screenshot: XCUIScreen.main.screenshot())
        shot.name = "Widget playback on Home Screen"
        shot.lifetime = .keepAlways
        add(shot)
    }
}

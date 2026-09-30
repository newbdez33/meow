import XCTest

/// Opt-in test for the real consent flow. Requires the app to be installed,
/// an English device, network access, and tracking requests enabled.
/// Run with TEST_RUNNER_MEOW_TRACKING_REVIEW=1 and
/// -only-testing:meowUITests/TrackingReviewTests. For a manual icon tap during
/// recording, also set TEST_RUNNER_MEOW_REVIEW_MANUAL_LAUNCH=1.
final class TrackingReviewTests: XCTestCase {
    func testTrackingPromptAfterReset() throws {
        guard ProcessInfo.processInfo.environment["MEOW_TRACKING_REVIEW"] == "1" else {
            throw XCTSkip("MEOW_TRACKING_REVIEW is not set")
        }
        continueAfterFailure = false

        let app = XCUIApplication()
        let springboard = XCUIApplication(bundleIdentifier: "com.apple.springboard")
        let decline = springboard.buttons["Ask App Not to Track"]
        // Clear a permission request left open by an interrupted recording.
        if decline.exists {
            decline.tap()
        }
        app.terminate()
        app.resetAuthorizationStatus(for: .userTracking)
        XCUIDevice.shared.press(.home)
        if ProcessInfo.processInfo.environment["MEOW_REVIEW_MANUAL_LAUNCH"] == "1" {
            // Some physical devices omit Home Screen icons from the accessibility tree.
            print("Tracking review: authorization reset; tap the Meow icon on the device.")
            XCTAssertTrue(app.wait(for: .runningForeground, timeout: 120))
        } else {
            let icon = springboard.icons["Meow"].firstMatch
            for _ in 0..<10 {
                if icon.isHittable { break }
                springboard.swipeLeft()
            }
            XCTAssertTrue(icon.isHittable, "The Meow icon is not visible on the Home Screen")
            print("Tracking review: authorization reset; tapping the icon in 10 seconds.")
            sleep(10)
            icon.tap()
            XCTAssertTrue(app.wait(for: .runningForeground, timeout: 15))
        }

        XCTAssertTrue(decline.waitForExistence(timeout: 40), "The ATT permission request did not appear")
        let prompt = XCTAttachment(screenshot: XCUIScreen.main.screenshot())
        prompt.name = "ATT permission request"
        prompt.lifetime = .keepAlways
        add(prompt)

        sleep(5)
        decline.tap()
        let cat = app.buttons["cat-4"]
        XCTAssertTrue(cat.waitForExistence(timeout: 10))
        cat.tap()
        sleep(5)
        let result = XCTAttachment(screenshot: XCUIScreen.main.screenshot())
        result.name = "App remains usable after declining tracking"
        result.lifetime = .keepAlways
        add(result)
    }
}

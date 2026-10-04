import AVFoundation
import XCTest
@testable import meow

@MainActor
final class WidgetTests: XCTestCase {
    override func tearDown() async throws { await Sounds.shared.stop() }

    func testCompletionReleasesTheAudioSession() async throws {
        try await Sounds.shared.play(soundID: "m_004")
        for _ in 0..<150 {
            if Sounds.shared.soundID == nil { break }
            try await Task.sleep(nanoseconds: 100_000_000)
        }
        XCTAssertNil(Sounds.shared.soundID)
    }

    func testUnknownIDsFallBackIndependently() {
        XCTAssertEqual(WidgetSelection(soundID: "removed", characterID: "ghost"), WidgetSelection(soundID: "m_004", characterID: "ghost"))
        XCTAssertEqual(WidgetSelection(soundID: "m_027", characterID: "removed"), WidgetSelection(soundID: "m_027", characterID: "ginger"))
    }

    func testEverySoundAndCharacterIsBundled() throws {
        XCTAssertEqual(WidgetSelection.soundIDs.count, 27)
        XCTAssertEqual(WidgetSelection.characterIDs.count, 7)
        for id in WidgetSelection.soundIDs {
            let url = try XCTUnwrap(Bundle.main.url(forResource: id, withExtension: "mp3"))
            XCTAssertGreaterThan(try AVAudioPlayer(contentsOf: url).duration, 0)
            let key = WidgetSelection(soundID: id).captionKey
            for locale in ["en", "zh-Hans", "ja"] {
                let path = try XCTUnwrap(Bundle.main.path(forResource: locale, ofType: "lproj"))
                XCTAssertNotEqual(Bundle(path: path)?.localizedString(forKey: key, value: nil, table: nil), key)
            }
        }
        for id in WidgetSelection.characterIDs {
            XCTAssertNotNil(UIImage(named: "widget_" + id))
            XCTAssertNotNil(UIImage(named: "widget_" + id + "_tinted"))
        }
    }

    func testConfigurationQueriesUseStableIDs() async throws {
        let sounds = try await SoundQuery().suggestedEntities()
        let characters = try await CharacterQuery().suggestedEntities()
        XCTAssertEqual(sounds.map(\.id), WidgetSelection.soundIDs)
        XCTAssertEqual(characters.map(\.id), WidgetSelection.characterIDs)
        let restored = try await SoundQuery().entities(for: ["m_027", "old"])
        XCTAssertEqual(restored.map(\.id), ["m_027", "old"])
        let configuration = MeowWidgetConfiguration()
        configuration.sound = restored[1]
        configuration.character = WidgetCharacter(id: "ghost")
        XCTAssertEqual(configuration.selection, WidgetSelection(soundID: "m_004", characterID: "ghost"))
    }

    func testIntentReplacesForegroundPlaybackAndStopsOnInterruption() async throws {
        try await Sounds.shared.play(soundID: "m_001")
        _ = try await PlayWidgetSound(soundID: "m_004").perform()
        XCTAssertEqual(Sounds.shared.soundID, "m_004")
        NotificationCenter.default.post(name: AVAudioSession.interruptionNotification, object: nil,
            userInfo: [AVAudioSessionInterruptionTypeKey: AVAudioSession.InterruptionType.began.rawValue])
        try await Task.sleep(nanoseconds: 100_000_000)
        XCTAssertNil(Sounds.shared.soundID)
    }

    func testFailedPlaybackDoesNotRetainAnActiveSound() async throws {
        try await Sounds.shared.play(soundID: "m_004")
        do {
            try await Sounds.shared.play(soundID: "missing")
            XCTFail("A missing sound must fail")
        } catch { }
        XCTAssertNil(Sounds.shared.soundID)
    }
}

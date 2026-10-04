import Foundation

struct WidgetSelection: Equatable {
    static let soundIDs = (1...27).map { String(format: "m_%03d", $0) }
    static let characterIDs = ["ginger", "black", "gray", "sleepy", "pumpkin", "ghost", "classic"]

    let soundID: String
    let characterID: String

    init(soundID: String? = nil, characterID: String? = nil) {
        self.soundID = soundID.flatMap { Self.soundIDs.contains($0) ? $0 : nil } ?? "m_004"
        self.characterID = characterID.flatMap { Self.characterIDs.contains($0) ? $0 : nil } ?? "ginger"
    }

    var captionKey: String { "t" + soundID.suffix(2) }
    var imageName: String { "widget_" + characterID }
}

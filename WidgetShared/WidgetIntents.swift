import AppIntents

@available(iOS 17.0, *)
struct WidgetSound: AppEntity {
    static let typeDisplayRepresentation = TypeDisplayRepresentation(name: "widget_sound")
    static let defaultQuery = SoundQuery()
    let id: String

    var displayRepresentation: DisplayRepresentation {
        DisplayRepresentation(title: LocalizedStringResource(stringLiteral: WidgetSelection(soundID: id).captionKey))
    }
}

@available(iOS 17.0, *)
struct SoundQuery: EntityQuery {
    func entities(for identifiers: [String]) async throws -> [WidgetSound] {
        // Preserve query identity during restore; the selection resolves stale IDs.
        identifiers.map { WidgetSound(id: $0) }
    }

    func suggestedEntities() async throws -> [WidgetSound] {
        WidgetSelection.soundIDs.map { WidgetSound(id: $0) }
    }

    func defaultResult() async -> WidgetSound? { WidgetSound(id: "m_004") }
}

@available(iOS 17.0, *)
struct WidgetCharacter: AppEntity {
    static let typeDisplayRepresentation = TypeDisplayRepresentation(name: "widget_character")
    static let defaultQuery = CharacterQuery()
    let id: String

    var displayRepresentation: DisplayRepresentation {
        let name = WidgetSelection(characterID: id).imageName
        return DisplayRepresentation(title: LocalizedStringResource(stringLiteral: name), image: .init(named: name))
    }
}

@available(iOS 17.0, *)
struct CharacterQuery: EntityQuery {
    func entities(for identifiers: [String]) async throws -> [WidgetCharacter] {
        identifiers.map { WidgetCharacter(id: $0) }
    }

    func suggestedEntities() async throws -> [WidgetCharacter] {
        WidgetSelection.characterIDs.map { WidgetCharacter(id: $0) }
    }

    func defaultResult() async -> WidgetCharacter? { WidgetCharacter(id: "ginger") }
}

@available(iOS 17.0, *)
struct MeowWidgetConfiguration: WidgetConfigurationIntent {
    static let title: LocalizedStringResource = "widget_title"
    static let description = IntentDescription("widget_description")

    @Parameter(title: "widget_sound") var sound: WidgetSound?
    @Parameter(title: "widget_character") var character: WidgetCharacter?

    var selection: WidgetSelection { WidgetSelection(soundID: sound?.id, characterID: character?.id) }
}

@available(iOS 17.0, *)
struct PlayWidgetSound: AudioPlaybackIntent {
    static let title: LocalizedStringResource = "widget_title"
    static let openAppWhenRun = false
    static let isDiscoverable = false

    @Parameter(title: "widget_sound", default: "m_004") var soundID: String

    init() {}
    init(soundID: String) { self.soundID = soundID }

    func perform() async throws -> some IntentResult {
        // AudioPlaybackIntent routes this action to the app, which owns the audio resources.
        #if !WIDGET_EXTENSION
        try await Sounds.shared.play(soundID: WidgetSelection(soundID: soundID).soundID)
        #endif
        return .result()
    }
}

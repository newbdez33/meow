import SwiftUI
import WidgetKit

struct MeowEntry: TimelineEntry {
    let date: Date
    let selection: WidgetSelection
}

struct MeowProvider: AppIntentTimelineProvider {
    func placeholder(in context: Context) -> MeowEntry {
        MeowEntry(date: .now, selection: WidgetSelection())
    }

    func snapshot(for configuration: MeowWidgetConfiguration, in context: Context) async -> MeowEntry {
        MeowEntry(date: .now, selection: configuration.selection)
    }

    func timeline(for configuration: MeowWidgetConfiguration, in context: Context) async -> Timeline<MeowEntry> {
        Timeline(entries: [MeowEntry(date: .now, selection: configuration.selection)], policy: .never)
    }
}

struct MeowWidgetView: View {
    let entry: MeowEntry
    @Environment(\.widgetRenderingMode) private var renderingMode

    var body: some View {
        Button(intent: PlayWidgetSound(soundID: entry.selection.soundID)) {
            ZStack(alignment: .bottom) {
                artwork
                    .scaledToFill()
                    .clipped()
                Text(LocalizedStringKey(entry.selection.captionKey))
                    .font(.system(.caption, design: .rounded, weight: .semibold))
                    .lineLimit(1)
                    .truncationMode(.tail)
                    .padding(.horizontal, 12)
                    .padding(.vertical, 7)
                    .foregroundStyle(renderingMode == .fullColor ? Color.black.opacity(0.8) : Color.primary)
                    .background(.white.opacity(0.85), in: Capsule())
                    .padding(12)
            }
            .frame(maxWidth: .infinity, maxHeight: .infinity)
            .contentShape(Rectangle())
        }
        .buttonStyle(.plain)
        .accessibilityLabel(String(format: NSLocalizedString("widget_play", comment: ""), NSLocalizedString(entry.selection.captionKey, comment: "")))
        .containerBackground(for: .widget) { Color.white }
    }

    private var artwork: some View {
        // Desaturated WidgetKit images can swallow their enclosing button's tap.
        // Use a template mask for system tinting so the playback target stays intact.
        Image(entry.selection.imageName + (renderingMode == .accented ? "_tinted" : ""))
            .resizable()
            .widgetAccentable(renderingMode == .accented)
    }
}

@main
struct MeowWidget: Widget {
    let kind = "MeowWidget"

    var body: some WidgetConfiguration {
        AppIntentConfiguration(kind: kind, intent: MeowWidgetConfiguration.self, provider: MeowProvider()) { entry in
            MeowWidgetView(entry: entry)
        }
        .configurationDisplayName("widget_title")
        .description("widget_description")
        .supportedFamilies([.systemSmall])
        .contentMarginsDisabled()
    }
}

#Preview(as: .systemSmall) {
    MeowWidget()
} timeline: {
    MeowEntry(date: .now, selection: WidgetSelection())
    MeowEntry(date: .now, selection: WidgetSelection(soundID: "m_002", characterID: "black"))
}

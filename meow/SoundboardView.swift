import SwiftUI

struct SoundboardView: View {
    @Environment(\.horizontalSizeClass) private var horizontalSizeClass
    @Environment(\.verticalSizeClass) private var verticalSizeClass
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize
    @State private var selectedSound: CatSound?
    @State private var visibleSoundID: Int?
    @State private var leadingSoundID: Int?
    @State private var trailingSoundID: Int?

    private var isExpanded: Bool {
        horizontalSizeClass == .regular && verticalSizeClass != .compact
    }

    var body: some View {
        GeometryReader { geometry in
            if #available(iOS 27.1, *),
               let division = geometry.reservedRegions(kind: .division, options: .includeInactive,
                                                       layoutDirectionBehavior: .fixed).first {
                duoContent(in: geometry, division: division)
            } else if isExpanded {
                expanded
            } else {
                SoundGallery(pane: .all, stacked: false, selectedSound: selectedSound,
                             visibleSoundID: $visibleSoundID, play: play)
            }
        }
        .background(Color(uiColor: .systemBackground))
        .onChange(of: isExpanded) { expanded in
            if !expanded {
                visibleSoundID = selectedSound?.id ?? trailingSoundID ?? leadingSoundID
            }
        }
    }

    @available(iOS 27.1, *)
    private func duoContent(in geometry: GeometryProxy, division: ReservedRegion) -> some View {
        let stacked = division.frame.width > division.frame.height
        let showsCat = !dynamicTypeSize.isAccessibilitySize && geometry.size.width >= 400
        let fullPortrait = !division.isActive && stacked
        return ZStack {
            if division.isActive {
                ArrangementView {
                    pane(.leading, stacked: stacked, visibleSoundID: $leadingSoundID, duo: true)
                        .padding(.bottom, stacked && showsCat ? 20 : 0)
                } secondary: {
                    pane(.trailing, stacked: stacked, visibleSoundID: $trailingSoundID, duo: true)
                        .padding(.top, stacked && showsCat ? 20 : 0)
                }
                .arrangementViewStyle(.split)
            } else {
                SoundGallery(pane: .all, stacked: geometry.size.height > geometry.size.width,
                             selectedSound: selectedSound, visibleSoundID: $visibleSoundID,
                             play: play, duo: true)
            }
        }
        .overlay {
            if showsCat {
                HingeCatView()
                    .frame(width: 192, height: fullPortrait ? 64 : stacked ? 80 : 96)
                    .position(x: fullPortrait ? geometry.size.width - 96
                                : stacked ? geometry.size.width / 2 : division.frame.midX,
                              y: fullPortrait ? 30 : division.isActive && stacked ? division.frame.midY
                                : 48 - geometry.safeAreaInsets.top)
            }
        }
        .onChange(of: division.isActive) { split in
            if !split { visibleSoundID = selectedSound?.id ?? trailingSoundID ?? leadingSoundID }
        }
    }

    private var expanded: some View {
        GeometryReader { geometry in
            let stacked = geometry.size.height > geometry.size.width
            if #available(iOS 27.1, *) {
                ArrangementView {
                    pane(.leading, stacked: stacked, visibleSoundID: $leadingSoundID)
                } secondary: {
                    pane(.trailing, stacked: stacked, visibleSoundID: $trailingSoundID)
                }
                .arrangementViewStyle(.split)
            } else if stacked {
                VStack(spacing: 0) {
                    pane(.leading, stacked: true, visibleSoundID: $leadingSoundID)
                    pane(.trailing, stacked: true, visibleSoundID: $trailingSoundID)
                }
            } else {
                HStack(spacing: 0) {
                    pane(.leading, stacked: false, visibleSoundID: $leadingSoundID)
                    pane(.trailing, stacked: false, visibleSoundID: $trailingSoundID)
                }
            }
        }
    }

    private func pane(_ pane: SoundPane, stacked: Bool, visibleSoundID: Binding<Int?>,
                      duo: Bool = false) -> some View {
        SoundGallery(pane: pane, stacked: stacked, selectedSound: selectedSound,
                     visibleSoundID: visibleSoundID, play: play, duo: duo)
            .frame(maxWidth: .infinity, maxHeight: .infinity)
    }

    private func play(_ sound: CatSound) {
        selectedSound = sound
        Sounds.playSounds(soundfile: sound.soundID)
    }
}

private enum SoundPane {
    case all, leading, trailing

    var sounds: [CatSound] {
        switch self {
        case .all: return CatSound.all
        case .leading: return Array(CatSound.all.prefix(15))
        case .trailing: return Array(CatSound.all.dropFirst(15))
        }
    }

    var title: LocalizedStringKey {
        self == .trailing ? "soundboard_more" : "soundboard_title"
    }

    var scrollIdentifier: String {
        self == .leading ? "leadingSoundGrid" : "soundGrid"
    }
}

private struct SoundGallery: View {
    let pane: SoundPane
    let stacked: Bool
    let selectedSound: CatSound?
    @Binding var visibleSoundID: Int?
    let play: (CatSound) -> Void
    var duo = false
    @ScaledMetric(relativeTo: .caption) private var minimumCellWidth = 104.0
    @ScaledMetric(relativeTo: .caption) private var fittedCellWidth = 100.0
    @ScaledMetric(relativeTo: .caption) private var fittedRowHeight = 96.0
    @ScaledMetric(relativeTo: .caption) private var stackedRowHeight = 88.0
    @ScaledMetric(relativeTo: .caption) private var duoCellWidth = 76.0
    @ScaledMetric(relativeTo: .caption) private var duoStackedRowHeight = 76.0
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    var body: some View {
        GeometryReader { geometry in
            if #available(iOS 16.0, *), (pane != .all || duo),
               !dynamicTypeSize.isAccessibilitySize, geometry.size.width >= fittedMinimumWidth + 32 {
                // Measure height at the available width so labels can wrap to two lines.
                ViewThatFits(in: .vertical) {
                    fittedContent
                    scrollingGallery
                }
            } else {
                scrollingGallery
            }
        }
    }

    private var heading: some View {
        VStack(alignment: .leading, spacing: 4) {
            HStack(alignment: .firstTextBaseline) {
                Text(pane.title)
                    .font(.system(.title2, design: .rounded).bold())
                    .accessibilityAddTraits(.isHeader)
                Spacer(minLength: 8)
                if pane != .all, let first = pane.sounds.first, let last = pane.sounds.last {
                    Text("\(first.id + 1)–\(last.id + 1)")
                        .font(.caption2.monospacedDigit())
                        .foregroundColor(.secondary)
                        .accessibilityHidden(true)
                }
            }
            Text("soundboard_hint")
                .font(.subheadline)
                .foregroundColor(.secondary)
        }
        .fixedSize(horizontal: false, vertical: true)
    }

    private var fittedColumns: Int {
        if duo && pane == .all { return stacked ? 6 : 8 }
        return stacked ? (pane == .leading ? 5 : 4) : duo ? 4 : 3
    }

    private var fittedMinimumWidth: CGFloat {
        CGFloat(fittedColumns) * (duo ? duoCellWidth : fittedCellWidth) + CGFloat(fittedColumns - 1) * 8
    }

    private var fittedContent: some View {
        let columns = fittedColumns
        let sounds = pane.sounds
        let rows = (sounds.count + columns - 1) / columns
        let minimumHeight = stacked ? (duo ? duoStackedRowHeight : stackedRowHeight) : fittedRowHeight
        let imageHeight = stacked ? (duo ? 28.0 : 40.0) : 48.0
        return VStack(alignment: .leading, spacing: 12) {
            heading
            VStack(spacing: 8) {
                ForEach(0..<rows, id: \.self) { row in
                    HStack(spacing: 8) {
                        ForEach(0..<columns, id: \.self) { column in
                            let index = row * columns + column
                            if index < sounds.count {
                                catButton(sounds[index], fittedImageHeight: imageHeight)
                                    .frame(maxWidth: .infinity, maxHeight: .infinity)
                            } else {
                                Color.clear.frame(maxWidth: .infinity).accessibilityHidden(true)
                            }
                        }
                    }
                    .frame(minHeight: minimumHeight, maxHeight: .infinity)
                    .overlay {
                        if pane == .all, row == rows - 1 {
                            GeometryReader { geometry in
                                let remaining = columns - sounds.count % columns
                                let cellWidth = (geometry.size.width - CGFloat(columns - 1) * 8) / CGFloat(columns)
                                let adWidth = cellWidth * CGFloat(remaining) + CGFloat(remaining - 1) * 8
                                Color.clear.overlay { Banner() }
                                    .frame(width: adWidth, height: 50)
                                    .position(x: geometry.size.width - adWidth / 2, y: geometry.size.height / 2)
                                    .accessibilityIdentifier("reservedAdRow")
                            }
                        }
                    }
                }
                if pane == .trailing {
                    // Keep the last row aligned while consent, ad loading, or purchases change.
                    Color.clear
                        .overlay { Banner() }
                        .frame(maxWidth: .infinity)
                        .frame(minHeight: stacked ? 50 : minimumHeight,
                               maxHeight: stacked ? 50 : .infinity)
                        .accessibilityIdentifier("reservedAdRow")
                }
            }
            .frame(minWidth: fittedMinimumWidth)
        }
        .padding(16)
        .accessibilityElement(children: .contain)
        .accessibilityIdentifier(pane == .all ? "fittedFullGrid" : pane == .leading ? "fittedLeadingGrid" : "fittedTrailingGrid")
    }

    private var scrollingGallery: some View {
        positionedScroll
            .safeAreaInset(edge: .bottom, spacing: 0) {
                if !dynamicTypeSize.isAccessibilitySize, let sound = replaySound {
                    ReplaySound(sound: sound) { play(sound) }
                }
            }
    }

    @ViewBuilder
    private var positionedScroll: some View {
        if #available(iOS 17.0, *) {
            scrollContent
                .scrollPosition(id: Binding(
                    get: { visibleSoundID },
                    set: { if let id = $0 { visibleSoundID = id } }
                ))
        } else {
            scrollContent
        }
    }

    private var replaySound: CatSound? {
        guard let selectedSound, pane.sounds.contains(selectedSound) else { return nil }
        return selectedSound
    }

    private var scrollContent: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                heading
                if pane == .all { Banner() }
                if #available(iOS 17.0, *) {
                    grid.scrollTargetLayout()
                } else {
                    grid
                }
                if pane == .trailing { Banner() }
                if dynamicTypeSize.isAccessibilitySize, let sound = replaySound {
                    ReplaySound(sound: sound) { play(sound) }
                }
            }
            .padding(16)
        }
        .accessibilityIdentifier(pane.scrollIdentifier)
    }

    private var grid: some View {
        LazyVGrid(columns: [GridItem(.adaptive(minimum: minimumCellWidth), spacing: 12)], spacing: 12) {
            ForEach(pane.sounds) { sound in catButton(sound) }
        }
    }

    private func catButton(_ sound: CatSound, fittedImageHeight: CGFloat? = nil) -> some View {
        Button { play(sound) } label: {
            Cat(sound: sound, selected: sound == selectedSound, fittedImageHeight: fittedImageHeight)
        }
        .buttonStyle(.plain)
        .accessibilityIdentifier("cat-\(sound.id)")
        .accessibilityLabel(Text(sound.titleKey))
        .accessibilityHint(Text("soundboard_play_hint"))
        .accessibilityAddTraits(sound == selectedSound ? .isSelected : [])
    }
}

private struct ReplaySound: View {
    let sound: CatSound
    let play: () -> Void
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize
    @Environment(\.colorScheme) private var colorScheme

    var body: some View {
        Group {
            if dynamicTypeSize.isAccessibilitySize {
                VStack(alignment: .leading, spacing: 8) {
                    caption
                    replayButton
                }
            } else {
                HStack(spacing: 12) {
                    Image(sound.imageName)
                        .resizable()
                        .scaledToFit()
                        .frame(width: 44, height: 44)
                        .accessibilityHidden(true)
                    caption
                    Spacer(minLength: 0)
                    replayButton
                        .labelStyle(.iconOnly)
                }
            }
        }
        .padding(12)
        .frame(maxWidth: .infinity, alignment: .leading)
        .background(.regularMaterial)
    }

    private var caption: some View {
        Text(sound.titleKey)
            .font(.system(.subheadline, design: .rounded).weight(.semibold))
            .fixedSize(horizontal: false, vertical: true)
            .accessibilityIdentifier("selectedSoundName")
    }

    private var replayButton: some View {
        Button(action: play) {
            Label("soundboard_replay", systemImage: "arrow.counterclockwise")
                .font(.headline)
                .frame(minWidth: 44, minHeight: 44)
        }
        .buttonStyle(.bordered)
        .tint(Color(hex: colorScheme == .dark ? 0xFFA5A0 : 0xB84642))
        .accessibilityIdentifier("replaySound")
    }
}

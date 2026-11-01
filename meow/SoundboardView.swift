import SwiftUI

struct SoundboardView: View {
    @Environment(\.horizontalSizeClass) private var horizontalSizeClass
    @Environment(\.verticalSizeClass) private var verticalSizeClass
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize
    // Both values belong to the soundboard, so changing the pane layout keeps them.
    @State private var selectedSound: CatSound?
    @State private var visibleSoundID: Int?

    var body: some View {
        Group {
            if horizontalSizeClass == .regular && verticalSizeClass != .compact {
                expanded
            } else {
                gallery
            }
        }
        .background(Color(uiColor: .systemBackground))
    }

    @ViewBuilder
    private var expanded: some View {
        if #available(iOS 27.1, *) {
            ArrangementView {
                CatSpotlight(sound: selectedSound)
            } secondary: {
                gallery
            }
            .arrangementViewStyle(.split)
        } else {
            GeometryReader { geometry in
                if geometry.size.width > geometry.size.height {
                    HStack(spacing: 0) {
                        CatSpotlight(sound: selectedSound)
                        gallery
                    }
                } else {
                    VStack(spacing: 0) {
                        CatSpotlight(sound: selectedSound)
                        gallery
                    }
                }
            }
        }
    }

    private var gallery: some View {
        SoundGallery(selectedSound: selectedSound, visibleSoundID: $visibleSoundID, play: play)
            .safeAreaInset(edge: .bottom, spacing: 0) {
                if !dynamicTypeSize.isAccessibilitySize, let selectedSound {
                    ReplaySound(sound: selectedSound) { play(selectedSound) }
                }
            }
    }

    private func play(_ sound: CatSound) {
        selectedSound = sound
        Sounds.playSounds(soundfile: sound.soundID)
    }
}

private struct SoundGallery: View {
    let selectedSound: CatSound?
    @Binding var visibleSoundID: Int?
    let play: (CatSound) -> Void
    @ScaledMetric(relativeTo: .caption) private var minimumCellWidth = 104.0
    @Environment(\.dynamicTypeSize) private var dynamicTypeSize

    var body: some View {
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

    private var scrollContent: some View {
        ScrollView {
            VStack(alignment: .leading, spacing: 18) {
                VStack(alignment: .leading, spacing: 4) {
                    Text("soundboard_title")
                        .font(.system(.title2, design: .rounded).bold())
                        .accessibilityAddTraits(.isHeader)
                    Text("soundboard_hint")
                        .font(.subheadline)
                        .foregroundColor(.secondary)
                }

                Banner()

                if #available(iOS 17.0, *) {
                    grid.scrollTargetLayout()
                } else {
                    grid
                }

                // Large text must scroll with the grid instead of consuming a short pane.
                if dynamicTypeSize.isAccessibilitySize, let selectedSound {
                    ReplaySound(sound: selectedSound) { play(selectedSound) }
                }
            }
            .padding(16)
        }
        .accessibilityIdentifier("soundGrid")
    }

    private var grid: some View {
        LazyVGrid(columns: [GridItem(.adaptive(minimum: minimumCellWidth), spacing: 12)], spacing: 12) {
            ForEach(CatSound.all) { sound in
                Button { play(sound) } label: {
                    Cat(sound: sound, selected: sound == selectedSound)
                }
                .buttonStyle(.plain)
                .accessibilityIdentifier("cat-\(sound.id)")
                .accessibilityLabel(Text(sound.titleKey))
                .accessibilityHint(Text("soundboard_play_hint"))
                .accessibilityAddTraits(sound == selectedSound ? .isSelected : [])
            }
        }
    }
}

private struct CatSpotlight: View {
    let sound: CatSound?

    var body: some View {
        GeometryReader { geometry in
            ScrollView {
                VStack(spacing: 18) {
                    ZStack {
                        Circle()
                            .fill(Color("lightYellow"))
                            .frame(width: 168, height: 168)
                        Image(sound?.imageName ?? "c04")
                            .resizable()
                            .scaledToFit()
                            .frame(width: 112, height: 112)
                    }
                    .accessibilityHidden(true)

                    VStack(spacing: 8) {
                        Text(sound == nil ? "soundboard_welcome" : "soundboard_selected")
                            .font(.subheadline)
                            .foregroundColor(.secondary)
                        Text(sound?.titleKey ?? "soundboard_welcome_title")
                            .font(.system(.largeTitle, design: .rounded).bold())
                            .fixedSize(horizontal: false, vertical: true)
                        Text("soundboard_hint")
                            .font(.body)
                            .foregroundColor(.secondary)
                    }
                    .multilineTextAlignment(.center)
                }
                .padding(24)
                .frame(maxWidth: .infinity)
                .frame(minHeight: max(0, geometry.size.height))
            }
            .background(Color("lightPink"), in: RoundedRectangle(cornerRadius: 32, style: .continuous))
        }
        .padding(16)
        .accessibilityIdentifier("catSpotlight")
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

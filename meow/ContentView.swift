//
//  ContentView.swift
//  meow
//
//  Created by Jacky on 2020/07/04.
//

import SwiftUI

struct ContentView: View {
    @EnvironmentObject private var ads: AdsController
    @EnvironmentObject private var store: Store
    @Environment(\.scenePhase) private var scenePhase
    @State private var showingSheet = false
    @State private var showingTip = false
    var body: some View {
        AppNavigation {
            SoundboardView()
                .navigationTitle(Text("title"))
                .navigationBarTitleDisplayMode(.inline)
                .modifier(TrailingButtons(showingTip: $showingTip, showingShare: $showingSheet))
        }
        .tint(Color(hex: 0xE76A66))
        .sheet(isPresented: $showingTip) {
            TipSheet()
        }
        .sheet(isPresented: $showingSheet, content: {
            AppActivityView(activityItems: [
                                NSLocalizedString("title", comment: "") + " - " + NSLocalizedString("subtitle", comment: ""),
                                URL(string: "https://itunes.apple.com/app/id826362662")!,
                                UIImage(named:"AppIcon40x40") ?? UIImage()])
        })
        .task(id: scenePhase) {
            guard scenePhase == .active else { return }
            await store.load()
            guard scenePhase == .active, !Task.isCancelled, !store.isAdFree,
                  let viewController = UIApplication.shared.keyRootViewController else { return }
            await ads.start(from: viewController)
        }
    }
}

/// The navigation bar buttons: the can (remove the ads), the privacy options when required, and the share cat.
/// On iOS 26 each one gets its own glass circle; earlier systems show them side by side.
private struct TrailingButtons: ViewModifier {
    @EnvironmentObject private var ads: AdsController
    @EnvironmentObject private var store: Store
    @Binding var showingTip: Bool
    @Binding var showingShare: Bool

    func body(content: Content) -> some View {
        if #available(iOS 26.0, *) {
            content.toolbar { separated }
        } else {
            content.toolbar {
                ToolbarItem(placement: .navigationBarTrailing) {
                    HStack(spacing: 14) {
                        if !store.isAdFree { tipButton }
                        if ads.isPrivacyOptionsRequired { privacyButton }
                        shareButton
                    }
                }
            }
        }
    }

    @available(iOS 26.0, *)
    @ToolbarContentBuilder
    private var separated: some ToolbarContent {
        if !store.isAdFree {
            ToolbarItem(placement: .topBarTrailing) { tipButton }
            ToolbarSpacer(.fixed, placement: .topBarTrailing)
        }
        if ads.isPrivacyOptionsRequired {
            ToolbarItem(placement: .topBarTrailing) { privacyButton }
            ToolbarSpacer(.fixed, placement: .topBarTrailing)
        }
        ToolbarItem(placement: .topBarTrailing) { shareButton }
    }

    private var tipButton: some View {
        Button(action: { showingTip = true }, label: {
            Label {
                Text("tip_button")
            } icon: {
                Image("navCan")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 26, height: 26)
            }
        })
        .accessibilityIdentifier("tipButton")
    }

    private var privacyButton: some View {
        Button(action: {
            guard let viewController = UIApplication.shared.keyRootViewController else { return }
            Task { await ads.presentPrivacyOptions(from: viewController) }
        }, label: {
            Label("privacy_options", systemImage: "hand.raised")
        })
    }

    private var shareButton: some View {
        Button(action: { showingShare = true }, label: {
            Label {
                Text("share_button")
            } icon: {
                Image("navCat")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 26, height: 26)
            }
        })
    }
}

struct ContentView_Previews: PreviewProvider {
    static var previews: some View {
        ContentView()
            .environmentObject(AdsController())
            .environmentObject(Store())
            .preferredColorScheme(.dark)
            .previewDevice("iPhone 11")

    }
}

struct AppNavigation<Content: View>: View {
    @ViewBuilder var content: Content

    var body: some View {
        if #available(iOS 16.0, *) {
            NavigationStack { content }
        } else {
            NavigationView { content }
                .navigationViewStyle(.stack)
        }
    }
}

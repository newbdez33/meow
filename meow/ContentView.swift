//
//  ContentView.swift
//  meow
//
//  Created by Jacky on 2020/07/04.
//

import SwiftUI
import AVFoundation

struct ContentView: View {
    @EnvironmentObject private var ads: AdsController
    @EnvironmentObject private var store: Store
    @State private var showingSheet = false
    @State private var showingTip = false
    @State private var selectedIndex = -1
    var body: some View {
        NavigationView {
            VStack {
                GridStack(minCellWidth: 110, spacing: 2, numItems: 27) { index, cellWidth in
                    Button(action: {
                        Sounds.playSounds(soundfile: String(format: "m_%03d", index+1))
                        selectedIndex = index
                    }, label: {
                        Cat(dataIndex: index, selected: index == selectedIndex).frame(width: cellWidth, height: cellWidth, alignment: .center)
                    })
                    .accessibilityIdentifier("cat-\(index)")
                }
                .navigationTitle(Text("title"))
                .modifier(TrailingButtons(showingTip: $showingTip, showingShare: $showingSheet))
                .background(NavigationConfigurator { nc in
                    nc.navigationBar.barTintColor = Color(hex: 0xE76A66).uiColor()
                    nc.navigationBar.titleTextAttributes = [.foregroundColor : UIColor.white]
                })
            }
        }
        .navigationViewStyle(StackNavigationViewStyle())
        .edgesIgnoringSafeArea(.bottom)
        .sheet(isPresented: $showingTip) {
            TipSheet()
        }
        .sheet(isPresented: $showingSheet, onDismiss: {
            print("Dismiss")
        }, content: {
            AppActivityView(activityItems: [
                                NSLocalizedString("title", comment: "") + " - " + NSLocalizedString("subtitle", comment: ""),
                                URL(string: "https://itunes.apple.com/app/id826362662")!,
                                UIImage(named:"AppIcon40x40") ?? UIImage()])
        })
        .onAppear {
            do {
                try AVAudioSession.sharedInstance().setCategory(.playback, options: .mixWithOthers)
            } catch {
                print(error)
            }
        }
        .task {
            await store.load()
            guard !store.isAdFree, let viewController = UIApplication.shared.keyRootViewController else { return }
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
            Image("navCan")
                .resizable()
                .scaledToFit()
                .frame(height: 26)
        })
        .accessibilityLabel(Text("tip_button"))
        .accessibilityIdentifier("tipButton")
    }

    private var privacyButton: some View {
        Button(action: {
            guard let viewController = UIApplication.shared.keyRootViewController else { return }
            Task { await ads.presentPrivacyOptions(from: viewController) }
        }, label: {
            Image(systemName: "hand.raised")
        })
        .accessibilityLabel(Text("privacy_options"))
    }

    private var shareButton: some View {
        Button(action: { showingShare = true }, label: {
            Image("navCat")
                .resizable()
                .scaledToFit()
                .frame(height: 26)
        })
        .accessibilityLabel(Text("share_button"))
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

struct NavigationConfigurator: UIViewControllerRepresentable {
    var configure: (UINavigationController) -> Void = { _ in }

    func makeUIViewController(context: UIViewControllerRepresentableContext<NavigationConfigurator>) -> UIViewController {
        UIViewController()
    }
    func updateUIViewController(_ uiViewController: UIViewController, context: UIViewControllerRepresentableContext<NavigationConfigurator>) {
        if let nc = uiViewController.navigationController {
            self.configure(nc)
        }
    }

}

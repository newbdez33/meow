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
                }
                .navigationTitle(Text("title"))
                .navigationBarItems(trailing:
                    HStack {
                        if !store.isAdFree {
                            Button(action: {
                                showingTip = true
                            }, label: {
                                Image("navCup")
                                    .resizable()
                                    .scaledToFit()
                                    .frame(height: 26)
                            })
                            .accessibilityLabel(Text("tip_button"))
                            .sheet(isPresented: $showingTip) {
                                TipSheet()
                            }
                        }
                        if ads.isPrivacyOptionsRequired {
                            Button(action: {
                                guard let viewController = UIApplication.shared.keyRootViewController else { return }
                                Task { await ads.presentPrivacyOptions(from: viewController) }
                            }, label: {
                                Image(systemName: "hand.raised")
                            })
                            .accessibilityLabel(Text("privacy_options"))
                        }
                        Button(action: {
                            self.showingSheet = true
                        }, label: {
                            Image("navCat")
                                .resizable()
                                .scaledToFit()
                                .frame(height: 26)
                        })
                        .accessibilityLabel(Text("share_button"))
                        .sheet(isPresented: $showingSheet, onDismiss: {
                            print("Dismiss")
                        }, content: {
                            AppActivityView(activityItems: [
                                                NSLocalizedString("title", comment: "") + " - " + NSLocalizedString("subtitle", comment: ""),
                                                URL(string: "https://itunes.apple.com/app/id826362662")!,
                                                UIImage(named:"AppIcon40x40") ?? UIImage()])
                        })

                    }
                )
                .background(NavigationConfigurator { nc in
                    nc.navigationBar.barTintColor = Color(hex: 0xE76A66).uiColor()
                    nc.navigationBar.titleTextAttributes = [.foregroundColor : UIColor.white]
                })
            }
        }
        .navigationViewStyle(StackNavigationViewStyle())
        .edgesIgnoringSafeArea(.bottom)
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

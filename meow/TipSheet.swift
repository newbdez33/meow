//
//  TipSheet.swift
//  meow
//
//  Created by Jacky on 2026/09/27.
//

import StoreKit
import SwiftUI

/// The "remove the ads" page: a can for the cat first, a coffee for the author second, restore below.
struct TipSheet: View {
    /// Black, gray, orange, tabby, and the orange one with a bowl. A different one greets every visit.
    static let cats = ["c02", "c01", "c04", "c19", "c21"]
    @MainActor private static var lastCat: String?

    @EnvironmentObject private var store: Store
    @State private var cat = TipSheet.cats[0]
    @State private var isPurchasing = false
    @State private var didFail = false

    var body: some View {
        ScrollView {
            VStack(spacing: 18) {
                TipHero(cat: cat)
                    .onTapGesture { cat = Self.nextCat() }
                    .accessibilityHidden(true)
                Text("tip_title")
                    .font(.title2.bold())
                    .multilineTextAlignment(.center)
                Text("tip_body")
                    .multilineTextAlignment(.center)
                    .foregroundColor(.secondary)

                if store.isAdFree {
                    Text("tip_thanks")
                        .font(.headline)
                        .foregroundColor(Color(hex: 0xE76A66))
                } else {
                    if let can = store.can {
                        Button(action: { Task { await buy(can) } }, label: {
                            TreatLabel(icon: "tipCan",
                                       title: String(format: NSLocalizedString("tip_can", comment: ""), can.displayPrice),
                                       note: "tip_can_note")
                        })
                        .buttonStyle(.borderedProminent)
                        .tint(Color(hex: 0xE76A66))
                        .disabled(isPurchasing)
                    }
                    if let coffee = store.coffee {
                        Button(action: { Task { await buy(coffee) } }, label: {
                            TreatLabel(icon: "tipMug",
                                       title: String(format: NSLocalizedString("tip_coffee", comment: ""), coffee.displayPrice),
                                       note: nil)
                        })
                        .buttonStyle(.bordered)
                        .tint(Color(hex: 0xE76A66))
                        .disabled(isPurchasing)
                    }
                    if store.can == nil && store.coffee == nil {
                        Text("tip_unavailable")
                            .multilineTextAlignment(.center)
                            .foregroundColor(.secondary)
                    }
                    Button("tip_restore") {
                        Task { await store.restore() }
                    }
                    .disabled(isPurchasing)
                }

                if didFail {
                    Text("tip_failed")
                        .font(.footnote)
                        .foregroundColor(.red)
                        .multilineTextAlignment(.center)
                }
            }
            .padding(28)
            .padding(.top, 8)
        }
        .onAppear { cat = Self.nextCat() }
        .modifier(HalfHeightSheet())
    }

    /// A random cat that is never the one shown last time.
    @MainActor
    private static func nextCat() -> String {
        let pick = cats.filter { $0 != lastCat }.randomElement() ?? cats[0]
        lastCat = pick
        return pick
    }

    private func buy(_ product: Product) async {
        isPurchasing = true
        didFail = false
        do {
            try await store.purchase(product)
        } catch {
            didFail = true
        }
        isPurchasing = false
    }
}

/// One treat button's content: the icon in a fixed slot on the left, the text on one line next to it.
private struct TreatLabel: View {
    let icon: String
    let title: String
    let note: LocalizedStringKey?

    var body: some View {
        HStack(spacing: 12) {
            Image(icon)
                .resizable()
                .scaledToFit()
                .frame(width: 34, height: 34)
            VStack(alignment: .leading, spacing: 1) {
                Text(title)
                    .font(.headline)
                    .lineLimit(1)
                    .minimumScaleFactor(0.8)
                if let note {
                    Text(note)
                        .font(.caption)
                        .lineLimit(1)
                }
            }
            Spacer(minLength: 0)
        }
        .padding(.vertical, 4)
        .padding(.leading, 4)
    }
}

/// The mug, one of the app's cats and the open can side by side.
private struct TipHero: View {
    let cat: String

    var body: some View {
        ZStack(alignment: .bottom) {
            Ellipse()
                .fill(Color.black.opacity(0.12))
                .frame(width: 150, height: 9)
            HStack(alignment: .bottom, spacing: -4) {
                Image("tipMug")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 60, height: 60)
                    .zIndex(1)
                Image(cat)
                    .resizable()
                    .scaledToFit()
                    .frame(width: 68, height: 68)
                    .padding(.bottom, 8)
                Image("tipCan")
                    .resizable()
                    .scaledToFit()
                    .frame(width: 54, height: 54)
                    .zIndex(1)
            }
            .padding(.bottom, 3)
        }
    }
}

/// Opens at half height and can be dragged up where the system supports it; iOS 15 keeps the full-height sheet.
private struct HalfHeightSheet: ViewModifier {
    func body(content: Content) -> some View {
        if #available(iOS 16.0, *) {
            content.presentationDetents([.medium, .large])
        } else {
            content
        }
    }
}

struct TipSheet_Previews: PreviewProvider {
    static var previews: some View {
        TipSheet()
            .environmentObject(Store())
    }
}

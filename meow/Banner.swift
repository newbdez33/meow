//
//  AdMobBanner.swift
//  meow
//
//  Created by Jacky on 2020/07/06.
//

import SwiftUI
import GoogleMobileAds
import UIKit

/// Requests its ad once it is on screen; SwiftUI may build and discard a view before it is ever laid out.
private final class LazyBannerView: BannerView {
    private var hasRequestedAd = false

    override func didMoveToWindow() {
        super.didMoveToWindow()
        guard window != nil, !hasRequestedAd else { return }
        hasRequestedAd = true
        load(Request())
    }
}

private struct BannerViewContainer: UIViewRepresentable {
    let adUnitID: String
    let width: CGFloat

    func makeCoordinator() -> Coordinator {
        Coordinator()
    }

    func makeUIView(context: Context) -> BannerView {
        let banner = LazyBannerView(adSize: inlineAdaptiveBanner(width: width, maxHeight: 50))
        banner.adUnitID = adUnitID
        banner.delegate = context.coordinator
        return banner
    }

    func updateUIView(_ uiView: BannerView, context: Context) {}

    final class Coordinator: NSObject, BannerViewDelegate {
        func bannerViewDidReceiveAd(_ bannerView: BannerView) {
            print("Banner loaded")
        }

        func bannerView(_ bannerView: BannerView, didFailToReceiveAdWithError error: Error) {
            print("Banner failed: \(error.localizedDescription)")
        }
    }
}

struct Banner: View {
    @EnvironmentObject private var ads: AdsController
    @EnvironmentObject private var store: Store
    @State private var bannerWidth: CGFloat = 0

    var body: some View {
        if ads.canRequestAds && !store.isAdFree {
            GeometryReader { geometry in
                Group {
                    if bannerWidth > 0 {
                        BannerViewContainer(adUnitID: AdsController.bannerAdUnitID, width: bannerWidth)
                            .id(bannerWidth)
                    }
                }
                .task(id: geometry.size.width.rounded(.down)) {
                    // Wait for resizing to settle before requesting another ad.
                    try? await Task.sleep(nanoseconds: 300_000_000)
                    guard !Task.isCancelled else { return }
                    bannerWidth = geometry.size.width.rounded(.down)
                }
            }
            .frame(height: 50)
            .clipped()
        }
    }
}

struct Banner_Previews: PreviewProvider {
    static var previews: some View {
        Banner()
            .environmentObject(AdsController())
            .environmentObject(Store())
    }
}

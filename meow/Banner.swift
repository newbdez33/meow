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

    func makeCoordinator() -> Coordinator {
        Coordinator()
    }

    func makeUIView(context: Context) -> BannerView {
        let banner = LazyBannerView(adSize: AdSizeBanner)
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

    var body: some View {
        if ads.canRequestAds {
            HStack {
                Spacer()
                BannerViewContainer(adUnitID: AdsController.bannerAdUnitID)
                    .frame(width: AdSizeBanner.size.width, height: AdSizeBanner.size.height, alignment: .center)
                Spacer()
            }
        }
    }
}

struct Banner_Previews: PreviewProvider {
    static var previews: some View {
        Banner()
            .environmentObject(AdsController())
    }
}

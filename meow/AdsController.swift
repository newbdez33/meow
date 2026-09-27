//
//  AdsController.swift
//  meow
//
//  Created by Jacky on 2026/09/27.
//

import AppTrackingTransparency
import GoogleMobileAds
import UIKit
import UserMessagingPlatform

/// Gathers the consent Google requires, asks for tracking permission, then starts the ads SDK.
@MainActor
final class AdsController: ObservableObject {
    static let bannerAdUnitID = "ca-app-pub-1295607594822275/2001183168"

    /// True once consent allows ad requests; the banner renders only while this is true.
    @Published private(set) var canRequestAds = false
    /// True when regulations require an entry point for changing consent.
    @Published private(set) var isPrivacyOptionsRequired = false

    private var isStarting = false
    private var isSDKStarted = false

    /// Runs once per launch from the first screen. Safe to call again; later calls are ignored while one is running.
    func start(from viewController: UIViewController) async {
        guard !isStarting else { return }
        isStarting = true
        defer { isStarting = false }

        do {
            try await ConsentInformation.shared.requestConsentInfoUpdate(with: RequestParameters())
            try await ConsentForm.loadAndPresentIfRequired(from: viewController)
        } catch {
            // A failed update keeps the consent state from the previous launch, which may still allow ads.
            print("Consent update failed: \(error.localizedDescription)")
        }
        isPrivacyOptionsRequired = ConsentInformation.shared.privacyOptionsRequirementStatus == .required

        if ConsentInformation.shared.canRequestAds {
            _ = await ATTrackingManager.requestTrackingAuthorization()
            await startSDKIfNeeded()
        }
        canRequestAds = ConsentInformation.shared.canRequestAds
    }

    /// Shows the privacy options form so the user can change an earlier consent choice.
    func presentPrivacyOptions(from viewController: UIViewController) async {
        do {
            try await ConsentForm.presentPrivacyOptionsForm(from: viewController)
        } catch {
            print("Privacy options form failed: \(error.localizedDescription)")
        }
        if ConsentInformation.shared.canRequestAds {
            await startSDKIfNeeded()
        }
        canRequestAds = ConsentInformation.shared.canRequestAds
    }

    private func startSDKIfNeeded() async {
        guard !isSDKStarted else { return }
        isSDKStarted = true
        await MobileAds.shared.start()
    }
}

extension UIApplication {
    /// The view controller consent forms are presented from.
    var keyRootViewController: UIViewController? {
        connectedScenes
            .compactMap { $0 as? UIWindowScene }
            .flatMap(\.windows)
            .first(where: \.isKeyWindow)?
            .rootViewController
    }
}

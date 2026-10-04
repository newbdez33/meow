//
//  Store.swift
//  meow
//
//  Created by Jacky on 2026/09/27.
//

import StoreKit
import UIKit

/// The two one-time treats that remove the ads: a coffee for the author, or a can for the author's cat.
@MainActor
final class Store: ObservableObject {
    static let coffeeProductID = "com.salmonapps.Meow.coffee"
    static let canProductID = "com.salmonapps.Meow.can"
    private static let productIDs = [coffeeProductID, canProductID]

    /// True while a verified, unrevoked purchase of either treat exists for the signed-in Apple ID.
    @Published private(set) var isAdFree = false
    /// The products once the App Store has answered; nil while loading or offline.
    @Published private(set) var coffee: Product?
    @Published private(set) var can: Product?

    private var updates: Task<Void, Never>?

    private func observeTransactionsIfNeeded() {
        guard updates == nil else { return }
        updates = Task { [weak self] in
            for await result in Transaction.updates {
                await self?.apply(result)
            }
        }
    }

    deinit {
        updates?.cancel()
    }

    /// Reads the current entitlement and fetches both products. Call once at launch.
    func load() async {
        observeTransactionsIfNeeded()
        await refreshEntitlement()
        let products = (try? await Product.products(for: Self.productIDs)) ?? []
        coffee = products.first { $0.id == Self.coffeeProductID }
        can = products.first { $0.id == Self.canProductID }
    }

    /// Runs the App Store purchase sheet for one treat. Cancelling is not an error.
    func purchase(_ product: Product) async throws {
        let result: Product.PurchaseResult
        if #available(iOS 17.0, *), let scene = UIApplication.shared.connectedScenes.first(where: { $0.activationState == .foregroundActive }) {
            result = try await product.purchase(confirmIn: scene)
        } else {
            result = try await product.purchase()
        }
        switch result {
        case .success(let verification):
            if case .verified(let transaction) = verification {
                isAdFree = isAdFree || Self.removesAds(transaction)
                await transaction.finish()
            }
        case .pending, .userCancelled:
            break
        @unknown default:
            break
        }
    }

    /// Asks the App Store for purchases made on other devices, then re-reads the entitlement.
    func restore() async {
        try? await AppStore.sync()
        await refreshEntitlement()
    }

    private func refreshEntitlement() async {
        var adFree = false
        for await result in Transaction.currentEntitlements {
            if case .verified(let transaction) = result, Self.removesAds(transaction) {
                adFree = true
            }
        }
        isAdFree = adFree
    }

    private func apply(_ result: VerificationResult<Transaction>) async {
        guard case .verified(let transaction) = result else { return }
        if Self.productIDs.contains(transaction.productID) {
            await refreshEntitlement()
        }
        await transaction.finish()
    }

    private static func removesAds(_ transaction: Transaction) -> Bool {
        productIDs.contains(transaction.productID) && transaction.revocationDate == nil
    }
}

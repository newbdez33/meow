//
//  StoreTests.swift
//  meowTests
//
//  Created by Jacky on 2026/09/27.
//

import StoreKit
import StoreKitTest
import XCTest
@testable import meow

/// Runs against the local `meow.storekit` configuration, so no App Store account is involved.
@MainActor
final class StoreTests: XCTestCase {
    private var session: SKTestSession!

    override func setUp() async throws {
        session = try SKTestSession(configurationFileNamed: "meow")
        session.resetToDefaultState()
        session.disableDialogs = true
        session.clearTransactions()
    }

    override func tearDown() async throws {
        session.clearTransactions()
        session = nil
    }

    func testFreshInstallStillShowsAdsAndOffersBothTreats() async throws {
        let store = Store()
        await store.load()

        XCTAssertFalse(store.isAdFree)
        XCTAssertEqual(store.coffee?.id, Store.coffeeProductID)
        XCTAssertEqual(store.can?.id, Store.canProductID)
    }

    func testBuyingACoffeeRemovesAds() async throws {
        let store = Store()
        await store.load()
        let coffee = try XCTUnwrap(store.coffee)

        try await store.purchase(coffee)

        XCTAssertTrue(store.isAdFree)
    }

    func testBuyingACanRemovesAds() async throws {
        let store = Store()
        await store.load()
        let can = try XCTUnwrap(store.can)

        try await store.purchase(can)

        XCTAssertTrue(store.isAdFree)
    }

    func testEarlierCanPurchaseIsHonouredOnNextLaunch() async throws {
        _ = try await session.buyProduct(identifier: Store.canProductID)

        let store = Store()
        await store.load()

        XCTAssertTrue(store.isAdFree)
    }

    func testRestoreFindsACoffeeBoughtOnAnotherDevice() async throws {
        let store = Store()
        await store.load()
        XCTAssertFalse(store.isAdFree)

        _ = try await session.buyProduct(identifier: Store.coffeeProductID)
        await store.restore()

        XCTAssertTrue(store.isAdFree)
    }
}

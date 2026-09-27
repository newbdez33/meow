//
//  meowApp.swift
//  meow
//
//  Created by Jacky on 2020/07/04.
//

import SwiftUI

@main
struct meowApp: App {
    @StateObject private var ads = AdsController()
    @StateObject private var store = Store()

    var body: some Scene {
        WindowGroup {
            ContentView()
                .environmentObject(ads)
                .environmentObject(store)
        }
    }
}

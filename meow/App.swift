//
//  meowApp.swift
//  meow
//
//  Created by Jacky on 2020/07/04.
//

import SwiftUI
import GoogleMobileAds

@main
struct meowApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
    init () {
        GADMobileAds.sharedInstance().start(completionHandler: nil)
    }
}

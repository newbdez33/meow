//
//  meowApp.swift
//  meow
//
//  Created by Jacky on 2020/07/04.
//

import SwiftUI
import GoogleMobileAds
import AppTrackingTransparency
import AdSupport


@main
struct meowApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
    init () {
        GADMobileAds.sharedInstance().start(completionHandler: nil)
        requestIDFA()
    }
    
    func requestIDFA() {
      ATTrackingManager.requestTrackingAuthorization(completionHandler: { status in
        // Tracking authorization completed. Start loading ads here.
        // loadAd()
      })
    }
}

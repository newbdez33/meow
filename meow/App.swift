//
//  meowApp.swift
//  meow
//
//  Created by Jacky on 2020/07/04.
//

import SwiftUI
import AVFoundation

@main
struct meowApp: App {
    var body: some Scene {
        WindowGroup {
            ContentView()
        }
    }
}

class AppDelegate: NSObject, UIApplicationDelegate {
    func applicationDidFinishLaunching(_ application: UIApplication) {
        do {
            try AVAudioSession.sharedInstance().setCategory(.playback, options: .mixWithOthers)
        } catch {
            print(error)
        }
    }
}

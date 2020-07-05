//
//  Sounds.swift
//  meow
//
//  Created by Jacky on 2020/07/05.
//

import AVFoundation

class Sounds {
    static var audioPlayer:AVAudioPlayer?
    static func playSounds(soundfile: String) {
        if ( audioPlayer?.isPlaying == true ) {
            audioPlayer?.stop()
            return
        }
        if let path = Bundle.main.path(forResource: soundfile, ofType: "mp3"){
            do{
                audioPlayer = try AVAudioPlayer(contentsOf: URL(fileURLWithPath: path))
                audioPlayer?.prepareToPlay()
                audioPlayer?.play()
            }catch {
                print("Error")
                
            }
            
        }else {
            print("sound not found:\(soundfile)")
        }
    }
}

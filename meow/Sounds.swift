import AVFoundation
import OSLog

/// Owns audio for both the foreground grid and AudioPlaybackIntent in the app process.
final class Sounds: NSObject, AVAudioPlayerDelegate, @unchecked Sendable {
    static let shared = Sounds()
    private let queue = DispatchQueue(label: "com.salmonapps.Meow.audio")
    private let logger = Logger(subsystem: "com.salmonapps.Meow", category: "Playback")
    private var player: AVAudioPlayer?
    private var currentSoundID: String?
    private var sessionActive = false
    private var observers: [NSObjectProtocol] = []

    var soundID: String? { queue.sync { currentSoundID } }

    private override init() {
        super.init()
        let center = NotificationCenter.default
        observers.append(center.addObserver(forName: AVAudioSession.interruptionNotification, object: nil, queue: nil) { [weak self] note in
            guard let type = note.userInfo?[AVAudioSessionInterruptionTypeKey] as? UInt,
                  type == AVAudioSession.InterruptionType.began.rawValue else { return }
            self?.enqueueStop()
        })
        observers.append(center.addObserver(forName: AVAudioSession.routeChangeNotification, object: nil, queue: nil) { [weak self] note in
            guard let reason = note.userInfo?[AVAudioSessionRouteChangeReasonKey] as? UInt,
                  reason == AVAudioSession.RouteChangeReason.oldDeviceUnavailable.rawValue else { return }
            self?.enqueueStop()
        })
        observers.append(center.addObserver(forName: AVAudioSession.mediaServicesWereResetNotification, object: nil, queue: nil) { [weak self] _ in
            self?.enqueueStop()
        })
    }

    static func playSounds(soundfile: String) {
        shared.queue.async {
            do { try shared.start(soundID: soundfile) }
            catch { shared.logger.error("Sound playback failed: \(error.localizedDescription)") }
        }
    }

    func play(soundID: String) async throws {
        try await withCheckedThrowingContinuation { (continuation: CheckedContinuation<Void, Error>) in
            queue.async {
                do {
                    try self.start(soundID: soundID)
                    continuation.resume()
                } catch { continuation.resume(throwing: error) }
            }
        }
    }

    private func start(soundID: String) throws {
        stopPlayback(deactivate: false)
        do {
            guard WidgetSelection.soundIDs.contains(soundID),
                  let url = Bundle.main.url(forResource: soundID, withExtension: "mp3") else {
                throw PlaybackError.missingSound
            }
            if !sessionActive {
                let session = AVAudioSession.sharedInstance()
                try session.setCategory(.playback, mode: .default, options: .mixWithOthers)
                try session.setActive(true)
                sessionActive = true
            }
            let next = try AVAudioPlayer(contentsOf: url)
            next.delegate = self
            player = next
            currentSoundID = soundID
            guard next.play() else { throw PlaybackError.cannotPlay }
            logger.info("Playing \(soundID, privacy: .public)")
        } catch {
            stopPlayback()
            throw error
        }
    }

    func stop() async {
        await withCheckedContinuation { continuation in
            queue.async {
                self.stopPlayback()
                continuation.resume()
            }
        }
    }

    private func enqueueStop() { queue.async { self.stopPlayback() } }

    private func stopPlayback(deactivate: Bool = true) {
        player?.stop()
        player = nil
        currentSoundID = nil
        if deactivate && sessionActive {
            try? AVAudioSession.sharedInstance().setActive(false, options: .notifyOthersOnDeactivation)
            sessionActive = false
            logger.info("Playback stopped; audio session released")
        }
    }

    func audioPlayerDidFinishPlaying(_ player: AVAudioPlayer, successfully flag: Bool) {
        queue.async { if self.player === player { self.stopPlayback() } }
    }

    func audioPlayerDecodeErrorDidOccur(_ player: AVAudioPlayer, error: Error?) {
        queue.async { if self.player === player { self.stopPlayback() } }
    }

    enum PlaybackError: Error { case missingSound, cannotPlay }
}

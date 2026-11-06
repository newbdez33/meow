import SwiftUI

struct HingeCatProgress {
    enum State { case absent, closed, partial, full }

    private(set) var state: State = .absent
    private(set) var value = 0.0
    private var fullAngle = Double.pi

    mutating func update(state: State, radians: Double) {
        self.state = state
        switch state {
        case .absent, .closed:
            value = 0
        case .full:
            if radians.isFinite && radians > 0 { fullAngle = radians }
            value = 1
        case .partial:
            guard radians.isFinite else { return }
            value = min(1, max(0, radians / fullAngle))
        }
    }

    func frame(reduceMotion: Bool) -> Int? {
        switch state {
        case .absent, .closed: return nil
        case .full: return 50
        case .partial:
            return reduceMotion ? 0 : Int((max(0, value - 0.5) * 100).rounded())
        }
    }
}

@available(iOS 27.1, *)
struct HingeCatView: View {
    @Environment(\.accessibilityReduceMotion) private var reduceMotion
    @Environment(\.scenePhase) private var scenePhase
    @State private var progress = HingeCatProgress()

    var body: some View {
        HingeCatSprite(frame: progress.frame(reduceMotion: reduceMotion) ?? 0)
            .opacity(progress.frame(reduceMotion: reduceMotion) == nil ? 0 : 1)
            .aspectRatio(5.0 / 3.0, contentMode: .fit)
            .allowsHitTesting(false)
            .accessibilityHidden(true)
            .onHingeChange(isEnabled: scenePhase == .active) { _, context in
                guard let hinge = context.hinge else {
                    progress.update(state: .absent, radians: 0)
                    return
                }
                let state: HingeCatProgress.State
                switch hinge.status {
                case .closed: state = .closed
                case .fullyOpen: state = .full
                case .partiallyOpen: state = .partial
                default: state = .absent
                }
                progress.update(state: state, radians: hinge.angle.radians)
            }
    }
}

private struct HingeCatSprite: UIViewRepresentable {
    let frame: Int

    func makeUIView(context: Context) -> UIView {
        let view = UIView()
        view.isUserInteractionEnabled = false
        view.backgroundColor = .clear
        view.layer.contents = UIImage(named: "hingeCatAtlas")?.cgImage
        view.layer.contentsGravity = .resize
        view.layer.magnificationFilter = .linear
        view.layer.minificationFilter = .linear
        return view
    }

    func updateUIView(_ view: UIView, context: Context) {
        let index = min(50, max(0, frame))
        // Sensor updates select an approved pose; no timer or implicit tween runs.
        CATransaction.begin()
        CATransaction.setDisableActions(true)
        view.layer.contentsRect = CGRect(x: Double(index % 8) / 8,
                                         y: Double(index / 8) / 7,
                                         width: 1.0 / 8, height: 1.0 / 7)
        CATransaction.commit()
    }
}

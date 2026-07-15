import LiveKit
import UIKit

@MainActor
final class LumiVideoViewRegistry {
    static let shared = LumiVideoViewRegistry()

    private var containers: [String: UIView] = [:]
    private var videoViews: [String: VideoView] = [:]

    private init() {}

    func registerContainer(_ container: UIView, identity: String) {
        containers[identity] = container
        attachVideoView(identity: identity, to: container)
    }

    func releaseContainer(_ container: UIView, identity: String) {
        guard containers[identity] === container else { return }
        videoViews[identity]?.removeFromSuperview()
        videoViews[identity] = nil
        containers[identity] = nil
    }

    func updateTracks(_ tracksByIdentity: [String: VideoTrack?]) {
        for (identity, track) in tracksByIdentity {
            videoViews[identity]?.track = track
        }
    }

    private func attachVideoView(identity: String, to container: UIView) {
        let videoView = videoViews[identity] ?? VideoView()
        videoViews[identity] = videoView

        videoView.translatesAutoresizingMaskIntoConstraints = false
        if videoView.superview !== container {
            videoView.removeFromSuperview()
            container.addSubview(videoView)
            NSLayoutConstraint.activate([
                videoView.leadingAnchor.constraint(equalTo: container.leadingAnchor),
                videoView.trailingAnchor.constraint(equalTo: container.trailingAnchor),
                videoView.topAnchor.constraint(equalTo: container.topAnchor),
                videoView.bottomAnchor.constraint(equalTo: container.bottomAnchor),
            ])
        }
    }
}

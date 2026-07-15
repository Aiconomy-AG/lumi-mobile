import LiveKit
import UIKit

@MainActor
final class LumiVideoViewRegistry {
    static let shared = LumiVideoViewRegistry()

    private var localContainer: UIView?
    private var remoteContainer: UIView?
    private var localVideoView: VideoView?
    private var remoteVideoView: VideoView?

    private init() {}

    func registerContainer(_ container: UIView, isLocal: Bool) {
        if isLocal {
            localContainer = container
            attachVideoView(isLocal: true, to: container)
        } else {
            remoteContainer = container
            attachVideoView(isLocal: false, to: container)
        }
    }

    func releaseContainer(_ container: UIView, isLocal: Bool) {
        if isLocal, localContainer === container {
            localVideoView?.removeFromSuperview()
            localVideoView = nil
            localContainer = nil
        } else if !isLocal, remoteContainer === container {
            remoteVideoView?.removeFromSuperview()
            remoteVideoView = nil
            remoteContainer = nil
        }
    }

    func updateTracks(localTrack: VideoTrack?, remoteTrack: VideoTrack?) {
        localVideoView?.track = localTrack
        remoteVideoView?.track = remoteTrack
    }

    private func attachVideoView(isLocal: Bool, to container: UIView) {
        let videoView = isLocal ? (localVideoView ?? VideoView()) : (remoteVideoView ?? VideoView())
        if isLocal {
            localVideoView = videoView
        } else {
            remoteVideoView = videoView
        }

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

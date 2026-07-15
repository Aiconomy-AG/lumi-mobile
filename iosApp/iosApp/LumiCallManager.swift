import AVFAudio
import CallKit
import Foundation
import UIKit
@preconcurrency import LiveKit
import Shared
import _Concurrency

@MainActor
final class LumiCallManager: NSObject, CXProviderDelegate {
    static let shared = LumiCallManager()

    private let provider: CXProvider
    private var room: Room?
    private var calls: [String: UUID] = [:]
    private var answeredCalls: Set<String> = []
    private var suppressCallKitRoute = false
    private var isMuted = false
    private var isVideoCall = false
    private var roomStateTask: _Concurrency.Task<Void, Never>?

    override private init() {
        let configuration = CXProviderConfiguration(localizedName: "Lumi")
        configuration.supportsVideo = true
        configuration.maximumCallsPerCallGroup = 1
        configuration.supportedHandleTypes = [.generic]
        provider = CXProvider(configuration: configuration)
        super.init()
        provider.setDelegate(self, queue: .main)

        let center = NotificationCenter.default
        center.addObserver(self, selector: #selector(connect(_:)), name: .lumiCallConnect, object: nil)
        center.addObserver(self, selector: #selector(disconnect), name: .lumiCallDisconnect, object: nil)
        center.addObserver(self, selector: #selector(setMuted(_:)), name: .lumiCallMute, object: nil)
        center.addObserver(self, selector: #selector(setCamera(_:)), name: .lumiCallCamera, object: nil)
        center.addObserver(self, selector: #selector(showIncoming(_:)), name: .lumiCallIncoming, object: nil)
        center.addObserver(self, selector: #selector(dismiss(_:)), name: .lumiCallDismiss, object: nil)
        center.addObserver(self, selector: #selector(answerIncoming(_:)), name: .lumiCallAnswered, object: nil)
        center.addObserver(self, selector: #selector(videoViewCreated(_:)), name: .lumiVideoViewCreated, object: nil)
        center.addObserver(self, selector: #selector(videoViewReleased(_:)), name: .lumiVideoViewReleased, object: nil)
    }

    func reportIncomingFromPush(
        callId: String,
        callerName: String,
        callerUserId: String,
        isVideo: Bool,
        isGroup: Bool = false,
        completion: (() -> Void)? = nil
    ) {
        if calls[callId] != nil {
            completion?()
            return
        }
        let uuid = UUID()
        calls[callId] = uuid
        let update = CXCallUpdate()
        update.hasVideo = isVideo
        if isGroup {
            let callKind = isVideo ? "Group video call" : "Group audio call"
            update.localizedCallerName = "\(callerName) — \(callKind)"
        } else {
            update.localizedCallerName = callerName
        }
        update.remoteHandle = CXHandle(type: .generic, value: "lumi-user-\(callerUserId)")
        provider.reportNewIncomingCall(with: uuid, update: update) { error in
            if let error {
                NSLog("CallKit incoming call failed: \(error.localizedDescription)")
            }
            completion?()
        }
    }

    func dismissCall(callId: String) {
        guard let uuid = calls.removeValue(forKey: callId) else { return }
        answeredCalls.remove(callId)
        provider.reportCall(with: uuid, endedAt: Date(), reason: .remoteEnded)
    }

    func reportCallAnswered(callId: String) {
        guard let uuid = calls[callId] else { return }
        answeredCalls.insert(callId)
        suppressCallKitRoute = true
        let controller = CXCallController()
        let answerAction = CXAnswerCallAction(call: uuid)
        controller.request(CXTransaction(action: answerAction)) { error in
            if let error {
                NSLog("CallKit programmatic answer failed: \(error.localizedDescription)")
                _Concurrency.Task { @MainActor in
                    self.suppressCallKitRoute = false
                }
            }
        }
    }

    @objc private func connect(_ notification: Notification) {
        guard let url = notification.userInfo?["url"] as? String,
              let token = notification.userInfo?["token"] as? String else { return }
        let video = notification.userInfo?["video"] as? Bool ?? false
        isVideoCall = video
        _Concurrency.Task {
            let room = Room()
            self.room = room
            do {
                try await room.connect(
                    url: url,
                    token: token,
                    connectOptions: ConnectOptions(enableMicrophone: true)
                )
                if video {
                    try await room.localParticipant.setCamera(enabled: true)
                }
                self.startRoomStateUpdates()
            } catch {
                NSLog("LiveKit connect failed: \(error.localizedDescription)")
            }
        }
    }

    @objc private func disconnect() {
        roomStateTask?.cancel()
        roomStateTask = nil
        _Concurrency.Task {
            await room?.disconnect()
            room = nil
            isMuted = false
            isVideoCall = false
            LumiVideoViewRegistry.shared.updateTracks([:])
            IosLiveKitRoomHolderKt.clearIosRoomState()
        }
    }

    @objc private func setMuted(_ notification: Notification) {
        let muted = notification.userInfo?["muted"] as? Bool ?? false
        isMuted = muted
        _Concurrency.Task {
            try? await room?.localParticipant.setMicrophone(enabled: !muted)
            publishRoomState()
        }
    }

    @objc private func setCamera(_ notification: Notification) {
        let enabled = notification.userInfo?["enabled"] as? Bool ?? false
        _Concurrency.Task {
            try? await room?.localParticipant.setCamera(enabled: enabled)
            publishRoomState()
        }
    }

    @objc private func showIncoming(_ notification: Notification) {
        guard let callId = notification.userInfo?["callId"] as? String else { return }
        let callerName = notification.userInfo?["callerName"] as? String ?? "Incoming call"
        let callerUserId = notification.userInfo?["callerUserId"] as? String ?? "unknown"
        let isVideo = notification.userInfo?["video"] as? Bool ?? false
        let isGroup = notification.userInfo?["group"] as? Bool ?? false
        reportIncomingFromPush(
            callId: callId,
            callerName: callerName,
            callerUserId: callerUserId,
            isVideo: isVideo,
            isGroup: isGroup
        )
    }

    @objc private func dismiss(_ notification: Notification) {
        guard let callId = notification.userInfo?["callId"] as? String else { return }
        dismissCall(callId: callId)
    }

    @objc private func answerIncoming(_ notification: Notification) {
        guard let callId = notification.userInfo?["callId"] as? String else { return }
        reportCallAnswered(callId: callId)
    }

    @objc private func videoViewCreated(_ notification: Notification) {
        guard let container = notification.object as? UIView else { return }
        let identity = notification.userInfo?["identity"] as? String ?? "unknown"
        LumiVideoViewRegistry.shared.registerContainer(container, identity: identity)
        publishRoomState()
    }

    @objc private func videoViewReleased(_ notification: Notification) {
        guard let container = notification.object as? UIView else { return }
        let identity = notification.userInfo?["identity"] as? String ?? "unknown"
        LumiVideoViewRegistry.shared.releaseContainer(container, identity: identity)
    }

    func debugIncomingCall() {
        let callId = "debug-\(UUID().uuidString)"
        showIncoming(Notification(
            name: .lumiCallIncoming,
            userInfo: [
                "callId": callId,
                "callerName": "Lumi Debug",
                "callerUserId": "debug",
                "video": true,
            ]
        ))
    }

    nonisolated func providerDidReset(_ provider: CXProvider) {
        _Concurrency.Task { @MainActor in self.disconnect() }
    }

    nonisolated func provider(_ provider: CXProvider, perform action: CXAnswerCallAction) {
        _Concurrency.Task { @MainActor in
            if !self.suppressCallKitRoute {
                self.routeCallKitAction(uuid: action.callUUID, action: "answer")
            } else {
                self.suppressCallKitRoute = false
            }
            action.fulfill()
        }
    }

    nonisolated func provider(_ provider: CXProvider, perform action: CXEndCallAction) {
        _Concurrency.Task { @MainActor in
            self.routeCallKitAction(uuid: action.callUUID, action: "hangup")
            action.fulfill()
        }
    }

    nonisolated func provider(_ provider: CXProvider, didActivate audioSession: AVAudioSession) {
        do {
            try audioSession.setCategory(.playAndRecord, mode: .voiceChat, options: [.allowBluetooth, .defaultToSpeaker])
            try audioSession.setActive(true)
        } catch {
            NSLog("Audio session activation failed: \(error.localizedDescription)")
        }
    }

    nonisolated func provider(_ provider: CXProvider, didDeactivate audioSession: AVAudioSession) {
        do {
            try audioSession.setActive(false, options: .notifyOthersOnDeactivation)
        } catch {
            NSLog("Audio session deactivation failed: \(error.localizedDescription)")
        }
    }

    private func routeCallKitAction(uuid: UUID, action: String) {
        guard let callId = calls.first(where: { $0.value == uuid })?.key else { return }
        IosPushBridgeKt.onIosNotificationDataReceived(data: [
            "type": "workspace_call_incoming",
            "call_id": callId,
            "call_action": action,
        ])
    }

    private func startRoomStateUpdates() {
        roomStateTask?.cancel()
        roomStateTask = _Concurrency.Task { [weak self] in
            while !_Concurrency.Task.isCancelled {
                await self?.publishRoomState()
                try? await _Concurrency.Task.sleep(nanoseconds: 400_000_000)
            }
        }
    }

    private func publishRoomState() {
        guard let room else {
            IosLiveKitRoomHolderKt.clearIosRoomState()
            return
        }

        var tracksByIdentity: [String: VideoTrack?] = [:]
        var identities: [String] = []
        var names: [String] = []
        var isLocalFlags: [Bool] = []
        var cameraEnabledFlags: [Bool] = []
        var mutedFlags: [Bool] = []
        var hasVideoTrackFlags: [Bool] = []

        let localIdentity = room.localParticipant.identity?.description ?? "local"
        let localName = room.localParticipant.name ?? localIdentity
        let localPublication = room.localParticipant.trackPublications.values.first { $0.source == .camera }
        let localMicPublication = room.localParticipant.trackPublications.values.first { $0.source == .microphone }
        let localTrack = localPublication?.track as? VideoTrack
        let localCameraEnabled = localPublication?.isMuted == false && localTrack != nil
        tracksByIdentity[localIdentity] = localTrack
        identities.append(localIdentity)
        names.append(localName)
        isLocalFlags.append(true)
        cameraEnabledFlags.append(localCameraEnabled)
        mutedFlags.append(localMicPublication?.isMuted == true)
        hasVideoTrackFlags.append(localTrack != nil)

        var remoteCameraEnabled = false
        var remoteHasVideoTrack = false
        var remoteName = ""
        for remoteParticipant in room.remoteParticipants.values {
            let identity = remoteParticipant.identity?.description ?? remoteParticipant.name ?? "remote"
            let name = remoteParticipant.name ?? identity
            let remotePublication = remoteParticipant.trackPublications.values.first { $0.source == .camera }
            let micPublication = remoteParticipant.trackPublications.values.first { $0.source == .microphone }
            let remoteTrack = remotePublication?.track as? VideoTrack
            let cameraEnabled = remotePublication?.isMuted == false && remoteTrack != nil
            tracksByIdentity[identity] = remoteTrack
            identities.append(identity)
            names.append(name)
            isLocalFlags.append(false)
            cameraEnabledFlags.append(cameraEnabled)
            mutedFlags.append(micPublication?.isMuted == true)
            hasVideoTrackFlags.append(remoteTrack != nil)
            if remoteName.isEmpty {
                remoteName = name
                remoteCameraEnabled = cameraEnabled
                remoteHasVideoTrack = remoteTrack != nil
            }
        }

        LumiVideoViewRegistry.shared.updateTracks(tracksByIdentity)

        IosLiveKitRoomHolderKt.updateIosRoomState(
            remoteCount: Int32(room.remoteParticipants.count),
            remoteCameraEnabled: remoteCameraEnabled,
            localCameraEnabled: localCameraEnabled,
            isMuted: isMuted,
            localHasVideoTrack: localTrack != nil,
            remoteHasVideoTrack: remoteHasVideoTrack,
            remoteParticipantName: remoteName,
            identities: identities,
            names: names,
            isLocalFlags: isLocalFlags.map { KotlinBoolean(bool: $0) },
            cameraEnabledFlags: cameraEnabledFlags.map { KotlinBoolean(bool: $0) },
            mutedFlags: mutedFlags.map { KotlinBoolean(bool: $0) },
            hasVideoTrackFlags: hasVideoTrackFlags.map { KotlinBoolean(bool: $0) }
        )
    }
}

private extension Notification.Name {
    static let lumiCallConnect = Notification.Name("LumiCallConnect")
    static let lumiCallDisconnect = Notification.Name("LumiCallDisconnect")
    static let lumiCallMute = Notification.Name("LumiCallMute")
    static let lumiCallCamera = Notification.Name("LumiCallCamera")
    static let lumiCallIncoming = Notification.Name("LumiCallIncoming")
    static let lumiCallDismiss = Notification.Name("LumiCallDismiss")
    static let lumiCallAnswered = Notification.Name("LumiCallAnswered")
    static let lumiVideoViewCreated = Notification.Name("LumiVideoViewCreated")
    static let lumiVideoViewReleased = Notification.Name("LumiVideoViewReleased")
}

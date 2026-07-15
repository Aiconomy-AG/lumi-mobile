import AVFAudio
import CallKit
import Foundation
@preconcurrency import LiveKit
import Shared
import _Concurrency

@MainActor
final class LumiCallManager: NSObject, CXProviderDelegate {
    static let shared = LumiCallManager()

    private let provider: CXProvider
    private let controller = CXCallController()
    private var room: Room?
    private var calls: [String: UUID] = [:]

    override private init() {
        let configuration = CXProviderConfiguration(localizedName: "Lumi")
        configuration.supportsVideo = false
        configuration.maximumCallsPerCallGroup = 1
        configuration.supportedHandleTypes = [.generic]
        provider = CXProvider(configuration: configuration)
        super.init()
        provider.setDelegate(self, queue: .main)

        let center = NotificationCenter.default
        center.addObserver(self, selector: #selector(connect(_:)), name: .lumiCallConnect, object: nil)
        center.addObserver(self, selector: #selector(disconnect), name: .lumiCallDisconnect, object: nil)
        center.addObserver(self, selector: #selector(setMuted(_:)), name: .lumiCallMute, object: nil)
        center.addObserver(self, selector: #selector(showIncoming(_:)), name: .lumiCallIncoming, object: nil)
        center.addObserver(self, selector: #selector(dismiss(_:)), name: .lumiCallDismiss, object: nil)
    }

    @objc private func connect(_ notification: Notification) {
        guard let url = notification.userInfo?["url"] as? String,
              let token = notification.userInfo?["token"] as? String else { return }
        _Concurrency.Task {
            let room = Room()
            self.room = room
            do {
                try await room.connect(url: url, token: token, connectOptions: ConnectOptions(enableMicrophone: true))
            } catch {
                NSLog("LiveKit connect failed: \(error.localizedDescription)")
            }
        }
    }

    @objc private func disconnect() {
        _Concurrency.Task { await room?.disconnect(); room = nil }
    }

    @objc private func setMuted(_ notification: Notification) {
        let muted = notification.userInfo?["muted"] as? Bool ?? false
        _Concurrency.Task { try? await room?.localParticipant.setMicrophone(enabled: !muted) }
    }

    @objc private func showIncoming(_ notification: Notification) {
        guard let callId = notification.userInfo?["callId"] as? String else { return }
        let uuid = calls[callId] ?? UUID()
        calls[callId] = uuid
        let update = CXCallUpdate()
        update.hasVideo = false
        update.localizedCallerName = notification.userInfo?["callerName"] as? String
        let callerUserId = notification.userInfo?["callerUserId"] as? String ?? "unknown"
        update.remoteHandle = CXHandle(type: .generic, value: "lumi-user-\(callerUserId)")
        provider.reportNewIncomingCall(with: uuid, update: update) { error in
            if let error { NSLog("CallKit incoming call failed: \(error.localizedDescription)") }
        }
    }

    @objc private func dismiss(_ notification: Notification) {
        guard let callId = notification.userInfo?["callId"] as? String, let uuid = calls.removeValue(forKey: callId) else { return }
        provider.reportCall(with: uuid, endedAt: Date(), reason: .remoteEnded)
    }

    func debugIncomingCall() {
        let callId = "debug-\(UUID().uuidString)"
        showIncoming(Notification(name: .lumiCallIncoming, userInfo: ["callId": callId, "callerName": "Lumi Debug", "callerUserId": "debug"]))
    }

    nonisolated func providerDidReset(_ provider: CXProvider) {
        _Concurrency.Task { @MainActor in self.disconnect() }
    }

    nonisolated func provider(_ provider: CXProvider, perform action: CXAnswerCallAction) {
        _Concurrency.Task { @MainActor in
            self.routeCallKitAction(uuid: action.callUUID, action: "answer")
            action.fulfill()
        }
    }
    nonisolated func provider(_ provider: CXProvider, perform action: CXEndCallAction) {
        _Concurrency.Task { @MainActor in
            self.routeCallKitAction(uuid: action.callUUID, action: "hangup")
            action.fulfill()
        }
    }
    nonisolated func provider(_ provider: CXProvider, didActivate audioSession: AVAudioSession) {}
    nonisolated func provider(_ provider: CXProvider, didDeactivate audioSession: AVAudioSession) {}

    private func routeCallKitAction(uuid: UUID, action: String) {
        guard let callId = calls.first(where: { $0.value == uuid })?.key else { return }
        IosPushBridgeKt.onIosNotificationDataReceived(data: [
            "type": "workspace_call_incoming",
            "call_id": callId,
            "call_action": action,
        ])
    }
}

private extension Notification.Name {
    static let lumiCallConnect = Notification.Name("LumiCallConnect")
    static let lumiCallDisconnect = Notification.Name("LumiCallDisconnect")
    static let lumiCallMute = Notification.Name("LumiCallMute")
    static let lumiCallIncoming = Notification.Name("LumiCallIncoming")
    static let lumiCallDismiss = Notification.Name("LumiCallDismiss")
}

import FirebaseMessaging
import PushKit
import Shared
import UIKit
import UserNotifications
import _Concurrency

final class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate, MessagingDelegate, PKPushRegistryDelegate {
    private var pushRegistry: PKPushRegistry?
    private var startedFromIncomingCall = false

    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        UNUserNotificationCenter.current().delegate = self
        Messaging.messaging().delegate = self
        _ = LumiCallManager.shared

        configureVoipPush()
        requestNotificationPermission(application: application)

        if let remoteNotification = launchOptions?[.remoteNotification] as? [AnyHashable: Any] {
            startedFromIncomingCall = isIncomingCallNotification(remoteNotification)
            handleNotificationUserInfo(remoteNotification)
        }

        return true
    }

    func applicationDidBecomeActive(_ application: UIApplication) {
        IosPushBridgeKt.refreshIosCallPermissions()
        if !startedFromIncomingCall {
            IosPushBridgeKt.requestIosLaunchCallPermissions()
        }
        startedFromIncomingCall = false
    }

    private func configureVoipPush() {
        let registry = PKPushRegistry(queue: .main)
        registry.delegate = self
        registry.desiredPushTypes = [.voIP]
        pushRegistry = registry
    }

    private func requestNotificationPermission(application: UIApplication) {
        UNUserNotificationCenter.current().requestAuthorization(options: [.alert, .badge, .sound]) { granted, _ in
            IosPushBridgeKt.updateIosNotificationPermission(granted: granted)

            guard granted else { return }

            DispatchQueue.main.async {
                application.registerForRemoteNotifications()
            }
        }
    }

    func application(
        _ application: UIApplication,
        didRegisterForRemoteNotificationsWithDeviceToken deviceToken: Data
    ) {
        Messaging.messaging().apnsToken = deviceToken
        refreshFcmToken()
    }

    func application(
        _ application: UIApplication,
        didFailToRegisterForRemoteNotificationsWithError error: Error
    ) {
        NSLog("APNs registration failed: \(error.localizedDescription)")
    }

    func application(
        _ application: UIApplication,
        didReceiveRemoteNotification userInfo: [AnyHashable: Any],
        fetchCompletionHandler completionHandler: @escaping (UIBackgroundFetchResult) -> Void
    ) {
        handleNotificationUserInfo(userInfo)
        completionHandler(.newData)
    }

    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        guard let fcmToken else { return }
        IosPushBridgeKt.onIosFcmTokenRefreshed(token: fcmToken)
    }

    func messaging(_ messaging: Messaging, didReceive remoteMessage: MessagingRemoteMessage) {
        handleNotificationUserInfo(remoteMessage.appData)
    }

    func pushRegistry(_ registry: PKPushRegistry, didUpdate pushCredentials: PKPushCredentials, for type: PKPushType) {
        guard type == .voIP else { return }
        let token = pushCredentials.token.map { String(format: "%02x", $0) }.joined()
        IosPushBridgeKt.onIosVoipTokenRefreshed(token: token)
    }

    func pushRegistry(
        _ registry: PKPushRegistry,
        didReceiveIncomingPushWith payload: PKPushPayload,
        for type: PKPushType,
        completion: @escaping () -> Void
    ) {
        guard type == .voIP else {
            completion()
            return
        }

        let data = extractNotificationData(from: payload.dictionaryPayload)
        guard let callId = data["call_id"], data["type"] == "workspace_call_incoming" else {
            completion()
            return
        }

        startedFromIncomingCall = true
        let callerName = data["caller_name"] ?? "Incoming call"
        let callerUserId = data["caller_user_id"] ?? "unknown"
        let isVideo = data["call_type"] == "video"

        _Concurrency.Task { @MainActor in
            LumiCallManager.shared.reportIncomingFromPush(
                callId: callId,
                callerName: callerName,
                callerUserId: callerUserId,
                isVideo: isVideo
            ) {
                IosPushBridgeKt.onIosNotificationDataReceived(data: data)
                completion()
            }
        }
    }

    func pushRegistry(_ registry: PKPushRegistry, didInvalidatePushTokenFor type: PKPushType) {
        NSLog("VoIP push token invalidated for type: \(type.rawValue)")
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
        let userInfo = notification.request.content.userInfo
        if isIncomingCallNotification(userInfo) {
            handleNotificationUserInfo(userInfo)
            completionHandler([])
            return
        }
        completionHandler([.banner, .sound, .badge])
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        didReceive response: UNNotificationResponse,
        withCompletionHandler completionHandler: @escaping () -> Void
    ) {
        handleNotificationUserInfo(response.notification.request.content.userInfo)
        completionHandler()
    }

    private func refreshFcmToken() {
        Messaging.messaging().token { token, error in
            if let error {
                NSLog("Failed to fetch FCM token: \(error.localizedDescription)")
                return
            }

            guard let token else { return }
            IosPushBridgeKt.onIosFcmTokenRefreshed(token: token)
        }
    }

    private func handleNotificationUserInfo(_ userInfo: [AnyHashable: Any]) {
        let data = extractNotificationData(from: userInfo)
        guard !data.isEmpty else { return }

        if data["type"] == "workspace_call_updated",
           let callId = data["call_id"],
           isTerminalCallUpdate(status: data["status"]) {
            _Concurrency.Task { @MainActor in
                LumiCallManager.shared.dismissCall(callId: callId)
            }
        }

        IosPushBridgeKt.onIosNotificationDataReceived(data: data)
    }

    private func extractNotificationData(from userInfo: [AnyHashable: Any]) -> [String: String] {
        if let lumi = userInfo["lumi"] as? [String: Any] {
            return flattenNotificationValues(lumi)
        }
        return flattenNotificationValues(userInfo)
    }

    private func flattenNotificationValues(_ values: [AnyHashable: Any]) -> [String: String] {
        var data: [String: String] = [:]

        for (key, value) in values {
            guard let key = key as? String else { continue }

            if let stringValue = value as? String {
                data[key] = stringValue
            } else if let numberValue = value as? NSNumber {
                data[key] = numberValue.stringValue
            } else if let boolValue = value as? Bool {
                data[key] = boolValue ? "true" : "false"
            }
        }

        return data
    }

    private func isIncomingCallNotification(_ userInfo: [AnyHashable: Any]) -> Bool {
        let data = extractNotificationData(from: userInfo)
        return data["type"] == "workspace_call_incoming"
    }

    private func isTerminalCallUpdate(status: String?) -> Bool {
        guard let status else { return false }
        return ["ended", "declined", "cancelled", "missed", "failed"].contains(status.lowercased())
    }
}

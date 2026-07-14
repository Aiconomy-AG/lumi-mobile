import FirebaseMessaging
import Shared
import UIKit
import UserNotifications

final class AppDelegate: NSObject, UIApplicationDelegate, UNUserNotificationCenterDelegate, MessagingDelegate {
    func application(
        _ application: UIApplication,
        didFinishLaunchingWithOptions launchOptions: [UIApplication.LaunchOptionsKey: Any]? = nil
    ) -> Bool {
        UNUserNotificationCenter.current().delegate = self
        Messaging.messaging().delegate = self
        _ = LumiCallManager.shared

        requestNotificationPermission(application: application)

        if let remoteNotification = launchOptions?[.remoteNotification] as? [AnyHashable: Any] {
            handleNotificationUserInfo(remoteNotification)
        }

        return true
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

    func messaging(_ messaging: Messaging, didReceiveRegistrationToken fcmToken: String?) {
        guard let fcmToken else { return }
        IosPushBridgeKt.onIosFcmTokenRefreshed(token: fcmToken)
    }

    func userNotificationCenter(
        _ center: UNUserNotificationCenter,
        willPresent notification: UNNotification,
        withCompletionHandler completionHandler: @escaping (UNNotificationPresentationOptions) -> Void
    ) {
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
        var data: [String: String] = [:]

        for (key, value) in userInfo {
            guard let key = key as? String else { continue }

            if let stringValue = value as? String {
                data[key] = stringValue
            } else if let numberValue = value as? NSNumber {
                data[key] = numberValue.stringValue
            }
        }

        guard !data.isEmpty else { return }
        IosPushBridgeKt.onIosNotificationDataReceived(data: data)
    }
}

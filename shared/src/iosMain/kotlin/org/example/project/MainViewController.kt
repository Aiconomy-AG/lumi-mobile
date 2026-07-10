package org.example.project

import androidx.compose.ui.window.ComposeUIViewController
import org.example.project.data.auth.SessionStorage
import org.example.project.data.chat.ChatReadStateStorage
import org.example.project.notifications.PushNotifications

fun MainViewController() = ComposeUIViewController {
    PushNotifications.initialize()
    SessionStorage.initialize()
    ChatReadStateStorage.initialize()
    App()
}
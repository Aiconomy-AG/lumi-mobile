package org.example.project

import androidx.compose.ui.window.ComposeUIViewController
import org.example.project.data.auth.SessionStorage
import org.example.project.data.calls.ClientInstanceIdStorage
import org.example.project.data.chat.ChatReadStateStorage
import org.example.project.data.search.RecentSearchStorage
import org.example.project.domain.calls.CallPermissions
import org.example.project.notifications.PushNotifications

fun MainViewController() = ComposeUIViewController {
    PushNotifications.initialize()
    SessionStorage.initialize()
    ChatReadStateStorage.initialize()
    ClientInstanceIdStorage.initialize()
    CallPermissions.initialize()
    RecentSearchStorage.initialize()
    App()
}

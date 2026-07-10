package org.example.project

import androidx.compose.ui.window.ComposeUIViewController
import org.example.project.data.auth.SessionStorage
import org.example.project.data.chat.ChatReadStateStorage

fun MainViewController() = ComposeUIViewController {
    SessionStorage.initialize()
    ChatReadStateStorage.initialize()
    App()
}
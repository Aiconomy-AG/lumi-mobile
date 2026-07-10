package org.example.project

import androidx.compose.ui.window.ComposeUIViewController
import org.example.project.data.auth.SessionStorage

fun MainViewController() = ComposeUIViewController {
    SessionStorage.initialize()
    App()
}
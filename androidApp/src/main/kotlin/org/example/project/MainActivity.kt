package org.example.project

import android.Manifest
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch
import org.example.project.data.calls.ClientInstanceIdStorage
import org.example.project.notifications.AndroidNotificationIntents
import org.example.project.notifications.PushNotificationCoordinator
import org.example.project.notifications.PushNotifications
import org.example.project.domain.calls.AndroidCallRuntime

class MainActivity : ComponentActivity() {
    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        lifecycleScope.launch {
            logFcmToken()
            PushNotificationCoordinator.reregisterIfPossible()
        }
    }

    private val requestCameraPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) { }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        ClientInstanceIdStorage.initialize(this)
        PushNotifications.initialize(this)
        AndroidCallRuntime.initialize(this)
        org.example.project.data.auth.SessionStorage.initialize(this)
        org.example.project.data.chat.ChatReadStateStorage.initialize(this)
        AndroidNotificationIntents.handle(intent)

        lifecycleScope.launch {
            when {
                !PushNotifications.needsRuntimePermission() -> logFcmToken()
                Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU -> {
                    requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
                else -> logFcmToken()
            }
        }

        if (checkSelfPermission(Manifest.permission.RECORD_AUDIO) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            registerForActivityResult(ActivityResultContracts.RequestPermission()) {}.launch(Manifest.permission.RECORD_AUDIO)
        }

        if (checkSelfPermission(Manifest.permission.CAMERA) != android.content.pm.PackageManager.PERMISSION_GRANTED) {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
        }

        setContent {
            App()
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        AndroidNotificationIntents.handle(intent)
    }

    private suspend fun logFcmToken() {
        val fcmToken = PushNotifications.getFcmToken()
        Log.d(TAG, "FCM token: ${fcmToken ?: "unavailable"}")
    }

    companion object {
        private const val TAG = "MainActivity"
    }
}

@Preview
@Composable
fun AppAndroidPreview() {
    App()
}

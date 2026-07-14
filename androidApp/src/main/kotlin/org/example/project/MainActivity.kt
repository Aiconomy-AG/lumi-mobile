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
import org.example.project.domain.calls.AndroidCallRuntime
import org.example.project.domain.calls.CallPermissions
import org.example.project.data.auth.SessionStorage
import org.example.project.data.chat.ChatReadStateStorage
import org.example.project.notifications.PendingNotificationIntent
import org.example.project.notifications.PushNotificationCoordinator
import org.example.project.notifications.PushNotifications

class MainActivity : ComponentActivity() {
    private var initialPermissionFlowStarted = false
    private var startedFromIncomingCall = false

    private val requestNotificationPermission = registerForActivityResult(
        ActivityResultContracts.RequestPermission(),
    ) {
        lifecycleScope.launch {
            logFcmToken()
            PushNotificationCoordinator.reregisterIfPossible()
            CallPermissions.requestAtLaunchIfNeeded()
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        ClientInstanceIdStorage.initialize(this)
        CallPermissions.attachActivity(this)
        CallPermissions.initialize(this)
        PushNotifications.initialize(this)
        AndroidCallRuntime.initialize(this)
        org.example.project.data.auth.SessionStorage.initialize(this)
        org.example.project.data.chat.ChatReadStateStorage.initialize(this)
        startedFromIncomingCall =
            intent.getStringExtra("type") == "workspace_call_incoming"
        PendingNotificationIntent.enqueue(intent)

        setContent {
            App()
        }
    }

    override fun onPostResume() {
        super.onPostResume()
        CallPermissions.refresh()
        if (initialPermissionFlowStarted) return
        initialPermissionFlowStarted = true

        if (startedFromIncomingCall) {
            lifecycleScope.launch { logFcmToken() }
            return
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            PushNotifications.needsRuntimePermission()
        ) {
            requestNotificationPermission.launch(Manifest.permission.POST_NOTIFICATIONS)
        } else {
            lifecycleScope.launch {
                logFcmToken()
                CallPermissions.requestAtLaunchIfNeeded()
            }
        }
    }

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        PendingNotificationIntent.enqueue(intent)
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

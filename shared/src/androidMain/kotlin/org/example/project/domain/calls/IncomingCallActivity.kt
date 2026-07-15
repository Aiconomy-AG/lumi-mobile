package org.example.project.domain.calls

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.notifications.IncomingCallRingingService
import org.example.project.presentation.theme.AppColorPalette

class IncomingCallActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        AndroidCallRuntime.initialize(this)

        val callId = intent.getStringExtra("call_id").orEmpty()
        val callerName = intent.getStringExtra("caller_name") ?: "Incoming call"
        val isVideo = intent.getStringExtra("call_type") == "video"
        val presetAction = intent.getStringExtra("call_action")?.takeIf { it.isNotBlank() }

        if (callId.isNotBlank()) {
            IncomingCallRingingService.stop(this, callId)
        }

        if (presetAction != null) {
            launchMain(presetAction)
            finish()
            return
        }

        setContent {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColorPalette.Background)
                    .padding(32.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) {
                Column(
                    modifier = Modifier
                        .size(96.dp)
                        .background(AppColorPalette.Primary, CircleShape),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        text = callerName.take(2).uppercase(),
                        color = AppColorPalette.OnPrimary,
                        fontSize = 28.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(modifier = Modifier.height(24.dp))
                Text(callerName, color = AppColorPalette.TextPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold)
                Text(
                    if (isVideo) "Incoming video call" else "Incoming audio call",
                    color = AppColorPalette.TextSecondary,
                )
                Spacer(modifier = Modifier.height(48.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceEvenly,
                ) {
                    Button(
                        onClick = {
                            launchMain("decline")
                            finish()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppColorPalette.LogoutDanger),
                    ) { Text("Decline") }
                    Button(
                        onClick = {
                            launchMain("answer")
                            finish()
                        },
                        colors = ButtonDefaults.buttonColors(containerColor = AppColorPalette.Success),
                    ) { Text("Answer") }
                }
            }
        }
    }

    private fun launchMain(action: String) {
        val launch = packageManager.getLaunchIntentForPackage(packageName) ?: return
        launch.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
        intent.extras?.let { extras -> launch.putExtras(extras) }
        launch.putExtra("call_action", action)
        startActivity(launch)
    }
}

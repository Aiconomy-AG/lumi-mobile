package org.example.project.presentation.calls

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.example.project.domain.calls.CallStatus
import org.example.project.presentation.localization.LocalAppStrings
import org.example.project.presentation.theme.AppColorPalette

@Composable
fun CallOverlay(viewModel: CallViewModel, currentUserId: Int, state: CallUiState) {
    val call = state.call ?: return
    val strings = LocalAppStrings.current
    val incoming = call.status == CallStatus.RINGING && call.initiatedByUserId != currentUserId
    val other = call.participants.firstOrNull { it.userId != currentUserId }
    val name = if (incoming) call.caller.name else other?.name ?: call.caller.name
    val callLabel = when {
        incoming && call.isVideo -> strings.text("Incoming video call")
        incoming -> strings.text("Incoming audio call")
        call.isVideo -> strings.text("Video call")
        else -> strings.text("Audio call")
    }

    Dialog(onDismissRequest = {}) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColorPalette.OverlaySurface, RoundedCornerShape(24.dp))
                .border(1.dp, AppColorPalette.Border, RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(callLabel, color = AppColorPalette.TextSecondary)
            if (call.isGroup) {
                Text(strings.text("Group call"), color = AppColorPalette.Primary, fontSize = 12.sp)
            }
            Spacer(Modifier.height(16.dp))

            if (call.isVideo && call.status == CallStatus.ACTIVE) {
                CallVideoRenderer(
                    isLocal = false,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(180.dp),
                )
                Spacer(Modifier.height(8.dp))
                CallVideoRenderer(
                    isLocal = true,
                    modifier = Modifier
                        .size(96.dp),
                )
                Spacer(Modifier.height(12.dp))
            } else {
                Column(
                    Modifier.size(80.dp).background(AppColorPalette.Primary, CircleShape),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    Text(
                        name.take(2).uppercase(),
                        color = AppColorPalette.OnPrimary,
                        fontSize = 24.sp,
                        fontWeight = FontWeight.Bold,
                    )
                }
                Spacer(Modifier.height(16.dp))
            }

            Text(name, color = AppColorPalette.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(
                if (incoming) strings.text("Lumi Workspace call") else state.connectionLabel,
                color = AppColorPalette.TextSecondary,
                fontSize = 12.sp,
            )

            if (call.isGroup) {
                Spacer(Modifier.height(12.dp))
                LazyColumn(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(120.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp),
                ) {
                    items(call.participants, key = { it.userId }) { participant ->
                        Text(
                            "${participant.name.ifBlank { "User ${participant.userId}" }} — ${participant.status}",
                            color = AppColorPalette.TextSecondary,
                            fontSize = 12.sp,
                        )
                    }
                }
            }

            state.error?.let {
                Text(it, color = AppColorPalette.Error, modifier = Modifier.padding(top = 8.dp))
            }

            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                when {
                    incoming -> {
                        CallButton(strings.text("Decline"), AppColorPalette.LogoutDanger, viewModel::decline)
                        CallButton(strings.text("Answer"), AppColorPalette.Success, viewModel::accept)
                    }
                    call.status == CallStatus.RINGING -> {
                        CallButton(strings.text("Cancel"), AppColorPalette.LogoutDanger, viewModel::cancel)
                    }
                    else -> {
                        if (call.isVideo) {
                            CallButton(
                                if (state.cameraEnabled) strings.text("Camera off") else strings.text("Camera on"),
                                AppColorPalette.SurfaceVariant,
                                viewModel::toggleCamera,
                            )
                        }
                        CallButton(
                            if (state.muted) strings.text("Unmute") else strings.text("Mute"),
                            AppColorPalette.SurfaceVariant,
                            viewModel::toggleMute,
                        )
                        if (call.isGroup) {
                            CallButton(strings.text("Leave"), AppColorPalette.SurfaceVariant, viewModel::leave)
                        }
                        CallButton(strings.text("End call"), AppColorPalette.LogoutDanger, viewModel::end)
                    }
                }
            }
        }
    }
}

@Composable
private fun CallButton(label: String, color: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = color)) {
        Text(label, fontSize = 12.sp)
    }
}

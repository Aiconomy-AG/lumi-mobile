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
import org.example.project.presentation.theme.AppColorPalette

@Composable
fun CallOverlay(viewModel: CallViewModel, currentUserId: Int, state: CallUiState) {
    val call = state.call ?: return
    val incoming = call.status == CallStatus.RINGING && call.initiatedByUserId != currentUserId
    val other = call.participants.firstOrNull { it.userId != currentUserId }
    val name = if (incoming) call.caller.name else other?.name ?: call.caller.name

    Dialog(onDismissRequest = {}) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColorPalette.OverlaySurface, RoundedCornerShape(24.dp))
                .border(1.dp, AppColorPalette.Border, RoundedCornerShape(24.dp))
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(if (incoming) "Incoming audio call" else "Lumi audio call", color = AppColorPalette.TextSecondary)
            Spacer(Modifier.height(20.dp))
            Column(
                Modifier.size(80.dp).background(AppColorPalette.Primary, CircleShape),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Center,
            ) { Text(name.take(2).uppercase(), color = AppColorPalette.OnPrimary, fontSize = 24.sp, fontWeight = FontWeight.Bold) }
            Spacer(Modifier.height(16.dp))
            Text(name, color = AppColorPalette.TextPrimary, fontSize = 20.sp, fontWeight = FontWeight.Bold)
            Text(
                if (incoming) "Lumi Workspace audio call" else state.connectionLabel,
                color = AppColorPalette.TextSecondary,
                fontSize = 12.sp,
            )
            state.error?.let { Text(it, color = AppColorPalette.Error, modifier = Modifier.padding(top = 8.dp)) }
            Spacer(Modifier.height(24.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                when {
                    incoming -> {
                        CallButton("Decline", AppColorPalette.LogoutDanger, viewModel::decline)
                        CallButton("Answer", AppColorPalette.Success, viewModel::accept)
                    }
                    call.status == CallStatus.RINGING -> CallButton("Cancel", AppColorPalette.LogoutDanger, viewModel::cancel)
                    else -> {
                        CallButton(if (state.muted) "Unmute" else "Mute", AppColorPalette.SurfaceVariant, viewModel::toggleMute)
                        CallButton("End", AppColorPalette.LogoutDanger, viewModel::end)
                    }
                }
            }
        }
    }
}

@Composable
private fun CallButton(label: String, color: androidx.compose.ui.graphics.Color, onClick: () -> Unit) {
    Button(onClick = onClick, colors = ButtonDefaults.buttonColors(containerColor = color)) { Text(label) }
}

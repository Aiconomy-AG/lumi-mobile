package org.example.project.presentation.calls

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.domain.calls.CallParticipant
import org.example.project.presentation.localization.LocalAppStrings
import org.example.project.presentation.theme.AppColorPalette

@OptIn(ExperimentalLayoutApi::class)
@Composable
fun GroupCallParticipantChips(
    participants: List<CallParticipant>,
    currentUserId: Int,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current
    val chips = participants.filter { it.userId != currentUserId }
    FlowRow(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        chips.forEach { participant ->
            ParticipantChip(
                name = participant.name.ifBlank { "User ${participant.userId}" },
                statusLabel = participantStatusLabel(participant.status, strings),
                status = participant.status,
            )
        }
    }
}

@Composable
private fun ParticipantChip(
    name: String,
    statusLabel: String,
    status: String,
) {
    val badgeColor = when (status.lowercase()) {
        "joined" -> AppColorPalette.Success
        "ringing", "invited" -> AppColorPalette.Warning
        else -> AppColorPalette.TextSecondary
    }
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(AppColorPalette.SurfaceVariant)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Box(
            modifier = Modifier
                .size(28.dp)
                .background(AppColorPalette.Primary, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = name.take(2).uppercase(),
                color = AppColorPalette.OnPrimary,
                fontSize = 11.sp,
                fontWeight = FontWeight.Bold,
            )
        }
        Column {
            Text(
                text = name,
                color = AppColorPalette.TextPrimary,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = statusLabel,
                color = badgeColor,
                fontSize = 11.sp,
            )
        }
    }
}

@Composable
fun OutgoingGroupRingingScreen(
    conversationName: String,
    participants: List<CallParticipant>,
    currentUserId: Int,
    connectionLabel: String,
    error: String?,
    onCancel: () -> Unit,
) {
    val strings = LocalAppStrings.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 24.dp, vertical = 32.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = conversationName.ifBlank { strings.text("Calling group…") },
            color = AppColorPalette.TextPrimary,
            fontSize = 24.sp,
            fontWeight = FontWeight.Bold,
        )
        Spacer(Modifier.height(8.dp))
        Text(
            text = connectionLabel.ifBlank { strings.text("Calling…") },
            color = AppColorPalette.TextSecondary,
            fontSize = 14.sp,
        )
        error?.let {
            Spacer(Modifier.height(8.dp))
            Text(it, color = AppColorPalette.Error, fontSize = 12.sp)
        }
        Spacer(Modifier.height(24.dp))
        GroupCallParticipantChips(participants = participants, currentUserId = currentUserId)
        Spacer(Modifier.height(28.dp))
        OutgoingRingingControls(onCancel = onCancel, strings = strings)
    }
}

@Composable
private fun OutgoingRingingControls(
    onCancel: () -> Unit,
    strings: org.example.project.presentation.localization.AppStrings,
) {
    androidx.compose.foundation.layout.Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        CallCircleButton(
            background = AppColorPalette.LogoutDanger,
            icon = Icons.Filled.CallEnd,
            contentDescription = strings.text("Cancel"),
            onClick = onCancel,
        )
    }
}

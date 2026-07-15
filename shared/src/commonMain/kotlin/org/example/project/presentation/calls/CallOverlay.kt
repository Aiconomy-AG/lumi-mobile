package org.example.project.presentation.calls

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.CallEnd
import androidx.compose.material.icons.filled.Mic
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.PersonAdd
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.domain.calls.CallStatus
import org.example.project.domain.calls.CallPermissionState
import org.example.project.presentation.localization.LocalAppStrings
import org.example.project.presentation.theme.AppColorPalette

@Composable
fun CallOverlay(
    viewModel: CallViewModel,
    currentUserId: Int,
    state: CallUiState,
    inviteCandidates: List<CallInviteCandidate> = emptyList(),
    conversationTitle: String = "",
) {
    val call = state.call ?: return
    val strings = LocalAppStrings.current
    val incoming = call.status == CallStatus.RINGING && call.initiatedByUserId != currentUserId
    val other = call.participants.firstOrNull { it.userId != currentUserId }
    val remoteName = if (incoming) call.caller.name else other?.name ?: call.caller.name
    val selfName = call.participants.find { it.userId == currentUserId }?.name ?: strings.text("You")
    val isActive = call.status == CallStatus.ACTIVE
    val isActiveVideo = call.isVideo && isActive
    val tiles = buildActiveCallTiles(
        call = call,
        mediaParticipants = state.mediaParticipants,
        currentUserId = currentUserId,
        selfName = selfName,
        localCameraEnabled = state.cameraEnabled,
    )
    var showInviteSheet by remember(call.id) { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(AppColorPalette.Background),
    ) {
        when {
            state.uiMode == CallUiMode.OutgoingRinging && call.isGroup -> {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .statusBarsPadding()
                        .navigationBarsPadding(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center,
                ) {
                    OutgoingGroupRingingScreen(
                        conversationName = conversationTitle,
                        participants = call.participants,
                        currentUserId = currentUserId,
                        connectionLabel = state.connectionLabel,
                        error = state.error,
                        onCancel = viewModel::cancel,
                    )
                }
            }
            isActive && call.isGroup -> {
                GroupCallVideoGrid(
                    tiles = tiles,
                    showVideo = call.isVideo,
                    modifier = Modifier.fillMaxSize(),
                )
            }
            isActiveVideo -> {
                CallVideoRenderer(
                    isLocal = false,
                    modifier = Modifier.fillMaxSize(),
                    participantName = remoteName,
                    cameraEnabled = state.remoteCameraEnabled,
                )
                CallVideoRenderer(
                    isLocal = true,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .statusBarsPadding()
                        .padding(16.dp)
                        .size(112.dp)
                        .clip(RoundedCornerShape(16.dp)),
                    participantName = selfName,
                    cameraEnabled = state.cameraEnabled,
                )
            }
        }

        if (state.uiMode != CallUiMode.OutgoingRinging || !call.isGroup) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .align(Alignment.BottomCenter)
                    .background(
                        Brush.verticalGradient(
                            colors = listOf(Color.Transparent, AppColorPalette.Background.copy(alpha = 0.92f)),
                        ),
                    )
                    .navigationBarsPadding()
                    .padding(horizontal = 24.dp, vertical = 32.dp),
            ) {
                Column(
                    horizontalAlignment = Alignment.CenterHorizontally,
                    modifier = Modifier.fillMaxWidth(),
                ) {
                    if (!isActiveVideo && !(isActive && call.isGroup)) {
                        CallAvatar(name = remoteName)
                        Spacer(Modifier.height(20.dp))
                    }

                    if ((!isActiveVideo || call.isGroup) && !(isActive && call.isGroup)) {
                        Text(
                            text = titleText(strings, call, incoming, remoteName, conversationTitle),
                            color = AppColorPalette.TextPrimary,
                            fontSize = 24.sp,
                            fontWeight = FontWeight.Bold,
                            textAlign = TextAlign.Center,
                        )
                        Spacer(Modifier.height(8.dp))
                    }

                    Text(
                        text = statusText(strings, call, incoming, state, currentUserId),
                        color = AppColorPalette.TextSecondary,
                        fontSize = 14.sp,
                        textAlign = TextAlign.Center,
                    )

                    state.error?.let {
                        Spacer(Modifier.height(8.dp))
                        Text(it, color = AppColorPalette.Error, fontSize = 12.sp, textAlign = TextAlign.Center)
                        if (state.permissionState == CallPermissionState.PERMANENTLY_DENIED) {
                            TextButton(onClick = viewModel::openPermissionSettings) {
                                Text(strings.text("Open App Settings"))
                            }
                        }
                    }

                    Spacer(Modifier.height(28.dp))

                    when {
                        incoming -> IncomingControls(viewModel, strings, state.accepting)
                        call.status == CallStatus.RINGING -> OutgoingRingingControls(viewModel, strings, call.isGroup)
                        else -> ActiveControls(
                            viewModel = viewModel,
                            state = state,
                            isVideo = call.isVideo,
                            isGroup = call.isGroup,
                            isActive = isActive,
                            strings = strings,
                            onInviteClick = { showInviteSheet = true },
                        )
                    }
                }
            }
        }
    }

    if (showInviteSheet) {
        CallInviteSheet(
            candidates = inviteCandidates,
            onInvite = { userIds ->
                viewModel.invite(userIds)
                showInviteSheet = false
            },
            onDismiss = { showInviteSheet = false },
        )
    }
}

@Composable
private fun CallAvatar(name: String) {
    Box(
        modifier = Modifier
            .size(96.dp)
            .background(AppColorPalette.Primary, CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = name.take(2).uppercase(),
            color = AppColorPalette.OnPrimary,
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

@Composable
private fun IncomingControls(
    viewModel: CallViewModel,
    strings: org.example.project.presentation.localization.AppStrings,
    accepting: Boolean,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        CallCircleButton(
            background = AppColorPalette.LogoutDanger,
            icon = Icons.Filled.CallEnd,
            contentDescription = strings.text("Decline"),
            onClick = viewModel::decline,
            enabled = !accepting,
        )
        CallCircleButton(
            background = AppColorPalette.Success,
            icon = Icons.Filled.Call,
            contentDescription = strings.text("Answer"),
            onClick = viewModel::accept,
            size = 72.dp,
            iconSize = 32.dp,
            enabled = !accepting,
        )
    }
}

@Composable
private fun OutgoingRingingControls(
    viewModel: CallViewModel,
    strings: org.example.project.presentation.localization.AppStrings,
    isGroup: Boolean,
) {
    if (isGroup) return
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
    ) {
        CallCircleButton(
            background = AppColorPalette.LogoutDanger,
            icon = Icons.Filled.CallEnd,
            contentDescription = strings.text("Cancel"),
            onClick = viewModel::cancel,
        )
    }
}

@Composable
private fun ActiveControls(
    viewModel: CallViewModel,
    state: CallUiState,
    isVideo: Boolean,
    isGroup: Boolean,
    isActive: Boolean,
    strings: org.example.project.presentation.localization.AppStrings,
    onInviteClick: () -> Unit,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        if (isVideo) {
            CallCircleButton(
                background = if (state.cameraEnabled) AppColorPalette.SurfaceVariant else AppColorPalette.BorderStrong,
                icon = if (state.cameraEnabled) Icons.Filled.Videocam else Icons.Filled.VideocamOff,
                contentDescription = if (state.cameraEnabled) {
                    strings.text("Camera off")
                } else {
                    strings.text("Camera on")
                },
                onClick = viewModel::toggleCamera,
            )
        }
        CallCircleButton(
            background = if (state.muted) AppColorPalette.BorderStrong else AppColorPalette.SurfaceVariant,
            icon = if (state.muted) Icons.Filled.MicOff else Icons.Filled.Mic,
            contentDescription = if (state.muted) strings.text("Unmute") else strings.text("Mute"),
            onClick = viewModel::toggleMute,
        )
        if (isGroup && isActive) {
            CallCircleButton(
                background = AppColorPalette.SurfaceVariant,
                icon = Icons.Filled.PersonAdd,
                contentDescription = strings.text("Invite"),
                onClick = onInviteClick,
            )
            CallCircleButton(
                background = AppColorPalette.LogoutDanger,
                icon = Icons.Filled.CallEnd,
                contentDescription = strings.text("Leave"),
                onClick = viewModel::leave,
            )
        } else if (isGroup) {
            CallCircleButton(
                background = AppColorPalette.LogoutDanger,
                icon = Icons.Filled.CallEnd,
                contentDescription = strings.text("Leave"),
                onClick = viewModel::leave,
            )
        } else {
            CallCircleButton(
                background = AppColorPalette.LogoutDanger,
                icon = Icons.Filled.CallEnd,
                contentDescription = strings.text("End call"),
                onClick = viewModel::end,
            )
        }
    }
}

private fun titleText(
    strings: org.example.project.presentation.localization.AppStrings,
    call: org.example.project.domain.calls.WorkspaceCall,
    incoming: Boolean,
    remoteName: String,
    conversationTitle: String,
): String {
    if (incoming && call.isGroup) {
        val kind = if (call.isVideo) strings.text("Group video call") else strings.text("Group audio call")
        return "$remoteName — $kind"
    }
    if (call.isGroup && call.status == CallStatus.RINGING) {
        return conversationTitle.ifBlank { strings.text("Calling group…") }
    }
    return remoteName
}

private fun statusText(
    strings: org.example.project.presentation.localization.AppStrings,
    call: org.example.project.domain.calls.WorkspaceCall,
    incoming: Boolean,
    state: CallUiState,
    currentUserId: Int,
): String {
    val memberCount = call.participants.count { it.userId != currentUserId }
    return when {
        incoming && call.isGroup -> strings.text("{count} members").replace("{count}", memberCount.toString())
        incoming && call.isVideo -> strings.text("Incoming video call")
        incoming && state.accepting -> strings.text("Requesting permissions…")
        incoming -> strings.text("Incoming audio call")
        call.status == CallStatus.RINGING && call.initiatedByUserId == currentUserId && call.isGroup ->
            strings.text("{count} members").replace("{count}", memberCount.toString())
        call.status == CallStatus.RINGING && call.initiatedByUserId == currentUserId -> strings.text("Calling…")
        call.status == CallStatus.RINGING -> strings.text("Ringing…")
        call.isGroup && call.status == CallStatus.ACTIVE ->
            strings.text("{count} connected").replace("{count}", state.remoteParticipantCount.coerceAtLeast(1).toString())
        state.connectionLabel.isNotBlank() -> state.connectionLabel
        call.isVideo -> strings.text("Video call")
        else -> strings.text("Audio call")
    }
}

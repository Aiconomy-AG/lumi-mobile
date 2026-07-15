package org.example.project.presentation.calls

import org.example.project.domain.calls.CallMediaParticipant
import org.example.project.domain.calls.CallParticipant
import org.example.project.domain.calls.WorkspaceCall

data class CallTileParticipant(
    val userId: Int?,
    val identity: String,
    val name: String,
    val isLocal: Boolean,
    val cameraEnabled: Boolean,
    val isMuted: Boolean,
    val status: String? = null,
)

fun buildActiveCallTiles(
    call: WorkspaceCall,
    mediaParticipants: List<CallMediaParticipant>,
    currentUserId: Int,
    selfName: String,
    localCameraEnabled: Boolean,
): List<CallTileParticipant> {
    val joined = call.participants.filter { participant ->
        participant.status == "joined" || participant.userId == currentUserId
    }
    if (joined.isEmpty()) {
        return mediaParticipants.map { media ->
            CallTileParticipant(
                userId = if (media.isLocal) currentUserId else null,
                identity = media.identity,
                name = media.name,
                isLocal = media.isLocal,
                cameraEnabled = media.cameraEnabled,
                isMuted = media.isMuted,
                status = "joined",
            )
        }
    }
    return joined.map { participant ->
        val isLocal = participant.userId == currentUserId
        val media = mediaForParticipant(mediaParticipants, participant, isLocal)
        CallTileParticipant(
            userId = participant.userId,
            identity = media?.identity ?: participant.userId.toString(),
            name = participant.name.ifBlank { selfName.takeIf { isLocal } ?: "User ${participant.userId}" },
            isLocal = isLocal,
            cameraEnabled = when {
                isLocal -> localCameraEnabled
                else -> media?.cameraEnabled == true
            },
            isMuted = media?.isMuted == true,
            status = participant.status,
        )
    }
}

private fun mediaForParticipant(
    mediaParticipants: List<CallMediaParticipant>,
    participant: CallParticipant,
    isLocal: Boolean,
): CallMediaParticipant? {
    val userId = participant.userId.toString()
    return mediaParticipants.firstOrNull { media ->
        media.isLocal == isLocal && (
            media.identity == userId ||
                media.name.equals(participant.name, ignoreCase = true)
        )
    } ?: mediaParticipants.firstOrNull { it.identity == userId }
}

fun participantStatusLabel(status: String, strings: org.example.project.presentation.localization.AppStrings): String {
    return when (status.lowercase()) {
        "ringing", "invited" -> strings.text("Ringing")
        "joined" -> strings.text("Joined")
        "declined" -> strings.text("Declined")
        "missed" -> strings.text("Missed")
        "left" -> strings.text("Left")
        else -> status.replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
    }
}

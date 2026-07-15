package org.example.project.presentation.calls

enum class CallUiMode {
    Hidden,
    Incoming,
    OutgoingRinging,
    FullScreen,
    Minimized,
}

fun groupCallGridColumns(participantCount: Int): Int = when {
    participantCount <= 1 -> 1
    participantCount <= 4 -> 2
    else -> 3
}

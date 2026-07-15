package org.example.project.domain.calls

internal object CallPermissionPolicy {
    fun canUseCalls(
        state: CallPermissionState,
        hasAudio: Boolean,
        hasCamera: Boolean,
    ): Boolean = state == CallPermissionState.GRANTED && hasAudio && hasCamera

    fun matchesRequestedMedia(
        requestedType: String,
        returnedType: String,
        returnedMediaType: String,
    ): Boolean {
        if (requestedType != "video") return true
        return returnedType == "video" || returnedMediaType == "video"
    }
}

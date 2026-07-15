package org.example.project.domain.calls

import kotlin.test.Test
import kotlin.test.assertFalse
import kotlin.test.assertTrue

class CallPermissionPolicyTest {
    @Test
    fun callsAreBlockedUntilBothMediaPermissionsAreGranted() {
        assertFalse(
            CallPermissionPolicy.canUseCalls(
                state = CallPermissionState.DENIED,
                hasAudio = false,
                hasCamera = false,
            ),
        )
        assertFalse(
            CallPermissionPolicy.canUseCalls(
                state = CallPermissionState.GRANTED,
                hasAudio = true,
                hasCamera = false,
            ),
        )
        assertFalse(
            CallPermissionPolicy.canUseCalls(
                state = CallPermissionState.GRANTED,
                hasAudio = false,
                hasCamera = true,
            ),
        )
        assertTrue(
            CallPermissionPolicy.canUseCalls(
                state = CallPermissionState.GRANTED,
                hasAudio = true,
                hasCamera = true,
            ),
        )
    }

    @Test
    fun permanentDenialNeverAllowsCallActions() {
        assertFalse(
            CallPermissionPolicy.canUseCalls(
                state = CallPermissionState.PERMANENTLY_DENIED,
                hasAudio = true,
                hasCamera = true,
            ),
        )
    }

    @Test
    fun videoRequestCannotSilentlyBecomeAudio() {
        assertFalse(
            CallPermissionPolicy.matchesRequestedMedia(
                requestedType = "video",
                returnedType = "audio",
                returnedMediaType = "audio",
            ),
        )
        assertTrue(
            CallPermissionPolicy.matchesRequestedMedia(
                requestedType = "video",
                returnedType = "video",
                returnedMediaType = "video",
            ),
        )
        assertTrue(
            CallPermissionPolicy.matchesRequestedMedia(
                requestedType = "video",
                returnedType = "audio",
                returnedMediaType = "video",
            ),
        )
    }
}

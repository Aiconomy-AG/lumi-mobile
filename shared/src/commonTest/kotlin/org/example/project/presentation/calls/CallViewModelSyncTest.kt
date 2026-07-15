package org.example.project.presentation.calls

import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.runBlocking
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertTrue
import org.example.project.domain.calls.CallApi
import org.example.project.domain.calls.CallConnection
import org.example.project.domain.calls.CallHistoryPage
import org.example.project.domain.calls.CallIdentity
import org.example.project.domain.calls.CallPresenceRealtimeApi
import org.example.project.domain.calls.CallRealtimeApi
import org.example.project.domain.calls.CallStatus
import org.example.project.domain.calls.CallMediaParticipant
import org.example.project.domain.calls.PlatformCallController
import org.example.project.domain.calls.WorkspaceCall

class CallViewModelSyncTest {
    private val instanceId = "test-instance-1"
    private val callerId = 1
    private val calleeId = 2

    @Test
    fun acceptPreparesIncomingAnswerBeforeApiCall() = runBlocking {
        val platform = RecordingPlatform()
        val api = FakeCallApi(
            ringingCall = sampleCall(status = CallStatus.RINGING, initiatedBy = callerId),
        )
        val viewModel = createViewModel(api = api, platform = platform, userId = calleeId)

        viewModel.openFromNotification("call-1", action = null)
        delay(100)

        viewModel.accept()
        delay(100)

        assertTrue(platform.answeredCallIds.contains("call-1"))
        viewModel.close()
    }

    @Test
    fun notificationAnswerDoesNotShowIncomingTwice() = runBlocking {
        val platform = RecordingPlatform()
        val api = FakeCallApi(
            ringingCall = sampleCall(status = CallStatus.RINGING, initiatedBy = callerId),
        )
        val viewModel = createViewModel(api = api, platform = platform, userId = calleeId)

        viewModel.openFromNotification("call-1", action = null)
        delay(100)
        assertEquals(1, platform.showIncomingCount)

        viewModel.openFromNotification("call-1", action = "answer")
        delay(100)

        assertEquals(1, platform.showIncomingCount)
        viewModel.close()
    }

    @Test
    fun callerTransitionsFromRingingToActiveOnRealtimeUpdate() = runBlocking {
        val platform = RecordingPlatform()
        val api = FakeCallApi(
            ringingCall = sampleCall(status = CallStatus.RINGING, initiatedBy = callerId),
            fetchedCall = sampleCall(status = CallStatus.RINGING, initiatedBy = callerId),
        )
        val realtime = FakeCallRealtime()
        val viewModel = createViewModel(
            api = api,
            platform = platform,
            userId = callerId,
            realtime = realtime,
        )

        viewModel.openFromNotification("call-1", action = null)
        delay(100)
        assertEquals(CallStatus.RINGING, viewModel.state.value.call?.status)

        realtime.emit(
            sampleCall(
                status = CallStatus.ACTIVE,
                initiatedBy = callerId,
                connection = CallConnection("wss://livekit", "token"),
            ),
        )
        delay(200)

        assertEquals(CallStatus.ACTIVE, viewModel.state.value.call?.status)
        assertFalse(viewModel.state.value.connectionLabel.contains("Calling", ignoreCase = true))
        viewModel.close()
    }

    @Test
    fun openFromNotificationWithActiveCallUpdatesCallerState() = runBlocking {
        val platform = RecordingPlatform()
        val api = FakeCallApi(
            ringingCall = sampleCall(status = CallStatus.RINGING, initiatedBy = callerId),
            fetchedCall = sampleCall(
                status = CallStatus.ACTIVE,
                initiatedBy = callerId,
                connection = CallConnection("wss://livekit", "token"),
            ),
        )
        val viewModel = createViewModel(api = api, platform = platform, userId = callerId)

        viewModel.openFromNotification("call-1", action = null)
        delay(100)

        assertEquals(CallStatus.ACTIVE, viewModel.state.value.call?.status)
        viewModel.close()
    }

    private fun createViewModel(
        api: FakeCallApi,
        platform: RecordingPlatform,
        userId: Int,
        realtime: FakeCallRealtime = FakeCallRealtime(),
    ): CallViewModel {
        return CallViewModel(
            currentUserId = userId,
            api = api,
            realtime = realtime,
            presenceRealtime = null,
            platform = platform,
            clientInstanceId = instanceId,
        )
    }

    private fun sampleCall(
        status: CallStatus,
        initiatedBy: Int,
        connection: CallConnection? = null,
        answeredClientInstanceId: String? = null,
    ): WorkspaceCall = WorkspaceCall(
        id = "call-1",
        initiatedByUserId = initiatedBy,
        caller = CallIdentity(id = initiatedBy, name = "Caller"),
        participants = emptyList(),
        status = status,
        connection = connection,
        answeredClientInstanceId = answeredClientInstanceId,
    )

    private class FakeCallApi(
        private val ringingCall: WorkspaceCall,
        private val fetchedCall: WorkspaceCall? = null,
    ) : CallApi {
        override suspend fun start(
            calleeIds: List<Int>,
            clientInstanceId: String,
            type: String,
            mode: String?,
        ): WorkspaceCall = ringingCall.copy(connection = CallConnection("wss://livekit", "caller-token"))

        override suspend fun startFromConversation(
            conversationId: Int,
            clientInstanceId: String,
            type: String,
        ): WorkspaceCall = ringingCall

        override suspend fun get(callId: String, clientInstanceId: String): WorkspaceCall =
            fetchedCall ?: ringingCall

        override suspend fun active(clientInstanceId: String): WorkspaceCall? = null

        override suspend fun history(page: Int, perPage: Int): CallHistoryPage = CallHistoryPage()

        override suspend fun accept(callId: String, clientInstanceId: String): WorkspaceCall =
            ringingCall.copy(
                status = CallStatus.ACTIVE,
                answeredClientInstanceId = clientInstanceId,
                connection = CallConnection("wss://livekit", "callee-token"),
            )

        override suspend fun decline(callId: String): WorkspaceCall = ringingCall
        override suspend fun cancel(callId: String): WorkspaceCall = ringingCall
        override suspend fun leave(callId: String): WorkspaceCall = ringingCall
        override suspend fun invite(callId: String, userIds: List<Int>): WorkspaceCall = ringingCall
        override suspend fun end(callId: String): WorkspaceCall = ringingCall
    }

    private class FakeCallRealtime : CallRealtimeApi {
        private val eventsFlow = MutableSharedFlow<WorkspaceCall>(extraBufferCapacity = 8)

        override fun events(userId: Int): Flow<WorkspaceCall> = eventsFlow

        suspend fun emit(call: WorkspaceCall) {
            eventsFlow.emit(call)
        }
    }

    private class RecordingPlatform : PlatformCallController {
        private val _remoteParticipantCount = MutableStateFlow(0)
        private val _remoteCameraEnabled = MutableStateFlow(false)
        private val _mediaParticipants = MutableStateFlow<List<CallMediaParticipant>>(emptyList())
        val answeredCallIds = mutableListOf<String>()
        var showIncomingCount = 0

        override val remoteCameraEnabled: StateFlow<Boolean> = _remoteCameraEnabled.asStateFlow()
        override val remoteParticipantCount: StateFlow<Int> = _remoteParticipantCount.asStateFlow()
        override val mediaParticipants: StateFlow<List<CallMediaParticipant>> = _mediaParticipants.asStateFlow()

        override suspend fun connect(call: WorkspaceCall) = Unit
        override suspend fun disconnect() = Unit
        override suspend fun setMuted(muted: Boolean) = Unit
        override suspend fun setCameraEnabled(enabled: Boolean) = Unit
        override fun isMuted(): Boolean = false
        override fun isCameraEnabled(): Boolean = false
        override fun showIncoming(call: WorkspaceCall) {
            showIncomingCount += 1
        }

        override fun dismissIncoming(callId: String) = Unit
        override fun onIncomingAnswered(callId: String) {
            answeredCallIds += callId
        }
    }
}

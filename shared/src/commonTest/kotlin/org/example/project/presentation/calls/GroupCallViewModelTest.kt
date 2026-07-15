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
import org.example.project.domain.calls.CallMediaParticipant
import org.example.project.domain.calls.CallParticipant
import org.example.project.domain.calls.CallPresenceRealtimeApi
import org.example.project.domain.calls.CallRealtimeApi
import org.example.project.domain.calls.CallStatus
import org.example.project.domain.calls.PlatformCallController
import org.example.project.domain.calls.WorkspaceCall

class GroupCallViewModelTest {
    private val instanceId = "group-test-instance"
    private val callerId = 10
    private val calleeA = 20
    private val calleeB = 30

    @Test
    fun groupConversationUsesWorkspaceEndpointCondition() {
        assertTrue(
            shouldUseConversationEndpoint(
                conversationId = 99,
                calleeCount = 2,
                isGroupConversation = true,
                type = "video",
            ),
        )
        assertTrue(
            shouldUseConversationEndpoint(
                conversationId = 5,
                calleeCount = 1,
                isGroupConversation = false,
                type = "audio",
            ),
        )
        assertFalse(
            shouldUseConversationEndpoint(
                conversationId = null,
                calleeCount = 2,
                isGroupConversation = true,
                type = "video",
            ),
        )
    }

    @Test
    fun partialDeclineKeepsCallerRingingState() = runBlocking {
        val api = RecordingGroupCallApi(
            initialCall = groupRingingCall(
                participants = listOf(
                    participant(calleeA, "ringing"),
                    participant(calleeB, "ringing"),
                ),
            ),
        )
        val realtime = FakeCallRealtime()
        val viewModel = createViewModel(api = api, userId = callerId, realtime = realtime)
        viewModel.openFromNotification("group-call-1", action = null)
        delay(100)

        realtime.emit(
            groupRingingCall(
                participants = listOf(
                    participant(calleeA, "declined"),
                    participant(calleeB, "ringing"),
                ),
            ),
        )
        delay(100)

        assertEquals(CallStatus.RINGING, viewModel.state.value.call?.status)
        assertEquals(CallUiMode.OutgoingRinging, viewModel.state.value.uiMode)
        viewModel.close()
    }

    @Test
    fun activeGroupEndRoutesToLeave() = runBlocking {
        val api = RecordingGroupCallApi(
            initialCall = groupRingingCall(status = CallStatus.ACTIVE),
        )
        val viewModel = createViewModel(api = api, userId = callerId)
        viewModel.openFromNotification("group-call-1", action = null)
        delay(100)

        viewModel.end()
        delay(100)

        assertTrue(api.leaveCalled)
        assertEquals(false, api.endCalled)
        viewModel.close()
    }

    @Test
    fun inviteRefreshesParticipantsWithoutReconnect() = runBlocking {
        val api = RecordingGroupCallApi(
            initialCall = groupRingingCall(status = CallStatus.ACTIVE),
        )
        val platform = RecordingGroupPlatform()
        val viewModel = createViewModel(api = api, platform = platform, userId = callerId)
        viewModel.openFromNotification("group-call-1", action = null)
        delay(100)
        val connectsBeforeInvite = platform.connectCount

        viewModel.invite(listOf(40))
        delay(100)

        assertEquals(1, api.inviteCount)
        assertEquals(3, viewModel.state.value.call?.participants?.size)
        assertEquals(connectsBeforeInvite, platform.connectCount)
        viewModel.close()
    }

    @Test
    fun minimizeAndExpandKeepCallConnected() = runBlocking {
        val api = RecordingGroupCallApi(
            initialCall = groupRingingCall(status = CallStatus.ACTIVE),
        )
        val platform = RecordingGroupPlatform()
        val viewModel = createViewModel(api = api, platform = platform, userId = callerId)
        viewModel.openFromNotification("group-call-1", action = null)
        delay(100)

        viewModel.minimize()
        assertEquals(CallUiMode.Minimized, viewModel.state.value.uiMode)

        viewModel.expand()
        assertEquals(CallUiMode.FullScreen, viewModel.state.value.uiMode)
        assertEquals(0, platform.disconnectCount)
        viewModel.close()
    }

    @Test
    fun mediaParticipantsFlowUpdatesUiState() = runBlocking {
        val platform = RecordingGroupPlatform()
        val viewModel = createViewModel(api = RecordingGroupCallApi(groupRingingCall()), platform = platform, userId = callerId)
        viewModel.openFromNotification("group-call-1", action = null)
        delay(50)

        platform.emitParticipants(
            listOf(
                CallMediaParticipant("20", "Alice", false, true, false, true),
                CallMediaParticipant("local", "You", true, true, false, true),
            ),
        )
        delay(50)

        assertEquals(2, viewModel.state.value.mediaParticipants.size)
        viewModel.close()
    }

    private fun createViewModel(
        api: RecordingGroupCallApi,
        platform: RecordingGroupPlatform = RecordingGroupPlatform(),
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

    private fun groupRingingCall(
        status: CallStatus = CallStatus.RINGING,
        participants: List<CallParticipant> = listOf(
            participant(calleeA, "ringing"),
            participant(calleeB, "invited"),
        ),
    ): WorkspaceCall = WorkspaceCall(
        id = "group-call-1",
        conversationId = 99,
        initiatedByUserId = callerId,
        caller = CallIdentity(id = callerId, name = "Caller"),
        participants = participants,
        mode = "group",
        type = "video",
        mediaType = "video",
        status = status,
        connection = CallConnection("wss://livekit", "token"),
    )

    private fun participant(userId: Int, status: String): CallParticipant = CallParticipant(
        userId = userId,
        name = "User $userId",
        role = "callee",
        status = status,
    )

    private class RecordingGroupCallApi(
        private val initialCall: WorkspaceCall,
    ) : CallApi {
        var usedConversationEndpoint = false
        var lastConversationId: Int? = null
        var lastConversationType: String? = null
        var inviteCount = 0
        var leaveCalled = false
        var endCalled = false

        override suspend fun start(
            calleeIds: List<Int>,
            clientInstanceId: String,
            type: String,
            mode: String?,
        ): WorkspaceCall = initialCall

        override suspend fun startFromConversation(
            conversationId: Int,
            clientInstanceId: String,
            type: String,
        ): WorkspaceCall {
            usedConversationEndpoint = true
            lastConversationId = conversationId
            lastConversationType = type
            return initialCall
        }

        override suspend fun get(callId: String, clientInstanceId: String): WorkspaceCall = initialCall

        override suspend fun active(clientInstanceId: String): WorkspaceCall? = null

        override suspend fun history(page: Int, perPage: Int): CallHistoryPage = CallHistoryPage()

        override suspend fun accept(callId: String, clientInstanceId: String): WorkspaceCall = initialCall

        override suspend fun decline(callId: String): WorkspaceCall = initialCall

        override suspend fun cancel(callId: String): WorkspaceCall = initialCall

        override suspend fun leave(callId: String): WorkspaceCall {
            leaveCalled = true
            return initialCall
        }

        override suspend fun invite(callId: String, userIds: List<Int>): WorkspaceCall {
            inviteCount += 1
            return initialCall.copy(
                participants = initialCall.participants + userIds.map {
                    CallParticipant(it, "Invited $it", "callee", "invited")
                },
            )
        }

        override suspend fun end(callId: String): WorkspaceCall {
            endCalled = true
            return initialCall
        }
    }

    private class FakeCallRealtime : CallRealtimeApi {
        private val eventsFlow = MutableSharedFlow<WorkspaceCall>(extraBufferCapacity = 8)

        override fun events(userId: Int): Flow<WorkspaceCall> = eventsFlow

        suspend fun emit(call: WorkspaceCall) {
            eventsFlow.emit(call)
        }
    }

    private class RecordingGroupPlatform : PlatformCallController {
        private val _remoteParticipantCount = MutableStateFlow(0)
        private val _remoteCameraEnabled = MutableStateFlow(false)
        private val _mediaParticipants = MutableStateFlow<List<CallMediaParticipant>>(emptyList())
        var connectCount = 0
        var disconnectCount = 0

        override val remoteCameraEnabled: StateFlow<Boolean> = _remoteCameraEnabled.asStateFlow()
        override val remoteParticipantCount: StateFlow<Int> = _remoteParticipantCount.asStateFlow()
        override val mediaParticipants: StateFlow<List<CallMediaParticipant>> = _mediaParticipants.asStateFlow()

        override suspend fun connect(call: WorkspaceCall) {
            connectCount += 1
        }

        override suspend fun disconnect() {
            disconnectCount += 1
        }

        override suspend fun setMuted(muted: Boolean) = Unit
        override suspend fun setCameraEnabled(enabled: Boolean) = Unit
        override fun isMuted(): Boolean = false
        override fun isCameraEnabled(): Boolean = false
        override fun showIncoming(call: WorkspaceCall) = Unit
        override fun dismissIncoming(callId: String) = Unit
        override fun onIncomingAnswered(callId: String) = Unit

        fun emitParticipants(participants: List<CallMediaParticipant>) {
            _mediaParticipants.value = participants
        }
    }
}

package org.example.project.presentation.chat

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import org.example.project.data.chat.ChatMockApiService
import org.example.project.data.employee.EmployeeMockApiService
import org.example.project.domain.chat.ChatMessage
import org.example.project.domain.chat.Conversation
import org.example.project.domain.chat.ConversationType
import org.example.project.domain.employee.Employee
import org.example.project.domain.employee.EmployeeApi

data class ChatConversationItem(
    val conversation: Conversation,
    val title: String,
    val subtitle: String,
    val initials: String,
    val lastMessage: String,
    val lastSentAt: String,
)

data class ChatUiState(
    val isLoading: Boolean = false,
    val conversations: List<ChatConversationItem> = emptyList(),
    val selectedConversation: ChatConversationItem? = null,
    val messages: List<ChatMessage> = emptyList(),
    val employeesById: Map<Int, Employee> = emptyMap(),
    val searchQuery: String = "",
    val messageDraft: String = "",
    val error: String? = null,
) {
    val filteredConversations: List<ChatConversationItem>
        get() = if (searchQuery.isBlank()) {
            conversations
        } else {
            conversations.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.subtitle.contains(searchQuery, ignoreCase = true)
            }
        }
}

class ChatViewModel(
    private val currentEmployeeId: Int,
    private val chatApi: ChatMockApiService = ChatMockApiService(),
    private val employeeApi: EmployeeApi = EmployeeMockApiService()
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Default)
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()

    init {
        loadChat()
    }

    fun loadChat() {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val employees = employeeApi.getEmployees()
                val employeesById = employees.associateBy { it.id }
                val conversations = chatApi.getConversations(currentEmployeeId)

                val items = conversations.map { conversation ->
                    val participants = chatApi.getParticipants(conversation.id)
                    val conversationMessages = chatApi.getMessages(conversation.id)
                    val lastMessage = conversationMessages.lastOrNull()

                    val otherParticipants = participants
                        .mapNotNull { employeesById[it.employeeId] }
                        .filter { it.id != currentEmployeeId }

                    val title = when (conversation.type) {
                        ConversationType.DIRECT -> otherParticipants.firstOrNull()?.name ?: "Chat direct"
                        ConversationType.GROUP -> conversation.name ?: "Chat de grup"
                    }

                    ChatConversationItem(
                        conversation = conversation,
                        title = title,
                        subtitle = otherParticipants.joinToString { it.name },
                        initials = title.initials(),
                        lastMessage = lastMessage?.messageText ?: "Nu exista mesaje inca.",
                        lastSentAt = lastMessage?.sentAt ?: "",
                    )
                }

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    conversations = items,
                    employeesById = employeesById,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Nu am putut incarca chat-urile.",
                )
            }
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun selectConversation(conversation: ChatConversationItem) {
        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                selectedConversation = conversation,
                isLoading = true,
                error = null,
            )

            try {
                val messages = chatApi.getMessages(conversation.conversation.id)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    messages = messages,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Nu am putut incarca mesajele.",
                )
            }
        }
    }

    fun backToConversationList() {
        _uiState.value = _uiState.value.copy(
            selectedConversation = null,
            messages = emptyList(),
            messageDraft = "",
        )
    }

    fun onMessageDraftChanged(value: String) {
        _uiState.value = _uiState.value.copy(messageDraft = value)
    }

    fun sendMessage() {
        val selectedConversation = _uiState.value.selectedConversation ?: return
        val text = _uiState.value.messageDraft.trim()

        if (text.isBlank()) return

        viewModelScope.launch {
            try {
                val message = chatApi.sendMessage(
                    conversationId = selectedConversation.conversation.id,
                    senderId = currentEmployeeId,
                    messageText = text,
                )

                _uiState.value = _uiState.value.copy(
                    messages = _uiState.value.messages + message,
                    messageDraft = "",
                    conversations = _uiState.value.conversations.map {
                        if (it.conversation.id == selectedConversation.conversation.id) {
                            it.copy(lastMessage = message.messageText, lastSentAt = message.sentAt)
                        } else {
                            it
                        }
                    },
                    selectedConversation = selectedConversation.copy(
                        lastMessage = message.messageText,
                        lastSentAt = message.sentAt,
                    ),
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Mesajul nu a putut fi trimis.",
                )
            }
        }
    }
    private fun String.initials(): String {
        return split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifBlank { "?" }
    }

}
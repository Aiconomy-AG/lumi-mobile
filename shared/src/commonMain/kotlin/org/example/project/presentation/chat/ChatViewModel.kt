package org.example.project.presentation.chat

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.currentCoroutineContext
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.catch
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import org.example.project.data.accounts.User
import org.example.project.data.accounts.UserApiService
import org.example.project.data.chat.ChatReadStateStorage
import org.example.project.data.chat.chatActivitySortKey
import org.example.project.data.chat.currentActivitySortKey
import org.example.project.data.chat.currentTimeLabel
import org.example.project.domain.chat.ChatApi
import org.example.project.domain.chat.ChatMessage
import org.example.project.domain.chat.ChatNotificationEvent
import org.example.project.domain.chat.ChatRealtimeApi
import org.example.project.domain.chat.Conversation
import org.example.project.domain.chat.ConversationType

data class ChatConversationItem(
    val conversation: Conversation,
    val title: String,
    val subtitle: String,
    val initials: String,
    val lastMessage: String,
    val lastSentAt: String,
    val lastMessageId: Int? = null,
    val lastMessageSenderId: Int? = null,
    val lastActivitySortKey: Long = 0L,
)

data class ChatContactItem(
    val user: User,
    val conversation: ChatConversationItem?,
    val hasUnread: Boolean = false,
) {
    val title: String = user.name
    val subtitle: String = user.email
    val initials: String = user.initials.ifBlank { "?" }
    val lastMessage: String = conversation?.lastMessage ?: "Incepe o conversatie."
    val lastSentAt: String = conversation?.lastSentAt ?: ""
}

data class ChatUiState(
    val isLoading: Boolean = false,
    val conversations: List<ChatConversationItem> = emptyList(),
    val contacts: List<ChatContactItem> = emptyList(),
    val selectedConversation: ChatConversationItem? = null,
    val messages: List<ChatMessage> = emptyList(),
    val usersById: Map<Int, User> = emptyMap(),
    val searchQuery: String = "",
    val messageDraft: String = "",
    val error: String? = null,
) {
    val filteredContacts: List<ChatContactItem>
        get() = if (searchQuery.isBlank()) {
            contacts
        } else {
            contacts.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.subtitle.contains(searchQuery, ignoreCase = true) ||
                        it.user.role.displayName.contains(searchQuery, ignoreCase = true)
            }
        }

    val sortedContacts: List<ChatContactItem>
        get() = filteredContacts.sortedWith(
            compareByDescending<ChatContactItem> { it.conversation?.lastActivitySortKey ?: 0L }
                .thenBy { it.title.lowercase() }
        )
}

class ChatViewModel(
    private val currentEmployeeId: Int,
    private val userApi: UserApiService,
    private val chatApi: ChatApi,
    private val chatRealtimeApi: ChatRealtimeApi? = null,
) {
    private val viewModelScope = CoroutineScope(Dispatchers.Default)
    private val _uiState = MutableStateFlow(ChatUiState())
    val uiState: StateFlow<ChatUiState> = _uiState.asStateFlow()
    private var nextOptimisticMessageId = -1
    private var readStateByConversationId = ChatReadStateStorage.load(currentEmployeeId).toMutableMap()

    init {
        loadChat()
        listenForRealtimeChat()
        startPollingFallback()
    }

    fun loadChat() {
        viewModelScope.launch {
            refreshChat(showLoading = true, showError = true)
        }
    }

    fun onSearchQueryChanged(query: String) {
        _uiState.value = _uiState.value.copy(searchQuery = query)
    }

    fun selectContact(contact: ChatContactItem) {
        contact.conversation?.let {
            selectConversation(it)
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)

            try {
                val conversation = chatApi.createDirectConversation(contact.user.id)
                val conversationItem = conversation.toConversationItem(
                    usersById = _uiState.value.usersById,
                    participants = listOf(currentEmployeeId, contact.user.id),
                    messages = emptyList(),
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    conversations = _uiState.value.conversations + conversationItem,
                    contacts = finalizeContacts(
                        _uiState.value.contacts.map {
                            if (it.user.id == contact.user.id) {
                                it.copy(conversation = conversationItem)
                            } else {
                                it
                            }
                        }
                    ),
                    selectedConversation = conversationItem,
                    messages = emptyList(),
                    messageDraft = "",
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Nu am putut porni conversatia.",
                )
            }
        }
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
                markConversationAsRead(conversation.conversation.id, messages)
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    messages = messages,
                    contacts = finalizeContacts(_uiState.value.contacts),
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Nu am putut incarca mesajele.",
                )
            }
        }
    }

    fun openConversationById(conversationId: Int) {
        viewModelScope.launch {
            val existing = _uiState.value.conversations
                .firstOrNull { it.conversation.id == conversationId }

            if (existing != null) {
                selectConversation(existing)
                return@launch
            }

            refreshChat(showLoading = false, showError = false)
            val conversation = _uiState.value.conversations
                .firstOrNull { it.conversation.id == conversationId }

            if (conversation != null) {
                selectConversation(conversation)
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

        val optimisticMessage = ChatMessage(
            id = nextOptimisticMessageId--,
            conversationId = selectedConversation.conversation.id,
            senderId = currentEmployeeId,
            messageText = text,
            sentAt = currentTimeLabel(),
        )
        val optimisticConversation = selectedConversation.copy(
            lastMessage = optimisticMessage.messageText,
            lastSentAt = optimisticMessage.sentAt,
            lastMessageId = optimisticMessage.id,
            lastMessageSenderId = optimisticMessage.senderId,
            lastActivitySortKey = currentActivitySortKey(),
        )

        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages + optimisticMessage,
            messageDraft = "",
            conversations = sortConversations(
                _uiState.value.conversations.map {
                    if (it.conversation.id == selectedConversation.conversation.id) {
                        optimisticConversation
                    } else {
                        it
                    }
                }
            ),
            selectedConversation = optimisticConversation,
            contacts = finalizeContacts(
                _uiState.value.contacts.map {
                    if (it.conversation?.conversation?.id == selectedConversation.conversation.id) {
                        it.copy(conversation = optimisticConversation)
                    } else {
                        it
                    }
                }
            ),
            error = null,
        )

        viewModelScope.launch {
            try {
                val message = chatApi.sendMessage(
                    conversationId = selectedConversation.conversation.id,
                    senderId = currentEmployeeId,
                    messageText = text,
                )

                replaceOptimisticMessage(
                    optimisticMessage = optimisticMessage,
                    savedMessage = message,
                )
            } catch (e: Exception) {
                removeOptimisticMessage(optimisticMessage)
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Mesajul nu a putut fi trimis.",
                )
            }
        }
    }

    private fun listenForRealtimeChat() {
        val realtimeApi = chatRealtimeApi ?: return

        viewModelScope.launch {
            realtimeApi.notificationEvents(currentEmployeeId)
                .catch { exception ->
                    _uiState.value = _uiState.value.copy(
                        error = exception.message ?: "Conexiunea real-time pentru chat nu este disponibila.",
                    )
                }
                .collect { event ->
                    handleRealtimeNotification(event)
                }
        }
    }

    private fun startPollingFallback() {
        viewModelScope.launch {
            while (currentCoroutineContext().isActive) {
                delay(CHAT_REFRESH_INTERVAL_MS)
                refreshChat(showLoading = false, showError = false)
            }
        }
    }

    private suspend fun refreshChat(showLoading: Boolean, showError: Boolean) {
        if (showLoading) {
            _uiState.value = _uiState.value.copy(isLoading = true, error = null)
        }

        try {
            val users = userApi.getUsers().getOrThrow()
            val usersById = users.associateBy { it.id }
            val conversations = chatApi.getConversations(currentEmployeeId)
            val participantIdsByConversation = conversations.associate { conversation ->
                conversation.id to chatApi.getParticipants(conversation.id).map { it.employeeId }
            }

            val items = conversations.map { conversation ->
                val conversationMessages = chatApi.getMessages(conversation.id)
                conversation.toConversationItem(
                    usersById = usersById,
                    participants = participantIdsByConversation[conversation.id].orEmpty(),
                    messages = conversationMessages,
                )
            }
            val directConversationByUserId = conversations
                .zip(items)
                .mapNotNull { (conversation, item) ->
                    if (conversation.type != ConversationType.DIRECT) return@mapNotNull null
                    val otherUserId = participantIdsByConversation[conversation.id]
                        .orEmpty()
                        .firstOrNull { it != currentEmployeeId }
                        ?: return@mapNotNull null

                    otherUserId to item
                }
                .toMap()
            val contacts = buildContacts(
                users = users,
                directConversationByUserId = directConversationByUserId,
            )
            val selectedConversationId = _uiState.value.selectedConversation?.conversation?.id
            val selectedConversation = selectedConversationId?.let { id ->
                items.firstOrNull { it.conversation.id == id }
            }
            val selectedMessages = selectedConversationId?.let { chatApi.getMessages(it) }
            val mergedSelectedMessages = if (selectedConversationId != null && selectedMessages != null) {
                mergeWithOptimisticMessages(
                    serverMessages = selectedMessages,
                    currentMessages = _uiState.value.messages,
                    conversationId = selectedConversationId,
                )
            } else {
                null
            }

            if (selectedConversationId != null && mergedSelectedMessages != null) {
                markConversationAsRead(selectedConversationId, mergedSelectedMessages)
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                conversations = sortConversations(items),
                contacts = contacts,
                selectedConversation = selectedConversation ?: _uiState.value.selectedConversation,
                messages = mergedSelectedMessages ?: _uiState.value.messages,
                usersById = usersById,
                error = null,
            )
        } catch (e: Exception) {
            if (showError) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Nu am putut incarca chat-urile.",
                )
            } else {
                _uiState.value = _uiState.value.copy(isLoading = false)
            }
        }
    }

    private suspend fun handleRealtimeNotification(event: ChatNotificationEvent) {
        when (event.type) {
            "chat_message_received" -> {
                val conversationId = event.conversationId ?: return
                refreshConversation(conversationId)
            }

            "chat_added_to_conversation" -> loadChat()
        }
    }

    private suspend fun refreshConversation(conversationId: Int) {
        val existingConversation = _uiState.value.conversations
            .firstOrNull { it.conversation.id == conversationId }

        if (existingConversation == null) {
            loadChat()
            return
        }

        val messages = chatApi.getMessages(conversationId)
        val currentState = _uiState.value
        val mergedMessages = mergeWithOptimisticMessages(
            serverMessages = messages,
            currentMessages = currentState.messages,
            conversationId = conversationId,
        )
        val conversation = currentState.conversations
            .firstOrNull { it.conversation.id == conversationId }
            ?: existingConversation
        val lastMessage = mergedMessages.lastOrNull()
        val updatedConversation = conversation.copy(
            lastMessage = lastMessage?.messageText ?: conversation.lastMessage,
            lastSentAt = lastMessage?.sentAt ?: conversation.lastSentAt,
            lastMessageId = lastMessage?.id?.takeIf { it > 0 } ?: conversation.lastMessageId,
            lastMessageSenderId = lastMessage?.senderId ?: conversation.lastMessageSenderId,
            lastActivitySortKey = chatActivitySortKey(lastMessage?.sentAt ?: conversation.lastSentAt),
        )

        val isSelected = currentState.selectedConversation?.conversation?.id == conversationId
        if (isSelected) {
            markConversationAsRead(conversationId, mergedMessages)
        }

        val nextConversations = sortConversations(
            currentState.conversations.map {
                if (it.conversation.id == conversationId) updatedConversation else it
            }
        )

        _uiState.value = currentState.copy(
            conversations = nextConversations,
            contacts = finalizeContacts(
                currentState.contacts.map {
                    if (it.conversation?.conversation?.id == conversationId) {
                        it.copy(conversation = updatedConversation)
                    } else {
                        it
                    }
                }
            ),
            selectedConversation = currentState.selectedConversation?.let {
                if (it.conversation.id == conversationId) updatedConversation else it
            },
            messages = if (isSelected) {
                mergedMessages
            } else {
                currentState.messages
            },
            error = null,
        )
    }

    private fun replaceOptimisticMessage(
        optimisticMessage: ChatMessage,
        savedMessage: ChatMessage,
    ) {
        val currentState = _uiState.value
        val nextMessages = currentState.messages
            .map { if (it.id == optimisticMessage.id) savedMessage else it }
            .let { messages ->
                if (messages.any { it.id == savedMessage.id }) {
                    messages
                } else {
                    messages + savedMessage
                }
            }
            .distinctBy { it.id }
        val lastMessage = nextMessages
            .lastOrNull { it.conversationId == savedMessage.conversationId }
            ?: savedMessage
        val updatedConversation = currentState.selectedConversation
            ?.takeIf { it.conversation.id == savedMessage.conversationId }
            ?.copy(
                lastMessage = lastMessage.messageText,
                lastSentAt = lastMessage.sentAt,
                lastMessageId = lastMessage.id,
                lastMessageSenderId = lastMessage.senderId,
                lastActivitySortKey = chatActivitySortKey(lastMessage.sentAt),
            )

        markConversationAsRead(savedMessage.conversationId, nextMessages)

        _uiState.value = currentState.copy(
            messages = nextMessages,
            conversations = sortConversations(
                currentState.conversations.map {
                    if (it.conversation.id == savedMessage.conversationId) {
                        it.copy(
                            lastMessage = lastMessage.messageText,
                            lastSentAt = lastMessage.sentAt,
                            lastMessageId = lastMessage.id,
                            lastMessageSenderId = lastMessage.senderId,
                            lastActivitySortKey = chatActivitySortKey(lastMessage.sentAt),
                        )
                    } else {
                        it
                    }
                }
            ),
            selectedConversation = updatedConversation ?: currentState.selectedConversation,
            contacts = finalizeContacts(
                currentState.contacts.map {
                    if (it.conversation?.conversation?.id == savedMessage.conversationId) {
                        it.copy(
                            conversation = it.conversation.copy(
                                lastMessage = lastMessage.messageText,
                                lastSentAt = lastMessage.sentAt,
                                lastMessageId = lastMessage.id,
                                lastMessageSenderId = lastMessage.senderId,
                                lastActivitySortKey = chatActivitySortKey(lastMessage.sentAt),
                            )
                        )
                    } else {
                        it
                    }
                }
            ),
        )
    }

    private fun removeOptimisticMessage(message: ChatMessage) {
        val currentState = _uiState.value
        val nextMessages = currentState.messages.filterNot { it.id == message.id }
        val lastMessage = nextMessages.lastOrNull { it.conversationId == message.conversationId }
        val fallbackLastMessage = lastMessage?.messageText ?: "Nu exista mesaje inca."
        val fallbackLastSentAt = lastMessage?.sentAt ?: ""

        _uiState.value = currentState.copy(
            messages = nextMessages,
            conversations = sortConversations(
                currentState.conversations.map {
                    if (it.conversation.id == message.conversationId) {
                        it.copy(
                            lastMessage = fallbackLastMessage,
                            lastSentAt = fallbackLastSentAt,
                            lastMessageId = lastMessage?.id?.takeIf { id -> id > 0 },
                            lastMessageSenderId = lastMessage?.senderId,
                            lastActivitySortKey = chatActivitySortKey(fallbackLastSentAt),
                        )
                    } else {
                        it
                    }
                }
            ),
            selectedConversation = currentState.selectedConversation?.let {
                if (it.conversation.id == message.conversationId) {
                    it.copy(
                        lastMessage = fallbackLastMessage,
                        lastSentAt = fallbackLastSentAt,
                        lastMessageId = lastMessage?.id?.takeIf { id -> id > 0 },
                        lastMessageSenderId = lastMessage?.senderId,
                        lastActivitySortKey = chatActivitySortKey(fallbackLastSentAt),
                    )
                } else {
                    it
                }
            },
            contacts = finalizeContacts(
                currentState.contacts.map {
                    if (it.conversation?.conversation?.id == message.conversationId) {
                        it.copy(
                            conversation = it.conversation.copy(
                                lastMessage = fallbackLastMessage,
                                lastSentAt = fallbackLastSentAt,
                                lastMessageId = lastMessage?.id?.takeIf { id -> id > 0 },
                                lastMessageSenderId = lastMessage?.senderId,
                                lastActivitySortKey = chatActivitySortKey(fallbackLastSentAt),
                            )
                        )
                    } else {
                        it
                    }
                }
            ),
        )
    }

    private fun mergeWithOptimisticMessages(
        serverMessages: List<ChatMessage>,
        currentMessages: List<ChatMessage>,
        conversationId: Int,
    ): List<ChatMessage> {
        val pendingMessages = currentMessages.filter {
            it.conversationId == conversationId && it.id < 0
        }

        return (serverMessages + pendingMessages).distinctBy { it.id }
    }

    private fun Conversation.toConversationItem(
        usersById: Map<Int, User>,
        participants: List<Int>,
        messages: List<ChatMessage>,
    ): ChatConversationItem {
        val lastMessage = messages.lastOrNull()
        val otherParticipants = participants
            .mapNotNull { usersById[it] }
            .filter { it.id != currentEmployeeId }
        val title = when (type) {
            ConversationType.DIRECT -> otherParticipants.firstOrNull()?.name ?: "Chat direct"
            ConversationType.GROUP -> name ?: "Chat de grup"
        }

        return ChatConversationItem(
            conversation = this,
            title = title,
            subtitle = otherParticipants.joinToString { it.name },
            initials = title.initials(),
            lastMessage = lastMessage?.messageText ?: "Nu exista mesaje inca.",
            lastSentAt = lastMessage?.sentAt ?: "",
            lastMessageId = lastMessage?.id?.takeIf { it > 0 },
            lastMessageSenderId = lastMessage?.senderId,
            lastActivitySortKey = chatActivitySortKey(lastMessage?.sentAt ?: ""),
        )
    }

    private fun buildContacts(
        users: List<User>,
        directConversationByUserId: Map<Int, ChatConversationItem>,
    ): List<ChatContactItem> {
        return finalizeContacts(
            users
                .filter { it.id != currentEmployeeId && it.isActive }
                .map { user ->
                    ChatContactItem(
                        user = user,
                        conversation = directConversationByUserId[user.id],
                    )
                }
        )
    }

    private fun finalizeContacts(contacts: List<ChatContactItem>): List<ChatContactItem> {
        return contacts
            .map { contact ->
                val conversation = contact.conversation ?: return@map contact
                contact.copy(hasUnread = isConversationUnread(conversation))
            }
            .sortedWith(
                compareByDescending<ChatContactItem> { it.conversation?.lastActivitySortKey ?: 0L }
                    .thenBy { it.title.lowercase() }
            )
    }

    private fun sortConversations(conversations: List<ChatConversationItem>): List<ChatConversationItem> {
        return conversations.sortedByDescending { it.lastActivitySortKey }
    }

    private fun isConversationUnread(conversation: ChatConversationItem): Boolean {
        val messageId = conversation.lastMessageId ?: return false
        if (conversation.lastMessageSenderId == currentEmployeeId) return false
        val lastReadId = readStateByConversationId[conversation.conversation.id] ?: 0
        return messageId > lastReadId
    }

    private fun markConversationAsRead(conversationId: Int, messages: List<ChatMessage>) {
        val lastReadId = messages.map { it.id }.filter { it > 0 }.maxOrNull() ?: return
        val currentReadId = readStateByConversationId[conversationId] ?: 0
        if (lastReadId <= currentReadId) return

        readStateByConversationId[conversationId] = lastReadId
        ChatReadStateStorage.save(currentEmployeeId, readStateByConversationId)
    }

    private fun String.initials(): String {
        return split(" ")
            .filter { it.isNotBlank() }
            .take(2)
            .joinToString("") { it.first().uppercase() }
            .ifBlank { "?" }
    }

    private companion object {
        const val CHAT_REFRESH_INTERVAL_MS = 1_000L
    }

}

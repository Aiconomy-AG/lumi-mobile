package org.example.project.presentation.chat

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
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
import org.example.project.domain.chat.ChatMessageReaction
import org.example.project.domain.chat.ChatNotificationEvent
import org.example.project.domain.chat.ChatParticipant
import org.example.project.domain.chat.ChatRealtimeApi
import org.example.project.domain.chat.ChatRealtimeEvent
import org.example.project.domain.chat.Conversation
import org.example.project.domain.chat.ConversationSummary
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
    val isGroup: Boolean = false,
    val participants: List<ChatParticipant> = emptyList(),
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

data class CreateGroupDialogState(
    val name: String = "",
    val memberSearch: String = "",
    val selectedMemberIds: Set<Int> = emptySet(),
    val isSaving: Boolean = false,
    val error: String? = null,
)

data class GroupSettingsState(
    val conversationId: Int,
    val name: String,
    val members: List<ChatParticipant>,
    val membersToRemove: Set<Int> = emptySet(),
    val memberSearch: String = "",
    val selectedToAdd: Set<Int> = emptySet(),
    val isSaving: Boolean = false,
    val error: String? = null,
)

data class ChatUiState(
    val isLoading: Boolean = false,
    val conversations: List<ChatConversationItem> = emptyList(),
    val contacts: List<ChatContactItem> = emptyList(),
    val selectedConversation: ChatConversationItem? = null,
    val messages: List<ChatMessage> = emptyList(),
    val usersById: Map<Int, User> = emptyMap(),
    val allUsers: List<User> = emptyList(),
    val searchQuery: String = "",
    val messageDraft: String = "",
    val error: String? = null,
    val showCreateGroupDialog: Boolean = false,
    val createGroupDialog: CreateGroupDialogState = CreateGroupDialogState(),
    val groupSettings: GroupSettingsState? = null,
    val showNewMessagePicker: Boolean = false,
) {
    val filteredConversations: List<ChatConversationItem>
        get() = if (searchQuery.isBlank()) {
            conversations
        } else {
            conversations.filter {
                it.title.contains(searchQuery, ignoreCase = true) ||
                        it.subtitle.contains(searchQuery, ignoreCase = true) ||
                        it.lastMessage.contains(searchQuery, ignoreCase = true)
            }
        }

    val sortedConversations: List<ChatConversationItem>
        get() = filteredConversations.sortedByDescending { it.lastActivitySortKey }

    val usersForNewMessage: List<User>
        get() {
            if (searchQuery.isBlank()) return emptyList()
            val directUserIds = conversations
                .filter { !it.isGroup }
                .flatMap { it.participants.map { p -> p.id } }
                .toSet()
            return allUsers.filter { user ->
                user.id != 0 &&
                        !directUserIds.contains(user.id) &&
                        (user.name.contains(searchQuery, ignoreCase = true) ||
                                user.email.contains(searchQuery, ignoreCase = true))
            }
        }

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
    private var conversationRealtimeJob: Job? = null

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

    fun showCreateGroupDialog() {
        _uiState.value = _uiState.value.copy(
            showCreateGroupDialog = true,
            createGroupDialog = CreateGroupDialogState(),
            error = null,
        )
    }

    fun dismissCreateGroupDialog() {
        _uiState.value = _uiState.value.copy(
            showCreateGroupDialog = false,
            createGroupDialog = CreateGroupDialogState(),
        )
    }

    fun onCreateGroupNameChanged(name: String) {
        _uiState.value = _uiState.value.copy(
            createGroupDialog = _uiState.value.createGroupDialog.copy(name = name, error = null),
        )
    }

    fun onCreateGroupMemberSearchChanged(query: String) {
        _uiState.value = _uiState.value.copy(
            createGroupDialog = _uiState.value.createGroupDialog.copy(memberSearch = query, error = null),
        )
    }

    fun toggleCreateGroupMember(userId: Int) {
        val dialog = _uiState.value.createGroupDialog
        val next = if (dialog.selectedMemberIds.contains(userId)) {
            dialog.selectedMemberIds - userId
        } else {
            dialog.selectedMemberIds + userId
        }
        _uiState.value = _uiState.value.copy(
            createGroupDialog = dialog.copy(selectedMemberIds = next, error = null),
        )
    }

    fun createGroup() {
        val dialog = _uiState.value.createGroupDialog
        val name = dialog.name.trim()
        if (name.isBlank()) {
            _uiState.value = _uiState.value.copy(
                createGroupDialog = dialog.copy(error = "Introdu numele grupului."),
            )
            return
        }
        if (dialog.selectedMemberIds.isEmpty()) {
            _uiState.value = _uiState.value.copy(
                createGroupDialog = dialog.copy(error = "Selectează cel puțin un membru."),
            )
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                createGroupDialog = dialog.copy(isSaving = true, error = null),
            )
            try {
                val conversation = chatApi.createGroupConversation(
                    name = name,
                    participantEmployeeIds = dialog.selectedMemberIds.toList(),
                )
                val detail = chatApi.getConversation(conversation.id)
                val item = detail.toConversationItem(
                    usersById = _uiState.value.usersById,
                    currentEmployeeId = currentEmployeeId,
                )
                _uiState.value = _uiState.value.copy(
                    showCreateGroupDialog = false,
                    createGroupDialog = CreateGroupDialogState(),
                    conversations = sortConversations(_uiState.value.conversations + item),
                    selectedConversation = item,
                    messages = emptyList(),
                    messageDraft = "",
                    error = null,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    createGroupDialog = _uiState.value.createGroupDialog.copy(
                        isSaving = false,
                        error = e.message ?: "Nu am putut crea grupul.",
                    ),
                )
            }
        }
    }

    fun openGroupSettings() {
        val selected = _uiState.value.selectedConversation ?: return
        if (!selected.isGroup) return

        viewModelScope.launch {
            try {
                val detail = chatApi.getConversation(selected.conversation.id)
                _uiState.value = _uiState.value.copy(
                    groupSettings = GroupSettingsState(
                        conversationId = detail.conversation.id,
                        name = detail.conversation.name.orEmpty(),
                        members = detail.participants,
                    ),
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Nu am putut încărca setările grupului.",
                )
            }
        }
    }

    fun dismissGroupSettings() {
        _uiState.value = _uiState.value.copy(groupSettings = null)
    }

    fun onGroupSettingsNameChanged(name: String) {
        val settings = _uiState.value.groupSettings ?: return
        _uiState.value = _uiState.value.copy(
            groupSettings = settings.copy(name = name, error = null),
        )
    }

    fun onGroupSettingsMemberSearchChanged(query: String) {
        val settings = _uiState.value.groupSettings ?: return
        _uiState.value = _uiState.value.copy(
            groupSettings = settings.copy(memberSearch = query, error = null),
        )
    }

    fun toggleGroupSettingsAddMember(userId: Int) {
        val settings = _uiState.value.groupSettings ?: return
        val next = if (settings.selectedToAdd.contains(userId)) {
            settings.selectedToAdd - userId
        } else {
            settings.selectedToAdd + userId
        }
        _uiState.value = _uiState.value.copy(
            groupSettings = settings.copy(selectedToAdd = next, error = null),
        )
    }

    fun toggleGroupSettingsRemoveMember(userId: Int) {
        val settings = _uiState.value.groupSettings ?: return
        if (userId == currentEmployeeId) return
        val next = if (settings.membersToRemove.contains(userId)) {
            settings.membersToRemove - userId
        } else {
            settings.membersToRemove + userId
        }
        _uiState.value = _uiState.value.copy(
            groupSettings = settings.copy(membersToRemove = next, error = null),
        )
    }

    fun saveGroupSettings() {
        val settings = _uiState.value.groupSettings ?: return
        val trimmedName = settings.name.trim()
        val selected = _uiState.value.selectedConversation ?: return

        if (trimmedName.isBlank()) {
            _uiState.value = _uiState.value.copy(
                groupSettings = settings.copy(error = "Introdu numele grupului."),
            )
            return
        }

        val nameChanged = trimmedName != selected.conversation.name.orEmpty()
        val hasChanges = nameChanged ||
                settings.selectedToAdd.isNotEmpty() ||
                settings.membersToRemove.isNotEmpty()

        if (!hasChanges) {
            dismissGroupSettings()
            return
        }

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                groupSettings = settings.copy(isSaving = true, error = null),
            )
            try {
                val updated = chatApi.updateGroupConversation(
                    conversationId = settings.conversationId,
                    name = if (nameChanged) trimmedName else null,
                    addParticipantEmployeeIds = settings.selectedToAdd.toList(),
                    removeParticipantEmployeeIds = settings.membersToRemove.toList(),
                )
                val detail = chatApi.getConversation(updated.id)
                val item = detail.toConversationItem(
                    usersById = _uiState.value.usersById,
                    currentEmployeeId = currentEmployeeId,
                )
                val currentState = _uiState.value
                _uiState.value = currentState.copy(
                    groupSettings = null,
                    conversations = sortConversations(
                        currentState.conversations.map {
                            if (it.conversation.id == item.conversation.id) item else it
                        }
                    ),
                    selectedConversation = currentState.selectedConversation?.let {
                        if (it.conversation.id == item.conversation.id) item else it
                    },
                    error = null,
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    groupSettings = _uiState.value.groupSettings?.copy(
                        isSaving = false,
                        error = e.message ?: "Nu am putut salva modificările.",
                    ),
                )
            }
        }
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
                val detail = chatApi.getConversation(conversation.id)
                val conversationItem = detail.toConversationItem(
                    usersById = _uiState.value.usersById,
                    currentEmployeeId = currentEmployeeId,
                )

                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    conversations = sortConversations(_uiState.value.conversations + conversationItem),
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
                    searchQuery = "",
                )
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    error = e.message ?: "Nu am putut porni conversatia.",
                )
            }
        }
    }

    fun startDirectMessage(user: User) {
        selectContact(
            ChatContactItem(
                user = user,
                conversation = null,
            )
        )
    }

    fun startDirectMessageByUserId(userId: Int) {
        viewModelScope.launch {
            var contact = _uiState.value.contacts.firstOrNull { it.user.id == userId }
            if (contact == null) {
                refreshChat(showLoading = false, showError = false)
                contact = _uiState.value.contacts.firstOrNull { it.user.id == userId }
            }
            contact?.let { selectContact(it) }
        }
    }

    fun selectConversation(conversation: ChatConversationItem) {
        listenForConversationEvents(conversation.conversation.id)

        viewModelScope.launch {
            _uiState.value = _uiState.value.copy(
                selectedConversation = conversation,
                isLoading = true,
                error = null,
            )

            try {
                val messages = chatApi.getMessages(conversation.conversation.id)
                markConversationAsRead(conversation.conversation.id, messages)
                val lastMessage = messages.lastOrNull()
                val updatedConversation = if (lastMessage != null) {
                    conversation.copy(
                        lastMessageId = lastMessage.id.takeIf { it > 0 },
                        lastMessageSenderId = lastMessage.senderId,
                        lastMessage = formatLastMessagePreview(
                            conversation = conversation.conversation,
                            messageText = lastMessage.messageText,
                            senderId = lastMessage.senderId,
                            participants = conversation.participants,
                            usersById = _uiState.value.usersById,
                        ),
                        lastSentAt = lastMessage.sentAt,
                        lastActivitySortKey = chatActivitySortKey(lastMessage.sentAt),
                    )
                } else {
                    conversation
                }
                _uiState.value = _uiState.value.copy(
                    isLoading = false,
                    messages = messages,
                    conversations = sortConversations(
                        _uiState.value.conversations.map {
                            if (it.conversation.id == updatedConversation.conversation.id) {
                                updatedConversation
                            } else {
                                it
                            }
                        }
                    ),
                    selectedConversation = updatedConversation,
                    contacts = finalizeContacts(_uiState.value.contacts),
                    searchQuery = "",
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
        stopListeningForConversationEvents()
        _uiState.value = _uiState.value.copy(
            selectedConversation = null,
            messages = emptyList(),
            messageDraft = "",
            groupSettings = null,
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
        val optimisticPreview = formatLastMessagePreview(
            conversation = selectedConversation.conversation,
            messageText = optimisticMessage.messageText,
            senderId = optimisticMessage.senderId,
            participants = selectedConversation.participants,
            usersById = _uiState.value.usersById,
        )
        val optimisticConversation = selectedConversation.copy(
            lastMessage = optimisticPreview,
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

    fun reactToMessage(message: ChatMessage, emoji: String) {
        if (message.id < 0) return
        val previousReactions = message.reactions
        applyMessageReactions(message.id, patchReaction(previousReactions, emoji, add = true))

        viewModelScope.launch {
            try {
                val updated = chatApi.addReaction(message.conversationId, message.id, emoji)
                applyMessageReactions(message.id, updated.reactions)
            } catch (e: Exception) {
                applyMessageReactions(message.id, previousReactions)
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Nu am putut adăuga reacția.",
                )
            }
        }
    }

    fun removeReaction(message: ChatMessage, emoji: String) {
        if (message.id < 0) return
        val previousReactions = message.reactions
        applyMessageReactions(message.id, patchReaction(previousReactions, emoji, add = false))

        viewModelScope.launch {
            try {
                val updated = chatApi.removeReaction(message.conversationId, message.id, emoji)
                applyMessageReactions(message.id, updated.reactions)
            } catch (e: Exception) {
                applyMessageReactions(message.id, previousReactions)
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Nu am putut elimina reacția.",
                )
            }
        }
    }

    private fun patchReaction(
        reactions: List<ChatMessageReaction>,
        emoji: String,
        add: Boolean,
    ): List<ChatMessageReaction> {
        val withoutSelf = reactions.mapNotNull { reaction ->
            if (currentEmployeeId !in reaction.userIds) return@mapNotNull reaction
            val remainingUserIds = reaction.userIds - currentEmployeeId
            if (remainingUserIds.isEmpty()) null else reaction.copy(count = remainingUserIds.size, userIds = remainingUserIds)
        }

        if (!add) return withoutSelf

        val existing = withoutSelf.find { it.emoji == emoji }
        return if (existing != null) {
            withoutSelf.map {
                if (it.emoji == emoji) {
                    it.copy(count = it.count + 1, userIds = it.userIds + currentEmployeeId)
                } else {
                    it
                }
            }
        } else {
            withoutSelf + ChatMessageReaction(emoji = emoji, count = 1, userIds = listOf(currentEmployeeId))
        }
    }

    private fun applyMessageReactions(messageId: Int, reactions: List<ChatMessageReaction>) {
        _uiState.value = _uiState.value.copy(
            messages = _uiState.value.messages.map {
                if (it.id == messageId) it.copy(reactions = reactions) else it
            },
        )
    }

    private fun listenForConversationEvents(conversationId: Int) {
        conversationRealtimeJob?.cancel()
        val realtimeApi = chatRealtimeApi ?: return

        conversationRealtimeJob = viewModelScope.launch {
            realtimeApi.conversationEvents(conversationId)
                .catch { }
                .collect { event ->
                    when (event) {
                        is ChatRealtimeEvent.ReactionUpdated -> applyRealtimeMessage(event.message)
                    }
                }
        }
    }

    private fun stopListeningForConversationEvents() {
        conversationRealtimeJob?.cancel()
        conversationRealtimeJob = null
    }

    private fun applyRealtimeMessage(message: ChatMessage) {
        val currentState = _uiState.value
        if (currentState.selectedConversation?.conversation?.id != message.conversationId) return
        _uiState.value = currentState.copy(
            messages = currentState.messages.map { if (it.id == message.id) message else it },
        )
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
            val summaries = chatApi.getConversations(currentEmployeeId)

            val items = summaries.map { summary ->
                summary.toConversationItem(
                    usersById = usersById,
                    currentEmployeeId = currentEmployeeId,
                )
            }
            val directConversationByUserId = summaries
                .zip(items)
                .mapNotNull { (summary, item) ->
                    if (summary.conversation.type != ConversationType.DIRECT) return@mapNotNull null
                    val otherUserId = summary.participants
                        .firstOrNull { it.id != currentEmployeeId }
                        ?.id
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

            val enrichedSelectedConversation = if (
                selectedConversation != null &&
                mergedSelectedMessages != null
            ) {
                val lastMessage = mergedSelectedMessages.lastOrNull()
                if (lastMessage != null) {
                    selectedConversation.copy(
                        lastMessageId = lastMessage.id.takeIf { it > 0 },
                        lastMessageSenderId = lastMessage.senderId,
                        lastMessage = formatLastMessagePreview(
                            conversation = selectedConversation.conversation,
                            messageText = lastMessage.messageText,
                            senderId = lastMessage.senderId,
                            participants = selectedConversation.participants,
                            usersById = usersById,
                        ),
                        lastSentAt = lastMessage.sentAt,
                        lastActivitySortKey = chatActivitySortKey(lastMessage.sentAt),
                    )
                } else {
                    selectedConversation
                }
            } else {
                selectedConversation ?: _uiState.value.selectedConversation
            }

            _uiState.value = _uiState.value.copy(
                isLoading = false,
                conversations = sortConversations(items),
                contacts = contacts,
                allUsers = users.filter { it.id != currentEmployeeId && it.isActive },
                selectedConversation = enrichedSelectedConversation,
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
                refreshConversationInternal(conversationId)
            }

            "chat_added_to_conversation" -> loadChat()
        }
    }

    fun refreshConversation(conversationId: Int) {
        viewModelScope.launch {
            refreshConversationInternal(conversationId)
        }
    }

    private suspend fun refreshConversationInternal(conversationId: Int) {
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
            lastMessage = lastMessage?.let {
                formatLastMessagePreview(
                    conversation = conversation.conversation,
                    messageText = it.messageText,
                    senderId = it.senderId,
                    participants = conversation.participants,
                    usersById = currentState.usersById,
                )
            } ?: conversation.lastMessage,
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
        val selected = currentState.selectedConversation
            ?.takeIf { it.conversation.id == savedMessage.conversationId }
        val updatedConversation = selected?.copy(
            lastMessage = formatLastMessagePreview(
                conversation = selected.conversation,
                messageText = lastMessage.messageText,
                senderId = lastMessage.senderId,
                participants = selected.participants,
                usersById = currentState.usersById,
            ),
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
                            lastMessage = updatedConversation?.lastMessage ?: it.lastMessage,
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
                                lastMessage = updatedConversation?.lastMessage ?: it.conversation.lastMessage,
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
        val fallbackLastMessage = lastMessage?.let {
            val selected = currentState.selectedConversation
                ?.takeIf { it.conversation.id == message.conversationId }
            if (selected != null) {
                formatLastMessagePreview(
                    conversation = selected.conversation,
                    messageText = it.messageText,
                    senderId = it.senderId,
                    participants = selected.participants,
                    usersById = currentState.usersById,
                )
            } else {
                it.messageText
            }
        } ?: "Nu exista mesaje inca."
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

    private fun ConversationSummary.toConversationItem(
        usersById: Map<Int, User>,
        currentEmployeeId: Int,
    ): ChatConversationItem = conversation.toConversationItem(
        usersById = usersById,
        currentEmployeeId = currentEmployeeId,
        participants = participants,
        lastMessageText = lastMessage?.message,
        lastMessageSenderId = lastMessage?.senderId,
        lastSentAt = lastMessage?.sentAt ?: lastMessageAt.orEmpty(),
    )

    private fun org.example.project.domain.chat.ConversationDetail.toConversationItem(
        usersById: Map<Int, User>,
        currentEmployeeId: Int,
    ): ChatConversationItem = conversation.toConversationItem(
        usersById = usersById,
        currentEmployeeId = currentEmployeeId,
        participants = participants,
        lastMessageText = null,
        lastMessageSenderId = null,
        lastSentAt = "",
    )

    private fun Conversation.toConversationItem(
        usersById: Map<Int, User>,
        currentEmployeeId: Int,
        participants: List<ChatParticipant>,
        lastMessageText: String?,
        lastMessageSenderId: Int?,
        lastSentAt: String,
    ): ChatConversationItem {
        val otherParticipants = participants.filter { it.id != currentEmployeeId }
        val title = when (type) {
            ConversationType.DIRECT -> otherParticipants.firstOrNull()?.name ?: "Chat direct"
            ConversationType.GROUP -> name ?: "Chat de grup"
        }
        val preview = if (lastMessageText.isNullOrBlank()) {
            "Nu exista mesaje inca."
        } else {
            formatLastMessagePreview(
                conversation = this,
                messageText = lastMessageText,
                senderId = lastMessageSenderId,
                participants = participants,
                usersById = usersById,
            )
        }

        return ChatConversationItem(
            conversation = this,
            title = title,
            subtitle = otherParticipants.joinToString { it.name },
            initials = title.initials(),
            lastMessage = preview,
            lastSentAt = lastSentAt,
            lastMessageSenderId = lastMessageSenderId,
            lastActivitySortKey = chatActivitySortKey(lastSentAt),
            isGroup = type == ConversationType.GROUP,
            participants = participants,
        )
    }

    fun approveAiAction(conversationId: Int, actionId: Int) {
        viewModelScope.launch {
            try {
                chatApi.approveAiAction(conversationId, actionId)
                refreshConversationInternal(conversationId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to approve AI action."
                )
            }
        }
    }

    fun rejectAiAction(conversationId: Int, actionId: Int) {
        viewModelScope.launch {
            try {
                chatApi.rejectAiAction(conversationId, actionId)
                refreshConversationInternal(conversationId)
            } catch (e: Exception) {
                _uiState.value = _uiState.value.copy(
                    error = e.message ?: "Failed to reject AI action."
                )
            }
        }
    }

    private fun formatLastMessagePreview(
        conversation: Conversation,
        messageText: String,
        senderId: Int?,
        participants: List<ChatParticipant>,
        usersById: Map<Int, User>,
    ): String {
        if (conversation.type != ConversationType.GROUP) return messageText
        val senderName = senderId?.let { id ->
            participants.find { it.id == id }?.name ?: usersById[id]?.name
        } ?: "?"
        return "$senderName: $messageText"
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

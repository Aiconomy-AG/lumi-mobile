package org.example.project.presentation.chat

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import org.example.project.data.accounts.User
import org.example.project.data.chat.chatClockTimeLabel
import org.example.project.domain.chat.ChatMessage
import org.example.project.domain.chat.ChatMessageReaction
import org.example.project.domain.chat.ChatMessageType
import org.example.project.domain.chat.ConversationType
import org.example.project.domain.chat.ChatParticipant
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Call
import androidx.compose.material.icons.filled.Group
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.Icon
import coil3.compose.AsyncImage
import org.example.project.presentation.components.AppBackButton
import org.example.project.presentation.components.AppSearchField
import org.example.project.presentation.components.DismissKeyboardOnTapOutside
import org.example.project.presentation.localization.LocalAppStrings
import org.example.project.presentation.theme.AppColorPalette
import org.example.project.presentation.theme.AppDimensions
import org.example.project.domain.chat.AiActionMeta
import org.example.project.presentation.components.AppButton
import org.example.project.presentation.components.AppOutlinedButton
import org.example.project.presentation.components.AppDetailGrid
import org.example.project.presentation.components.AppDetailField
import org.example.project.presentation.components.PickedPhoto
import org.example.project.presentation.components.rememberPhotoPicker

@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    currentEmployeeId: Int,
    onStartCall: (participantIds: List<Int>, type: String, conversationId: Int, isGroupConversation: Boolean) -> Unit,
    modifier: Modifier = Modifier,
) {
    val uiState by viewModel.uiState.collectAsState()

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(AppColorPalette.Background),
    ) {
        if (uiState.selectedConversation == null) {
            DismissKeyboardOnTapOutside(modifier = Modifier.fillMaxSize()) {
                ConversationListScreen(
                    uiState = uiState,
                    currentEmployeeId = currentEmployeeId,
                    onSearchQueryChanged = viewModel::onSearchQueryChanged,
                    onConversationClick = viewModel::selectConversation,
                    onNewGroupClick = viewModel::showCreateGroupDialog,
                    onStartDirectMessage = viewModel::startDirectMessage,
                )
            }
        } else {
            ConversationDetailScreen(
                uiState = uiState,
                currentEmployeeId = currentEmployeeId,
                onBackClick = viewModel::backToConversationList,
                onMessageDraftChanged = viewModel::onMessageDraftChanged,
                onSendClick = viewModel::sendMessage,
                onPhotoPicked = viewModel::sendPhoto,
                onGroupSettingsClick = viewModel::openGroupSettings,
                onStartCall = { type ->
                    val conversation = uiState.selectedConversation!!
                    onStartCall(
                        conversation.participants.filter { !it.isBot }.map { it.id },
                        type,
                        conversation.conversation.id,
                        conversation.conversation.type == ConversationType.GROUP,
                    )
                },
                onApproveAiAction = viewModel::approveAiAction,
                onRejectAiAction = viewModel::rejectAiAction,
                onReact = viewModel::reactToMessage,
                onRemoveReaction = viewModel::removeReaction,
            )
        }

        if (uiState.showCreateGroupDialog) {
            CreateGroupDialog(
                uiState = uiState,
                currentEmployeeId = currentEmployeeId,
                onDismiss = viewModel::dismissCreateGroupDialog,
                onNameChanged = viewModel::onCreateGroupNameChanged,
                onMemberSearchChanged = viewModel::onCreateGroupMemberSearchChanged,
                onToggleMember = viewModel::toggleCreateGroupMember,
                onCreate = viewModel::createGroup,
            )
        }

        uiState.groupSettings?.let { settings ->
            EditGroupDialog(
                settings = settings,
                currentEmployeeId = currentEmployeeId,
                allUsers = uiState.allUsers,
                onDismiss = viewModel::dismissGroupSettings,
                onNameChanged = viewModel::onGroupSettingsNameChanged,
                onMemberSearchChanged = viewModel::onGroupSettingsMemberSearchChanged,
                onToggleAddMember = viewModel::toggleGroupSettingsAddMember,
                onToggleRemoveMember = viewModel::toggleGroupSettingsRemoveMember,
                onSave = viewModel::saveGroupSettings,
            )
        }
    }
}

@Composable
private fun AiActionCard(
    conversationId: Int,
    meta: AiActionMeta,
    currentUserId: Int,
    onApprove: (Int, Int) -> Unit,
    onReject: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    val strings = LocalAppStrings.current

    val isExpired = meta.status == "expired"
    val effectiveStatus = if (isExpired && meta.status == "pending") "expired" else meta.status
    val isRequester = currentUserId == meta.requestedByUserId
    val canRespond = isRequester && effectiveStatus == "pending" && !isExpired

    Column(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(AppDimensions.TableCornerRadius))
            .background(AppColorPalette.Surface)
            .border(1.dp, AppColorPalette.Border, RoundedCornerShape(AppDimensions.TableCornerRadius))
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        // --- 1. Header & Status ---
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.Top
        ) {
            Text(
                text = meta.summary,
                color = AppColorPalette.TextPrimary,
                style = MaterialTheme.typography.titleMedium,
                fontWeight = FontWeight.SemiBold,
                modifier = Modifier.weight(1f).padding(end = 8.dp)
            )

            // Mimics AppStatusBadge but manually maps to your AppColorPalette
            val (statusTextColor, statusBgColor) = when (effectiveStatus) {
                "pending" -> AppColorPalette.Warning to AppColorPalette.Warning.copy(alpha = 0.15f)
                "approved", "executed" -> AppColorPalette.Success to AppColorPalette.Success.copy(alpha = 0.15f)
                "failed", "rejected" -> AppColorPalette.Error to AppColorPalette.Error.copy(alpha = 0.15f)
                else -> AppColorPalette.TextSecondary to AppColorPalette.SurfaceVariant
            }

            Box(
                modifier = Modifier
                    .background(statusBgColor, MaterialTheme.shapes.extraSmall)
                    .padding(horizontal = 10.dp, vertical = 4.dp)
            ) {
                Text(
                    text = strings.text(effectiveStatus.uppercase()),
                    color = statusTextColor,
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold
                )
            }
        }

        // --- 2. Arguments Grid ---
        val detailFields = meta.arguments?.map { (key, value) ->
            AppDetailField(
                label = key.replace("_", " ").replaceFirstChar { it.uppercase() },
                value = value.toString().removeSurrounding("\"")
            )
        } ?: emptyList()

        if (detailFields.isNotEmpty()) {
            AppDetailGrid(fields = detailFields)
        }

        // --- 3. Error Message ---
        meta.error?.let { errorMsg ->
            Text(
                text = errorMsg,
                color = AppColorPalette.Error,
                style = MaterialTheme.typography.bodyMedium
            )
        }

        // --- 4. Call to Action ---
        if (canRespond) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                AppButton(
                    onClick = { onApprove(conversationId, meta.actionId) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(strings.text("Approve"))
                }

                AppOutlinedButton(
                    onClick = { onReject(conversationId, meta.actionId) },
                    modifier = Modifier.weight(1f)
                ) {
                    Text(strings.text("Reject"), color = AppColorPalette.TextPrimary)
                }
            }
        } else if (effectiveStatus == "pending" && !isRequester) {
            Text(
                text = strings.format("Waiting for {name} to confirm", "name" to (meta.requestedByName ?: "someone")),
                color = AppColorPalette.TextSecondary,
                style = MaterialTheme.typography.labelMedium
            )
        }
    }
}

@Composable
private fun ConversationListScreen(
    uiState: ChatUiState,
    currentEmployeeId: Int,
    onSearchQueryChanged: (String) -> Unit,
    onConversationClick: (ChatConversationItem) -> Unit,
    onNewGroupClick: () -> Unit,
    onStartDirectMessage: (User) -> Unit,
) {
    val strings = LocalAppStrings.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(AppDimensions.ScreenPadding),
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppSearchField(
                value = uiState.searchQuery,
                onValueChange = onSearchQueryChanged,
                placeholder = strings.text("Search chat or person..."),
                modifier = Modifier.weight(1f),
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onNewGroupClick,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColorPalette.Primary,
                    contentColor = AppColorPalette.OnPrimary,
                ),
                modifier = Modifier.height(44.dp),
            ) {
                Text(strings.text("New group"), fontSize = 13.sp)
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading && uiState.conversations.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColorPalette.Primary)
                }
            }

            uiState.error != null && uiState.conversations.isEmpty() -> {
                Text(
                    text = uiState.error,
                    color = AppColorPalette.Error,
                    modifier = Modifier.padding(16.dp),
                )
            }

            uiState.sortedConversations.isEmpty() && uiState.usersForNewMessage.isEmpty() -> {
                Text(
                    text = strings.text("No chats found."),
                    color = AppColorPalette.TextSecondary,
                    modifier = Modifier.padding(16.dp),
                )
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(uiState.sortedConversations, key = { it.conversation.id }) { conversation ->
                        ConversationRow(
                            conversation = conversation,
                            currentEmployeeId = currentEmployeeId,
                            onClick = { onConversationClick(conversation) },
                        )
                    }

                    items(uiState.usersForNewMessage, key = { "new-${it.id}" }) { user ->
                        NewMessageRow(
                            user = user,
                            onClick = { onStartDirectMessage(user) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ConversationRow(
    conversation: ChatConversationItem,
    currentEmployeeId: Int,
    onClick: () -> Unit,
) {
    val hasUnread = conversation.lastMessageId != null &&
            conversation.lastMessageSenderId != currentEmployeeId

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AppColorPalette.Surface)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ConversationAvatar(
            conversation = conversation,
            modifier = Modifier.size(44.dp),
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = conversation.title,
                    color = AppColorPalette.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f, fill = false),
                )

                if (conversation.isGroup) {
                    Spacer(modifier = Modifier.width(6.dp))
                    GroupBadge()
                }

                Spacer(modifier = Modifier.width(8.dp))

                Text(
                    text = chatClockTimeLabel(conversation.lastSentAt),
                    color = AppColorPalette.TextSecondary,
                    style = MaterialTheme.typography.labelSmall,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = conversation.lastMessage,
                color = if (hasUnread) AppColorPalette.TextPrimary else AppColorPalette.TextSecondary,
                fontWeight = if (hasUnread) FontWeight.Medium else FontWeight.Normal,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            if (conversation.isGroup && conversation.subtitle.isNotBlank()) {
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                    text = conversation.subtitle,
                    color = AppColorPalette.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelSmall,
                )
            }
        }
    }
}

@Composable
private fun NewMessageRow(
    user: User,
    onClick: () -> Unit,
) {
    val strings = LocalAppStrings.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AppColorPalette.SurfaceVariant)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        ParticipantAvatar(
            participant = ChatParticipant(
                id = user.id,
                name = user.name,
                email = user.email,
                role = user.role.displayName,
                status = user.status,
            ),
            modifier = Modifier.size(40.dp),
        )

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = strings.format("Start chat with {name}", "name" to user.name),
                color = AppColorPalette.TextPrimary,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = user.email,
                color = AppColorPalette.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelSmall,
            )
        }
    }
}



@Composable
private fun GroupBadge() {
    val strings = LocalAppStrings.current

    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(50))
            .background(AppColorPalette.Primary.copy(alpha = 0.2f))
            .padding(horizontal = 6.dp, vertical = 2.dp),
    ) {
        Text(
            text = strings.text("GROUP"),
            color = AppColorPalette.Primary,
            style = MaterialTheme.typography.labelSmall,
            fontWeight = FontWeight.Bold,
            fontSize = 10.sp,
        )
    }
}

@Composable
private fun ConversationAvatar(
    conversation: ChatConversationItem,
    modifier: Modifier = Modifier,
) {
    if (conversation.isGroup) {
        GroupAvatar(
            participants = conversation.participants,
            modifier = modifier,
        )
    } else {
        val participant = conversation.participants.firstOrNull()
        if (participant != null) {
            ParticipantAvatar(participant = participant, modifier = modifier)
        } else {
            InitialsAvatar(initials = conversation.initials, userId = conversation.conversation.id, modifier = modifier)
        }
    }
}

@Composable
private fun GroupAvatar(
    participants: List<ChatParticipant>,
    modifier: Modifier = Modifier,
) {
    val shown = participants.take(2)

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(12.dp))
                .background(AppColorPalette.SurfaceVariant)
                .border(1.dp, AppColorPalette.Border, RoundedCornerShape(12.dp)),
        ) {
            if (shown.isEmpty()) {
                InitialsAvatar(
                    initials = "G",
                    userId = 0,
                    modifier = Modifier
                        .size(28.dp)
                        .align(Alignment.Center),
                )
            } else {
                shown.forEachIndexed { index, participant ->
                    Box(
                        modifier = Modifier
                            .align(if (index == 0) Alignment.TopStart else Alignment.BottomEnd)
                            .padding(4.dp)
                            .size(22.dp),
                    ) {
                        ParticipantAvatar(
                            participant = participant,
                            modifier = Modifier.fillMaxSize(),
                            showStatus = false,
                        )
                    }
                }
            }
        }

        Box(
            modifier = Modifier
                .align(Alignment.BottomEnd)
                .size(16.dp)
                .clip(CircleShape)
                .background(AppColorPalette.Primary),
            contentAlignment = Alignment.Center,
        ) {
            Text(
                text = "G",
                color = AppColorPalette.OnPrimary,
                fontSize = 8.sp,
                fontWeight = FontWeight.Bold,
            )
        }
    }
}

@Composable
fun ParticipantAvatar(
    participant: ChatParticipant,
    modifier: Modifier = Modifier,
    showStatus: Boolean = true,
) {
    val color = AppColorPalette.AvatarPalette[participant.id % AppColorPalette.AvatarPalette.size]
    val initials = participant.name
        .split(" ")
        .filter { it.isNotBlank() }
        .take(2)
        .joinToString("") { it.first().uppercase() }
        .ifBlank { "?" }

    Box(modifier = modifier) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .clip(CircleShape)
                .background(color),
            contentAlignment = Alignment.Center,
        ) {
            if (!participant.photoUrl.isNullOrBlank() && !participant.isBot) {
                AsyncImage(
                    model = participant.photoUrl,
                    contentDescription = null,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize(),
                )
            } else {
                Text(
                    text = if (participant.isBot) "AI" else initials,
                    color = AppColorPalette.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                )
            }
        }

        if (showStatus) {
            val statusColor = when (participant.status.lowercase()) {
                "online" -> AppColorPalette.Success
                "busy" -> AppColorPalette.Error
                else -> AppColorPalette.TextSecondary
            }
            Box(
                modifier = Modifier
                    .align(Alignment.BottomEnd)
                    .size(10.dp)
                    .clip(CircleShape)
                    .background(AppColorPalette.Surface)
                    .padding(1.dp),
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .clip(CircleShape)
                        .background(statusColor),
                )
            }
        }
    }
}

@Composable
private fun InitialsAvatar(
    initials: String,
    userId: Int,
    modifier: Modifier = Modifier,
) {
    val color = AppColorPalette.AvatarPalette[userId % AppColorPalette.AvatarPalette.size]

    Box(
        modifier = modifier
            .clip(CircleShape)
            .background(color),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = initials,
            color = AppColorPalette.TextPrimary,
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
        )
    }
}

@Composable
private fun ConversationDetailScreen(
    uiState: ChatUiState,
    currentEmployeeId: Int,
    onBackClick: () -> Unit,
    onMessageDraftChanged: (String) -> Unit,
    onSendClick: () -> Unit,
    onPhotoPicked: (PickedPhoto) -> Unit,
    onGroupSettingsClick: () -> Unit,
    onStartCall: (String) -> Unit,
    onApproveAiAction: (Int, Int) -> Unit,
    onRejectAiAction: (Int, Int) -> Unit,
    onReact: (ChatMessage, String) -> Unit,
    onRemoveReaction: (ChatMessage, String) -> Unit,
) {
    val selectedConversation = uiState.selectedConversation ?: return
    val listState = rememberLazyListState()
    val strings = LocalAppStrings.current
    val photoPicker = rememberPhotoPicker(
        onPhotoPicked = onPhotoPicked,
        onError = { },
    )

    LaunchedEffect(uiState.messages.size, selectedConversation.conversation.id) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier.fillMaxSize(),
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColorPalette.Surface)
                .padding(horizontal = 8.dp, vertical = 10.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppBackButton(onClick = onBackClick)

            Spacer(modifier = Modifier.width(4.dp))

            ConversationAvatar(
                conversation = selectedConversation,
                modifier = Modifier.size(36.dp),
            )

            Spacer(modifier = Modifier.width(8.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = selectedConversation.title,
                    color = AppColorPalette.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
                Text(
                    text = selectedConversation.subtitle,
                    color = AppColorPalette.TextSecondary,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    style = MaterialTheme.typography.labelMedium,
                )
            }

            if (selectedConversation.isGroup) {
                TextButton(onClick = onGroupSettingsClick) {
                    Text(
                        text = strings.text("Settings"),
                        color = AppColorPalette.Primary,
                        fontSize = 12.sp,
                    )
                }
            }
            IconButton(onClick = { onStartCall("audio") }) {
                Icon(
                    imageVector = Icons.Filled.Call,
                    contentDescription = strings.text("Audio call"),
                    tint = AppColorPalette.Primary,
                    modifier = Modifier.size(22.dp),
                )
            }
            IconButton(onClick = { onStartCall("video") }) {
                Icon(
                    imageVector = Icons.Filled.Videocam,
                    contentDescription = strings.text("Video call"),
                    tint = AppColorPalette.Primary,
                    modifier = Modifier.size(22.dp),
                )
            }
        }

        HorizontalDivider(color = AppColorPalette.Border)

        LazyColumn(
            state = listState,
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            items(uiState.messages, key = { it.id }) { message ->
                when (message.messageType) {
                    ChatMessageType.CALL -> CallLogBubble(
                        message = message,
                        strings = strings,
                    )
                    else -> MessageBubble(
                        message = message,
                        isMine = message.senderId == currentEmployeeId,
                        senderName = uiState.usersById[message.senderId]?.name
                            ?: selectedConversation.participants.find { it.id == message.senderId }?.name
                            ?: strings.text("Unknown sender"),
                        showSenderName = selectedConversation.isGroup,
                        currentUserId = currentEmployeeId,
                        onApproveAiAction = onApproveAiAction,
                        onRejectAiAction = onRejectAiAction,
                        onReact = onReact,
                        onRemoveReaction = onRemoveReaction,
                    )
                }
            }
        }

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(AppColorPalette.Surface)
                .navigationBarsPadding()
                .imePadding()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(
                onClick = { photoPicker.launch() },
                enabled = !uiState.isSendingPhoto,
            ) {
                if (uiState.isSendingPhoto) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        color = AppColorPalette.Primary,
                        strokeWidth = 2.dp,
                    )
                } else {
                    Icon(
                        imageVector = Icons.Filled.Image,
                        contentDescription = strings.text("Add photo"),
                        tint = AppColorPalette.Primary,
                        modifier = Modifier.size(22.dp),
                    )
                }
            }

            Spacer(modifier = Modifier.width(4.dp))

            BasicTextField(
                value = uiState.messageDraft,
                onValueChange = onMessageDraftChanged,
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = 44.dp, max = 112.dp)
                    .clip(RoundedCornerShape(18.dp))
                    .background(AppColorPalette.SurfaceVariant)
                    .border(1.dp, AppColorPalette.Border, RoundedCornerShape(18.dp))
                    .padding(horizontal = 14.dp, vertical = 10.dp),
                textStyle = MaterialTheme.typography.bodyLarge.copy(
                    color = AppColorPalette.TextPrimary,
                    fontFamily = FontFamily.SansSerif,
                ),
                cursorBrush = SolidColor(AppColorPalette.Primary),
                minLines = 1,
                maxLines = 4,
                decorationBox = { innerTextField ->
                    Box(contentAlignment = Alignment.CenterStart) {
                        if (uiState.messageDraft.isEmpty()) {
                            Text(
                                text = strings.text("Write a message..."),
                                color = AppColorPalette.TextSecondary,
                                style = MaterialTheme.typography.bodyLarge,
                            )
                        }
                        innerTextField()
                    }
                },
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onSendClick,
                enabled = uiState.messageDraft.isNotBlank() && !uiState.isSendingPhoto,
                shape = RoundedCornerShape(50),
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppColorPalette.Primary,
                    contentColor = AppColorPalette.OnPrimary,
                    disabledContainerColor = AppColorPalette.BorderStrong,
                    disabledContentColor = AppColorPalette.TextSecondary,
                ),
            ) {
                Text(strings.text("Send"))
            }
        }
    }
}

@Composable
private fun CallLogBubble(
    message: ChatMessage,
    strings: org.example.project.presentation.localization.AppStrings,
) {
    val call = message.call
    val isMissed = call?.status in setOf("missed", "declined", "cancelled", "failed")
    val isCompleted = !isMissed && (
        call?.status in setOf("ended", "completed") ||
            (call?.durationSeconds ?: 0) > 0
        )
    val icon = when {
        call?.mode == "group" -> Icons.Filled.Group
        call?.type == "video" -> Icons.Filled.Videocam
        else -> Icons.Filled.Call
    }
    val accentColor = when {
        isMissed -> AppColorPalette.LogoutDanger
        isCompleted -> AppColorPalette.Success
        else -> AppColorPalette.TextSecondary
    }
    val bubbleBackground = when {
        isMissed -> AppColorPalette.SurfaceVariant
        isCompleted -> AppColorPalette.StatusComplete.background
        else -> AppColorPalette.SurfaceVariant
    }
    val label = buildCallLogLabel(message.messageText, call?.durationSeconds)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            modifier = Modifier
                .clip(RoundedCornerShape(20.dp))
                .background(bubbleBackground)
                .padding(horizontal = 16.dp, vertical = 10.dp),
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = accentColor,
                modifier = Modifier.size(18.dp),
            )
            Text(
                text = label,
                color = accentColor,
                fontSize = 13.sp,
                fontWeight = FontWeight.Medium,
            )
        }
        if (message.sentAt.isNotBlank()) {
            Spacer(modifier = Modifier.height(4.dp))
            Text(
                text = chatClockTimeLabel(message.sentAt),
                color = AppColorPalette.TextSecondary,
                fontSize = 11.sp,
            )
        }
    }
}

private fun buildCallLogLabel(messageText: String, durationSeconds: Int?): String {
    val duration = durationSeconds ?: 0
    if (duration <= 0) return messageText
    val minutes = duration / 60
    val seconds = duration % 60
    val formatted = "$minutes:${seconds.toString().padStart(2, '0')}"
    return "$messageText · $formatted"
}

private val REACTION_EMOJIS = listOf("👍", "❤️", "😂", "😮", "😢", "🔥", "🐓", "🍗")

@OptIn(ExperimentalFoundationApi::class)
@Composable
private fun MessageBubble(
    message: ChatMessage,
    isMine: Boolean,
    senderName: String,
    showSenderName: Boolean,
    currentUserId: Int? = null,
    onApproveAiAction: ((Int, Int) -> Unit)? = null,
    onRejectAiAction: ((Int, Int) -> Unit)? = null,
    onReact: ((ChatMessage, String) -> Unit)? = null,
    onRemoveReaction: ((ChatMessage, String) -> Unit)? = null,
) {
    var showReactionPicker by remember(message.id) { mutableStateOf(false) }
    val canReact = onReact != null && message.id > 0

    Column(modifier = Modifier.fillMaxWidth()) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
        ) {
            Box {
                Column(
                    modifier = Modifier
                        .fillMaxWidth(0.78f)
                        .clip(RoundedCornerShape(14.dp))
                        .background(if (isMine) AppColorPalette.Primary else AppColorPalette.SurfaceVariant)
                        .combinedClickable(
                            onClick = {},
                            onLongClick = { if (canReact) showReactionPicker = true },
                        )
                        .padding(12.dp),
                ) {
                    if (!isMine && showSenderName) {
                        Text(
                            text = senderName,
                            color = AppColorPalette.Primary,
                            style = MaterialTheme.typography.labelSmall,
                            fontWeight = FontWeight.SemiBold,
                        )

                        Spacer(modifier = Modifier.height(4.dp))
                    }

                    if (message.messageType == ChatMessageType.AI_ACTION && message.meta != null) {
                        AiActionCard(
                            conversationId = message.conversationId,
                            meta = message.meta,
                            currentUserId = currentUserId ?: 0,
                            onApprove = { cid, aid -> onApproveAiAction?.invoke(cid, aid) },
                            onReject = { cid, aid -> onRejectAiAction?.invoke(cid, aid) }
                        )
                    } else if (message.messageType == ChatMessageType.IMAGE) {
                        if (!message.photoUrl.isNullOrBlank()) {
                            AsyncImage(
                                model = message.photoUrl,
                                contentDescription = null,
                                contentScale = ContentScale.Crop,
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .heightIn(min = 140.dp, max = 260.dp)
                                    .clip(RoundedCornerShape(10.dp)),
                            )
                        } else {
                            Text(
                                text = message.messageText.ifBlank { "Photo" },
                                color = if (isMine) AppColorPalette.OnPrimary else AppColorPalette.TextPrimary,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Medium,
                                ),
                            )
                        }

                        if (!message.photoUrl.isNullOrBlank() &&
                            message.messageText.isNotBlank() &&
                            !message.messageText.equals("Photo", ignoreCase = true)
                        ) {
                            Spacer(modifier = Modifier.height(8.dp))
                            Text(
                                text = message.messageText,
                                color = if (isMine) AppColorPalette.OnPrimary else AppColorPalette.TextPrimary,
                                style = MaterialTheme.typography.bodyLarge.copy(
                                    fontFamily = FontFamily.SansSerif,
                                    fontWeight = FontWeight.Medium,
                                ),
                            )
                        }
                    } else {
                        Text(
                            text = message.messageText,
                            color = if (isMine) AppColorPalette.OnPrimary else AppColorPalette.TextPrimary,
                            style = MaterialTheme.typography.bodyLarge.copy(
                                fontFamily = FontFamily.SansSerif,
                                fontWeight = FontWeight.Medium,
                            ),
                        )
                    }

                    Spacer(modifier = Modifier.height(4.dp))

                    Text(
                        text = chatClockTimeLabel(message.sentAt),
                        color = if (isMine) AppColorPalette.OnPrimary.copy(alpha = 0.7f) else AppColorPalette.TextSecondary,
                        style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.SansSerif),
                        modifier = Modifier.align(Alignment.End),
                    )
                }

                DropdownMenu(
                    expanded = showReactionPicker,
                    onDismissRequest = { showReactionPicker = false },
                ) {
                    Row(
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        horizontalArrangement = Arrangement.spacedBy(4.dp),
                    ) {
                        REACTION_EMOJIS.forEach { emoji ->
                            Text(
                                text = emoji,
                                fontSize = 22.sp,
                                modifier = Modifier
                                    .clip(RoundedCornerShape(8.dp))
                                    .clickable {
                                        onReact?.invoke(message, emoji)
                                        showReactionPicker = false
                                    }
                                    .padding(6.dp),
                            )
                        }
                    }
                }
            }
        }

        if (message.reactions.isNotEmpty()) {
            MessageReactionRow(
                reactions = message.reactions,
                currentUserId = currentUserId,
                isMine = isMine,
                onToggle = { emoji, hasReacted ->
                    if (hasReacted) {
                        onRemoveReaction?.invoke(message, emoji)
                    } else {
                        onReact?.invoke(message, emoji)
                    }
                },
            )
        }
    }
}

@Composable
private fun MessageReactionRow(
    reactions: List<ChatMessageReaction>,
    currentUserId: Int?,
    isMine: Boolean,
    onToggle: (emoji: String, hasReacted: Boolean) -> Unit,
) {
    FlowRow(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
    ) {
        reactions.forEach { reaction ->
            val hasReacted = currentUserId != null && currentUserId in reaction.userIds
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp),
                modifier = Modifier
                    .padding(end = 4.dp, bottom = 4.dp)
                    .clip(RoundedCornerShape(50))
                    .background(
                        if (hasReacted) AppColorPalette.Primary.copy(alpha = 0.18f) else AppColorPalette.SurfaceVariant
                    )
                    .border(
                        width = 1.dp,
                        color = if (hasReacted) AppColorPalette.Primary.copy(alpha = 0.4f) else AppColorPalette.Border,
                        shape = RoundedCornerShape(50),
                    )
                    .clickable { onToggle(reaction.emoji, hasReacted) }
                    .padding(horizontal = 8.dp, vertical = 3.dp),
            ) {
                Text(text = reaction.emoji, fontSize = 13.sp)
                Text(
                    text = reaction.count.toString(),
                    fontSize = 11.sp,
                    fontWeight = FontWeight.SemiBold,
                    color = if (hasReacted) AppColorPalette.Primary else AppColorPalette.TextSecondary,
                )
            }
        }
    }
}

@Composable
private fun CreateGroupDialog(
    uiState: ChatUiState,
    currentEmployeeId: Int,
    onDismiss: () -> Unit,
    onNameChanged: (String) -> Unit,
    onMemberSearchChanged: (String) -> Unit,
    onToggleMember: (Int) -> Unit,
    onCreate: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val dialog = uiState.createGroupDialog
    val availableUsers = uiState.allUsers.filter { user ->
        user.id != currentEmployeeId &&
                (dialog.memberSearch.isBlank() ||
                        user.name.contains(dialog.memberSearch, ignoreCase = true) ||
                        user.email.contains(dialog.memberSearch, ignoreCase = true))
    }

    GroupDialogShell(
        title = strings.text("Create group"),
        onDismiss = onDismiss,
        error = dialog.error,
        isSaving = dialog.isSaving,
        onCancel = onDismiss,
        onSave = onCreate,
        saveLabel = strings.text("Create"),
    ) {
        OutlinedTextField(
            value = dialog.name,
            onValueChange = onNameChanged,
            label = { Text(strings.text("Group name")) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = chatTextFieldColors(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = strings.text("Add members"),
            color = AppColorPalette.TextPrimary,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        AppSearchField(
            value = dialog.memberSearch,
            onValueChange = onMemberSearchChanged,
            placeholder = strings.text("Search people..."),
        )

        Spacer(modifier = Modifier.height(8.dp))

        MemberPickerList(
            users = availableUsers,
            selectedIds = dialog.selectedMemberIds,
            onToggle = onToggleMember,
            emptyText = strings.text("No users found"),
        )
    }
}

@Composable
private fun EditGroupDialog(
    settings: GroupSettingsState,
    currentEmployeeId: Int,
    allUsers: List<User>,
    onDismiss: () -> Unit,
    onNameChanged: (String) -> Unit,
    onMemberSearchChanged: (String) -> Unit,
    onToggleAddMember: (Int) -> Unit,
    onToggleRemoveMember: (Int) -> Unit,
    onSave: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val memberIds = settings.members.map { it.id }.toSet()
    val availableToAdd = allUsers.filter { user ->
        user.id != currentEmployeeId &&
                !memberIds.contains(user.id) &&
                (settings.memberSearch.isBlank() ||
                        user.name.contains(settings.memberSearch, ignoreCase = true) ||
                        user.email.contains(settings.memberSearch, ignoreCase = true))
    }

    GroupDialogShell(
        title = strings.text("Edit group"),
        onDismiss = onDismiss,
        error = settings.error,
        isSaving = settings.isSaving,
        onCancel = onDismiss,
        onSave = onSave,
        saveLabel = strings.text("Save changes"),
    ) {
        OutlinedTextField(
            value = settings.name,
            onValueChange = onNameChanged,
            label = { Text(strings.text("Group name")) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = chatTextFieldColors(),
        )

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = strings.text("Current members"),
            color = AppColorPalette.TextPrimary,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        settings.members.forEach { member ->
            val isCurrentUser = member.id == currentEmployeeId
            val markedForRemoval = settings.membersToRemove.contains(member.id)

            MemberRow(
                participant = member,
                trailing = {
                    if (!isCurrentUser) {
                        TextButton(
                            onClick = { onToggleRemoveMember(member.id) },
                        ) {
                            Text(
                                text = if (markedForRemoval) {
                                    strings.text("Undo remove")
                                } else {
                                    strings.text("Remove")
                                },
                                color = if (markedForRemoval) AppColorPalette.TextSecondary else AppColorPalette.Error,
                            )
                        }
                    } else {
                        Text(
                            text = strings.text("You"),
                            color = AppColorPalette.TextSecondary,
                            style = MaterialTheme.typography.labelSmall,
                        )
                    }
                },
                dimmed = markedForRemoval,
            )
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = strings.text("Add members"),
            color = AppColorPalette.TextPrimary,
            fontWeight = FontWeight.SemiBold,
        )

        Spacer(modifier = Modifier.height(8.dp))

        AppSearchField(
            value = settings.memberSearch,
            onValueChange = onMemberSearchChanged,
            placeholder = strings.text("Search people..."),
        )

        Spacer(modifier = Modifier.height(8.dp))

        MemberPickerList(
            users = availableToAdd,
            selectedIds = settings.selectedToAdd,
            onToggle = onToggleAddMember,
            emptyText = strings.text("No users found"),
        )
    }
}

@Composable
private fun GroupDialogShell(
    title: String,
    onDismiss: () -> Unit,
    error: String?,
    isSaving: Boolean,
    onCancel: () -> Unit,
    onSave: () -> Unit,
    saveLabel: String,
    content: @Composable () -> Unit,
) {
    Dialog(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .heightIn(max = 560.dp)
                .background(AppColorPalette.Surface, RoundedCornerShape(20.dp))
                .border(1.dp, AppColorPalette.Border, RoundedCornerShape(20.dp))
                .padding(20.dp),
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Text(
                    text = title,
                    color = AppColorPalette.TextPrimary,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
                TextButton(onClick = onDismiss) {
                    Text("✕", color = AppColorPalette.TextSecondary)
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState()),
            ) {
                content()
            }

            if (error != null) {
                Spacer(modifier = Modifier.height(8.dp))
                Text(text = error, color = AppColorPalette.Error)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically,
            ) {
                TextButton(onClick = onCancel, enabled = !isSaving) {
                    Text(strings.text("Cancel"), color = AppColorPalette.TextSecondary)
                }

                Spacer(modifier = Modifier.width(8.dp))

                Button(
                    onClick = onSave,
                    enabled = !isSaving,
                    colors = ButtonDefaults.buttonColors(
                        containerColor = AppColorPalette.Primary,
                        contentColor = AppColorPalette.OnPrimary,
                    ),
                ) {
                    if (isSaving) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(18.dp),
                            color = AppColorPalette.OnPrimary,
                            strokeWidth = 2.dp,
                        )
                    } else {
                        Text(saveLabel)
                    }
                }
            }
        }
    }
}

@Composable
private fun MemberPickerList(
    users: List<User>,
    selectedIds: Set<Int>,
    onToggle: (Int) -> Unit,
    emptyText: String,
) {
    if (users.isEmpty()) {
        Text(
            text = emptyText,
            color = AppColorPalette.TextSecondary,
            modifier = Modifier.padding(vertical = 8.dp),
        )
        return
    }

    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        users.forEach { user ->
            val participant = ChatParticipant(
                id = user.id,
                name = user.name,
                email = user.email,
                role = user.role.displayName,
                status = user.status,
            )
            MemberRow(
                participant = participant,
                leading = {
                    Checkbox(
                        checked = selectedIds.contains(user.id),
                        onCheckedChange = { onToggle(user.id) },
                        colors = CheckboxDefaults.colors(
                            checkedColor = AppColorPalette.Primary,
                            uncheckedColor = AppColorPalette.TextSecondary,
                        ),
                    )
                },
                onClick = { onToggle(user.id) },
            )
        }
    }
}

@Composable
private fun MemberRow(
    participant: ChatParticipant,
    modifier: Modifier = Modifier,
    leading: (@Composable () -> Unit)? = null,
    trailing: (@Composable () -> Unit)? = null,
    dimmed: Boolean = false,
    onClick: (() -> Unit)? = null,
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .then(if (onClick != null) Modifier.clickable(onClick = onClick) else Modifier)
            .background(if (dimmed) AppColorPalette.SurfaceVariant.copy(alpha = 0.5f) else AppColorPalette.SurfaceVariant)
            .padding(horizontal = 8.dp, vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        leading?.invoke()

        ParticipantAvatar(
            participant = participant,
            modifier = Modifier.size(36.dp),
        )

        Spacer(modifier = Modifier.width(10.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = participant.name,
                color = if (dimmed) AppColorPalette.TextSecondary else AppColorPalette.TextPrimary,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Text(
                text = participant.role.ifBlank { participant.email },
                color = AppColorPalette.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                style = MaterialTheme.typography.labelSmall,
            )
        }

        trailing?.invoke()
    }
}

@Composable
private fun chatTextFieldColors() = OutlinedTextFieldDefaults.colors(
    focusedTextColor = AppColorPalette.TextPrimary,
    unfocusedTextColor = AppColorPalette.TextPrimary,
    focusedContainerColor = AppColorPalette.FieldBackground,
    unfocusedContainerColor = AppColorPalette.FieldBackground,
    cursorColor = AppColorPalette.Primary,
    focusedBorderColor = AppColorPalette.Primary,
    unfocusedBorderColor = AppColorPalette.Border,
    focusedPlaceholderColor = AppColorPalette.TextSecondary,
    unfocusedPlaceholderColor = AppColorPalette.TextSecondary,
    focusedLabelColor = AppColorPalette.TextSecondary,
    unfocusedLabelColor = AppColorPalette.TextSecondary,
)

private val strings
    @Composable get() = LocalAppStrings.current

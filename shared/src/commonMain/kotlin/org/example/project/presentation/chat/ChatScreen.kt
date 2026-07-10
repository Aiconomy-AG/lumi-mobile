package org.example.project.presentation.chat

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import org.example.project.presentation.components.AppBackButton
import org.example.project.presentation.components.AppSearchField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.example.project.data.chat.chatClockTimeLabel
import org.example.project.domain.chat.ChatMessage
import org.example.project.presentation.components.DismissKeyboardOnTapOutside
import org.example.project.presentation.localization.LocalAppStrings
import org.example.project.presentation.theme.AppColorPalette


@Composable
fun ChatScreen(
    viewModel: ChatViewModel,
    currentEmployeeId: Int,
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
                    onSearchQueryChanged = viewModel::onSearchQueryChanged,
                    onContactClick = viewModel::selectContact,
                )
            }
        } else {
            ConversationDetailScreen(
                uiState = uiState,
                currentEmployeeId = currentEmployeeId,
                onBackClick = viewModel::backToConversationList,
                onMessageDraftChanged = viewModel::onMessageDraftChanged,
                onSendClick = viewModel::sendMessage,
            )
        }
    }
}

@Composable
private fun ConversationListScreen(
    uiState: ChatUiState,
    onSearchQueryChanged: (String) -> Unit,
    onContactClick: (ChatContactItem) -> Unit,
) {
    val strings = LocalAppStrings.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
    ) {
        AppSearchField(
            value = uiState.searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = strings.text("Search chat or person..."),
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            uiState.isLoading && uiState.contacts.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColorPalette.Primary)
                }
            }

            uiState.error != null -> {
                Text(
                    text = uiState.error,
                    color = AppColorPalette.Error,
                    modifier = Modifier.padding(16.dp),
                )
            }

            uiState.sortedContacts.isEmpty() -> {
                Text(
                    text = strings.text("No users found"),
                    color = AppColorPalette.TextSecondary,
                    modifier = Modifier.padding(16.dp),
                )
            }

            else -> {
                LazyColumn(
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(uiState.sortedContacts, key = { it.user.id }) { contact ->
                        ContactRow(
                            contact = contact,
                            onClick = { onContactClick(contact) },
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun ContactRow(
    contact: ChatContactItem,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(AppColorPalette.Surface)
            .clickable(onClick = onClick)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier.size(44.dp),
            contentAlignment = Alignment.Center,
        ) {
            Box(
                modifier = Modifier
                    .size(44.dp)
                    .background(AppColorPalette.SelectionOverlay, CircleShape),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = contact.initials,
                    color = AppColorPalette.TextPrimary,
                    fontWeight = FontWeight.Bold,
                )
            }

            if (contact.hasUnread) {
                Box(
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .size(10.dp)
                        .background(AppColorPalette.Error, CircleShape),
                )
            }
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = contact.title,
                    color = AppColorPalette.TextPrimary,
                    fontWeight = FontWeight.SemiBold,
                    modifier = Modifier.weight(1f),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                Text(
                    text = chatClockTimeLabel(contact.lastSentAt),
                    color = AppColorPalette.TextSecondary,
                    style = MaterialTheme.typography.labelSmall,
                )
            }

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = contact.lastMessage,
                color = AppColorPalette.TextSecondary,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}

@Composable
private fun ConversationDetailScreen(
    uiState: ChatUiState,
    currentEmployeeId: Int,
    onBackClick: () -> Unit,
    onMessageDraftChanged: (String) -> Unit,
    onSendClick: () -> Unit,
) {
    val selectedConversation = uiState.selectedConversation ?: return
    val listState = rememberLazyListState()
    val strings = LocalAppStrings.current

    LaunchedEffect(uiState.messages.size, selectedConversation.conversation.id) {
        if (uiState.messages.isNotEmpty()) {
            listState.animateScrollToItem(uiState.messages.lastIndex)
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize(),
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
                MessageBubble(
                    message = message,
                    isMine = message.senderId == currentEmployeeId,
                    senderName = uiState.usersById[message.senderId]?.name ?: strings.text("Unknown sender"),
                )
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
            OutlinedTextField(
                value = uiState.messageDraft,
                onValueChange = onMessageDraftChanged,
                placeholder = { Text(strings.text("Write a message...")) },
                modifier = Modifier.weight(1f),
                maxLines = 4,
                colors = chatTextFieldColors(),
            )

            Spacer(modifier = Modifier.width(8.dp))

            Button(
                onClick = onSendClick,
                enabled = uiState.messageDraft.isNotBlank(),
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
private fun MessageBubble(
    message: ChatMessage,
    isMine: Boolean,
    senderName: String,
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = if (isMine) Arrangement.End else Arrangement.Start,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth(0.78f)
                .clip(RoundedCornerShape(14.dp))
                .background(if (isMine) AppColorPalette.Primary else AppColorPalette.SurfaceVariant)
                .padding(12.dp),
        ) {
            if (!isMine) {
                Text(
                    text = senderName,
                    color = AppColorPalette.TextSecondary,
                    style = MaterialTheme.typography.labelSmall,
                    fontWeight = FontWeight.SemiBold,
                )

                Spacer(modifier = Modifier.height(4.dp))
            }

            Text(
                text = message.messageText,
                color = if (isMine) AppColorPalette.OnPrimary else AppColorPalette.TextPrimary,
                style = MaterialTheme.typography.bodyLarge.copy(
                    fontFamily = FontFamily.SansSerif,
                    fontWeight = FontWeight.Medium,
                ),
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = chatClockTimeLabel(message.sentAt),
                color = if (isMine) AppColorPalette.OnPrimary.copy(alpha = 0.7f) else AppColorPalette.TextSecondary,
                style = MaterialTheme.typography.labelSmall.copy(fontFamily = FontFamily.SansSerif),
                modifier = Modifier.align(Alignment.End),
            )
        }
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
)

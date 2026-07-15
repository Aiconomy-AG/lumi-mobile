package org.example.project.presentation.returns

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.example.project.domain.returns.ReturnDisplayItem
import org.example.project.domain.returns.ReturnRequest
import org.example.project.domain.returns.ReturnStatus
import org.example.project.presentation.components.AppButton
import org.example.project.presentation.components.AppDetailField
import org.example.project.presentation.components.AppDetailGridRows
import org.example.project.presentation.components.AppDetailOverlay
import org.example.project.presentation.components.AppListContainer
import org.example.project.presentation.components.AppListRow
import org.example.project.presentation.components.AppOutlinedButton
import org.example.project.presentation.components.AppSearchField
import org.example.project.presentation.components.AppStatusBadge
import org.example.project.presentation.components.DismissKeyboardOnTapOutside
import org.example.project.presentation.components.PlatformBackHandler
import org.example.project.presentation.localization.LocalAppStrings
import org.example.project.presentation.theme.AppColorPalette
import org.example.project.presentation.theme.AppComponentDefaults
import org.example.project.presentation.theme.AppDimensions
import org.example.project.presentation.theme.AppTextStyles
import org.example.project.presentation.theme.StatusColor
import org.example.project.presentation.theme.formatChf

@Composable
fun ReturnsScreen(
    viewModel: ReturnsViewModel,
    modifier: Modifier = Modifier,
    openReturnId: Int? = null,
    onOpenReturnConsumed: () -> Unit = {},
) {
    val state by viewModel.state.collectAsState()
    val strings = LocalAppStrings.current

    LaunchedEffect(openReturnId) {
        val returnId = openReturnId ?: return@LaunchedEffect
        viewModel.openReturn(returnId)
        onOpenReturnConsumed()
    }

    PlatformBackHandler(
        enabled = state.selectedReturn != null,
        onBack = viewModel::closeReturn,
    )

    Box(modifier = modifier.fillMaxSize()) {
        DismissKeyboardOnTapOutside(modifier = Modifier.fillMaxSize()) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .background(AppColorPalette.Background)
                    .padding(AppDimensions.ScreenPadding),
            ) {
        ReturnsHeader(
                    requestedCount = state.requestedCount,
                    activeCount = state.activeCount,
                    searchQuery = state.searchQuery,
                    onSearchQueryChanged = viewModel::onSearchQueryChanged,
                )

                state.errorMessage?.let { message ->
                    Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))
                    Text(
                        text = strings.format("Error: {message}", "message" to message),
                        color = AppColorPalette.Error,
                        style = AppTextStyles.Emphasis,
                    )
                }

                Spacer(modifier = Modifier.height(AppDimensions.SectionSpacing))

                when {
                    state.isLoading && state.returns.isEmpty() -> {
                        Box(
                            modifier = Modifier.fillMaxSize(),
                            contentAlignment = Alignment.Center,
                        ) {
                            CircularProgressIndicator(color = AppColorPalette.Primary)
                        }
                    }

                    else -> {
                        ReturnsTable(
                            returns = state.filteredReturns,
                            onReturnClick = { viewModel.openReturn(it.id) },
                            modifier = Modifier.weight(1f),
                        )
                    }
                }
            }
        }

        state.selectedReturn?.let { returnRequest ->
            ReturnDetailsOverlay(
                returnRequest = returnRequest,
                isSaving = state.isSaving,
                errorMessage = state.dialogErrorMessage,
                onBackClick = viewModel::closeReturn,
                onSave = { status, notes ->
                    viewModel.updateReturn(
                        returnId = returnRequest.id,
                        status = status,
                        notes = notes,
                    )
                },
            )
        }
    }
}

@Composable
private fun ReturnsHeader(
    requestedCount: Int,
    activeCount: Int,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
) {
    val strings = LocalAppStrings.current

    Column {
        Text(
            text = strings.format(
                "{count} requested",
                "count" to requestedCount.toString(),
            ) + " · " + strings.format(
                "{count} active",
                "count" to activeCount.toString(),
            ),
            color = AppColorPalette.TextSecondary,
            style = AppTextStyles.Emphasis,
        )

        Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

        AppSearchField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = strings.text("Search returns..."),
        )
    }
}

@Composable
private fun ReturnsTable(
    returns: List<ReturnRequest>,
    onReturnClick: (ReturnRequest) -> Unit,
    modifier: Modifier = Modifier,
) {
    AppListContainer(
        items = returns,
        emptyMessage = LocalAppStrings.current.text("No returns found."),
        modifier = modifier,
        key = { it.id },
    ) { returnRequest ->
        ReturnRow(
            returnRequest = returnRequest,
            onClick = { onReturnClick(returnRequest) },
        )
    }
}

@Composable
private fun ReturnRow(
    returnRequest: ReturnRequest,
    onClick: () -> Unit,
) {
    AppListRow(onClick = onClick) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = returnRequest.orderLabel(),
                modifier = Modifier.weight(1f),
                color = AppColorPalette.TextPrimary,
                style = AppTextStyles.Emphasis,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Text(
                text = returnRequest.refundLabel(),
                color = AppColorPalette.TextPrimary,
                maxLines = 1,
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = returnRequest.customerLabel(),
                modifier = Modifier.weight(1f),
                color = AppColorPalette.TextPrimary.copy(alpha = 0.85f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )

            Spacer(modifier = Modifier.width(AppDimensions.TinySpacing))

            ReturnStatusBadge(status = returnRequest.status)
        }

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = returnRequest.reason.ifBlank { "—" },
            color = AppColorPalette.TextSecondary,
            maxLines = 2,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
private fun ReturnDetailsOverlay(
    returnRequest: ReturnRequest,
    isSaving: Boolean,
    errorMessage: String?,
    onBackClick: () -> Unit,
    onSave: (ReturnStatus, String) -> Unit,
) {
    val strings = LocalAppStrings.current
    var selectedStatus by remember(returnRequest.id, returnRequest.status) {
        mutableStateOf(returnRequest.status)
    }
    var notes by remember(returnRequest.id, returnRequest.notes) {
        mutableStateOf(returnRequest.notes.orEmpty())
    }

    AppDetailOverlay(
        title = strings.format("Return #{id}", "id" to returnRequest.id.toString()),
        onBackClick = onBackClick,
        trailingContent = {
            ReturnStatusBadge(status = selectedStatus)
        },
        bottomContent = {
            AppButton(
                onClick = { onSave(selectedStatus, notes) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving,
            ) {
                Text(if (isSaving) strings.text("Saving...") else strings.text("Save changes"))
            }
        },
    ) {
        AppDetailGridRows(
            rows = listOf(
                listOf(
                    AppDetailField(strings.text("Order"), returnRequest.orderLabel()),
                    AppDetailField(strings.text("Reason"), returnRequest.reason),
                ),
                listOf(
                    AppDetailField(strings.text("Customer"), returnRequest.email),
                ),
                listOf(
                    AppDetailField(strings.text("Refund amount"), returnRequest.refundLabel()),
                    AppDetailField(strings.text("Created"), returnRequest.createdAt.dateLabel()),
                ),
                listOf(
                    AppDetailField(strings.text("Received"), returnRequest.receivedAt.dateLabel()),
                    AppDetailField(strings.text("Refunded"), returnRequest.refundedAt.dateLabel()),
                ),
            ),
        )

        Spacer(modifier = Modifier.height(AppDimensions.SectionSpacing))

        Text(
            text = strings.text("Status"),
            color = AppColorPalette.TextPrimary,
            style = AppTextStyles.Emphasis,
        )

        Spacer(modifier = Modifier.height(AppDimensions.TinySpacing))

        FlowRow(
            horizontalArrangement = Arrangement.spacedBy(8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            ReturnStatus.entries.forEach { status ->
                val selected = status == selectedStatus
                if (selected) {
                    AppButton(
                        onClick = { selectedStatus = status },
                    ) {
                        Text(strings.returnStatus(status))
                    }
                } else {
                    AppOutlinedButton(
                        onClick = { selectedStatus = status },
                    ) {
                        Text(strings.returnStatus(status), color = AppColorPalette.TextPrimary)
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

        OutlinedTextField(
            value = notes,
            onValueChange = { notes = it },
            label = {
                Text(strings.text("Notes"))
            },
            modifier = Modifier.fillMaxWidth(),
            minLines = 3,
            colors = AppComponentDefaults.appTextFieldColors(),
        )

        errorMessage?.let { message ->
            Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))
            Text(
                text = message,
                color = AppColorPalette.Error,
                style = AppTextStyles.Emphasis,
            )
        }

        Spacer(modifier = Modifier.height(AppDimensions.SectionSpacing))

        Text(
            text = strings.format(
                "{count} return items",
                "count" to returnRequest.items.size.toString(),
            ),
            color = AppColorPalette.TextPrimary,
            style = AppTextStyles.Emphasis,
        )

        Spacer(modifier = Modifier.height(AppDimensions.TinySpacing))

        if (returnRequest.items.isEmpty()) {
            Text(
                text = strings.text("No item details available."),
                color = AppColorPalette.TextSecondary,
            )
        } else {
            returnRequest.items.forEach { item ->
                ReturnItemRow(item = item)
            }
        }
    }
}

@Composable
private fun ReturnItemRow(item: ReturnDisplayItem) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp)
            .background(
                color = AppColorPalette.SurfaceVariant,
                shape = RoundedCornerShape(AppDimensions.ControlCornerRadius),
            )
            .padding(10.dp),
    ) {
        Text(
            text = item.title,
            color = AppColorPalette.TextPrimary,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.SemiBold,
        )
        item.quantity?.let { quantity ->
            Text(
                text = LocalAppStrings.current.format("Quantity: {count}", "count" to quantity.toString()),
                color = AppColorPalette.TextSecondary,
            )
        }
        item.details?.let { details ->
            Text(
                text = details,
                color = AppColorPalette.TextSecondary,
            )
        }
    }
}

@Composable
private fun ReturnStatusBadge(
    status: ReturnStatus,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current
    AppStatusBadge(
        label = strings.returnStatus(status),
        statusColor = status.statusColor(),
        modifier = modifier,
    )
}

private fun ReturnStatus.statusColor(): StatusColor {
    return when (this) {
        ReturnStatus.REQUESTED -> AppColorPalette.StatusInProgress
        ReturnStatus.APPROVED -> AppColorPalette.StatusComplete
        ReturnStatus.REJECTED -> AppColorPalette.StatusBlocked
        ReturnStatus.RECEIVED -> AppColorPalette.StatusToDo
        ReturnStatus.REFUNDED -> AppColorPalette.StatusComplete
    }
}

private fun ReturnRequest.orderLabel(): String {
    return shopifyOrderName
        ?: shopifyOrderId?.let { "#$it" }
        ?: orderId?.let { "#$it" }
        ?: "#$id"
}

private fun ReturnRequest.refundLabel(): String {
    return refundAmount?.let { formatChf(it) } ?: "—"
}

private fun ReturnRequest.customerLabel(): String {
    return email?.takeIf { it.isNotBlank() } ?: "—"
}

private fun String?.dateLabel(): String {
    return this?.take(10)?.takeIf { it.isNotBlank() } ?: "—"
}

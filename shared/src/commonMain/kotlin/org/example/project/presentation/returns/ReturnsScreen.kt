package org.example.project.presentation.returns

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
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
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.example.project.domain.returns.ReturnDisplayItem
import org.example.project.domain.returns.ReturnRequest
import org.example.project.domain.returns.ReturnStatus
import org.example.project.presentation.components.AppPaginationBar
import org.example.project.presentation.components.AppSearchField
import org.example.project.presentation.components.DismissKeyboardOnTapOutside
import org.example.project.presentation.localization.LocalAppStrings
import org.example.project.presentation.theme.AppColorPalette
import org.example.project.presentation.theme.AppComponentDefaults
import org.example.project.presentation.theme.AppDimensions
import org.example.project.presentation.theme.AppTextStyles
import org.example.project.presentation.theme.StatusColor
import org.example.project.presentation.theme.formatChf

private const val RETURNS_PAGE_SIZE = 6

@Composable
fun ReturnsScreen(
    viewModel: ReturnsViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val strings = LocalAppStrings.current
    var currentPage by remember { mutableStateOf(0) }
    val filteredReturns = state.filteredReturns
    val totalPages = maxOf(1, (filteredReturns.size + RETURNS_PAGE_SIZE - 1) / RETURNS_PAGE_SIZE)
    val pagedReturns = filteredReturns
        .drop(currentPage * RETURNS_PAGE_SIZE)
        .take(RETURNS_PAGE_SIZE)

    LaunchedEffect(filteredReturns.size) {
        if (currentPage > totalPages - 1) {
            currentPage = totalPages - 1
        }
    }

    DismissKeyboardOnTapOutside(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColorPalette.Background)
                .padding(AppDimensions.ScreenPadding),
        ) {
        ReturnsHeader(
            returnCount = state.returns.size,
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

            pagedReturns.isEmpty() -> {
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = strings.text("No returns found."),
                        color = AppColorPalette.TextSecondary,
                    )
                }
            }

            else -> {
                ReturnsTable(
                    returns = pagedReturns,
                    onReturnClick = { viewModel.openReturn(it.id) },
                    modifier = Modifier.weight(1f),
                )

                Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

                AppPaginationBar(
                    currentPage = currentPage,
                    totalPages = totalPages,
                    onPreviousClick = { if (currentPage > 0) currentPage-- },
                    onNextClick = { if (currentPage < totalPages - 1) currentPage++ },
                )
            }
        }
    }
    }

    state.selectedReturn?.let { returnRequest ->
        ReturnDetailsDialog(
            returnRequest = returnRequest,
            isSaving = state.isSaving,
            onDismiss = viewModel::closeReturn,
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

@Composable
private fun ReturnsHeader(
    returnCount: Int,
    requestedCount: Int,
    activeCount: Int,
    searchQuery: String,
    onSearchQueryChanged: (String) -> Unit,
) {
    val strings = LocalAppStrings.current

    Column {
        Text(
            text = strings.text("Returns"),
            color = AppColorPalette.TextPrimary,
            style = AppTextStyles.PageTitle,
        )

        Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

        Text(
            text = strings.format(
                "{count} returns",
                "count" to returnCount.toString(),
            ) + " · " + strings.format(
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
    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = AppColorPalette.Border,
                shape = RoundedCornerShape(AppDimensions.TableCornerRadius),
            )
            .background(
                color = AppColorPalette.Surface,
                shape = RoundedCornerShape(AppDimensions.TableCornerRadius),
            )
            .padding(12.dp),
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            returns.forEachIndexed { index, returnRequest ->
                ReturnRow(
                    returnRequest = returnRequest,
                    onClick = { onReturnClick(returnRequest) },
                )
                if (index != returns.lastIndex) {
                    HorizontalDivider(color = AppColorPalette.Border)
                }
            }
        }
    }
}

@Composable
private fun ReturnRow(
    returnRequest: ReturnRequest,
    onClick: () -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
    ) {
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

            Spacer(modifier = Modifier.width(AppDimensions.TinySpacing))

            ReturnStatusBadge(status = returnRequest.status)
        }

        Spacer(modifier = Modifier.height(6.dp))

        Text(
            text = returnRequest.customerLabel(),
            color = AppColorPalette.TextPrimary.copy(alpha = 0.85f),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )

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
private fun ReturnDetailsDialog(
    returnRequest: ReturnRequest,
    isSaving: Boolean,
    onDismiss: () -> Unit,
    onSave: (ReturnStatus, String) -> Unit,
) {
    val strings = LocalAppStrings.current
    var selectedStatus by remember(returnRequest.id, returnRequest.status) {
        mutableStateOf(returnRequest.status)
    }
    var notes by remember(returnRequest.id, returnRequest.notes) {
        mutableStateOf(returnRequest.notes.orEmpty())
    }
    val scrollState = rememberScrollState()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = AppColorPalette.OverlaySurface,
        title = {
            Text(
                text = strings.format("Return #{id}", "id" to returnRequest.id.toString()),
                color = AppColorPalette.TextPrimary,
            )
        },
        text = {
            Column(
                modifier = Modifier
                    .heightIn(max = 560.dp)
                    .verticalScroll(scrollState),
            ) {
                DetailRow(strings.text("Order"), returnRequest.orderLabel())
                DetailRow(strings.text("Customer"), returnRequest.email ?: "—")
                DetailRow(strings.text("Reason"), returnRequest.reason.ifBlank { "—" })
                DetailRow(strings.text("Refund amount"), returnRequest.refundLabel())
                DetailRow(strings.text("Created"), returnRequest.createdAt.dateLabel())
                DetailRow(strings.text("Received"), returnRequest.receivedAt.dateLabel())
                DetailRow(strings.text("Refunded"), returnRequest.refundedAt.dateLabel())

                Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

                Text(
                    text = strings.text("Status"),
                    color = AppColorPalette.TextSecondary,
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
                            Button(
                                onClick = { selectedStatus = status },
                                colors = AppComponentDefaults.primaryButtonColors(),
                            ) {
                                Text(strings.returnStatus(status))
                            }
                        } else {
                            OutlinedButton(
                                onClick = { selectedStatus = status },
                            ) {
                                Text(strings.returnStatus(status))
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

                Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

                Text(
                    text = strings.format(
                        "{count} return items",
                        "count" to returnRequest.items.size.toString(),
                    ),
                    color = AppColorPalette.TextSecondary,
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
        },
        confirmButton = {
            Button(
                onClick = { onSave(selectedStatus, notes) },
                enabled = !isSaving,
                colors = AppComponentDefaults.primaryButtonColors(),
            ) {
                Text(if (isSaving) strings.text("Saving...") else strings.text("Save changes"))
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text(strings.text("Close"))
            }
        },
    )
}

@Composable
private fun DetailRow(
    label: String,
    value: String,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
    ) {
        Text(
            text = label,
            modifier = Modifier.width(112.dp),
            color = AppColorPalette.TextSecondary,
            fontWeight = FontWeight.SemiBold,
        )
        Text(
            text = value,
            modifier = Modifier.weight(1f),
            color = AppColorPalette.TextPrimary,
        )
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
    val statusColor = status.statusColor()

    Box(
        modifier = modifier.background(
            color = statusColor.background,
            shape = MaterialTheme.shapes.extraSmall,
        ),
    ) {
        Text(
            text = strings.returnStatus(status),
            modifier = Modifier.padding(horizontal = 10.dp, vertical = 4.dp),
            color = statusColor.content,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
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

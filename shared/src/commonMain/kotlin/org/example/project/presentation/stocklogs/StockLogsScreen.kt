package org.example.project.presentation.stocklogs

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import org.example.project.presentation.auditlogs.AuditDateField
import org.example.project.presentation.auditlogs.AuditLogRow
import org.example.project.presentation.components.AppPaginationBar
import org.example.project.presentation.components.DismissKeyboardOnTapOutside
import org.example.project.presentation.localization.LocalAppStrings
import org.example.project.presentation.theme.AppColorPalette
import org.example.project.presentation.theme.AppComponentDefaults
import org.example.project.presentation.theme.AppDimensions
import org.example.project.presentation.theme.AppTextStyles

@Composable
fun StockLogsScreen(
    viewModel: StockLogsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val strings = LocalAppStrings.current

    DismissKeyboardOnTapOutside(
        modifier = modifier.fillMaxSize()
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColorPalette.Background)
                .padding(AppDimensions.ScreenPadding)
        ) {
            Text(
                text = strings.text("Stock Logs"),
                color = AppColorPalette.TextPrimary,
                style = AppTextStyles.PageTitle
            )

            Spacer(
                modifier = Modifier.height(AppDimensions.SmallSpacing)
            )

            StockActionDropdown(
                selectedAction = state.actionFilter,
                availableActions = state.availableActions,
                onActionSelected = viewModel::onActionFilterChanged,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(
                modifier = Modifier.height(AppDimensions.TinySpacing)
            )

            Row(
                modifier = Modifier.fillMaxWidth()
            ) {
                AuditDateField(
                    label = strings.text("From"),
                    value = state.fromFilter,
                    onDateSelected = viewModel::onFromFilterChanged,
                    modifier = Modifier.weight(1f)
                )

                Spacer(
                    modifier = Modifier.width(AppDimensions.TinySpacing)
                )

                AuditDateField(
                    label = strings.text("To"),
                    value = state.toFilter,
                    onDateSelected = viewModel::onToFilterChanged,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(
                modifier = Modifier.height(AppDimensions.SectionSpacing)
            )

            when {
                state.isLoading -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        CircularProgressIndicator(
                            color = AppColorPalette.Primary
                        )
                    }
                }

                state.errorMessage != null -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = state.errorMessage.orEmpty(),
                            color = AppColorPalette.Error
                        )
                    }
                }

                state.logs.isEmpty() -> {
                    Box(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth(),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = strings.text("No stock logs found."),
                            color = AppColorPalette.TextSecondary
                        )
                    }
                }

                else -> {
                    LazyColumn(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxWidth()
                    ) {
                        items(
                            items = state.logs,
                            key = { log -> log.id }
                        ) { log ->
                            AuditLogRow(
                                log = log,
                                expanded = state.expandedLogId == log.id,
                                onClick = {
                                    viewModel.onLogClicked(log.id)
                                }
                            )

                            Spacer(
                                modifier = Modifier.height(
                                    AppDimensions.SmallSpacing
                                )
                            )
                        }
                    }
                }
            }

            if (!state.isLoading) {
                Spacer(
                    modifier = Modifier.height(AppDimensions.SmallSpacing)
                )

                Text(
                    text = buildString {
                        append(
                            strings.format(
                                "Page {page} of {total}",
                                "page" to state.currentPage.toString(),
                                "total" to state.lastPage.toString()
                            )
                        )

                        state.total?.let { total ->
                            append(" • ")
                            append(total)
                        }
                    },
                    color = AppColorPalette.TextSecondary
                )

                Spacer(
                    modifier = Modifier.height(AppDimensions.TinySpacing)
                )

                AppPaginationBar(
                    currentPage = state.currentPage - 1,
                    totalPages = state.lastPage,
                    onPreviousClick = {
                        viewModel.onPageChanged(
                            state.currentPage - 1
                        )
                    },
                    onNextClick = {
                        viewModel.onPageChanged(
                            state.currentPage + 1
                        )
                    }
                )
            }
        }
    }
}

@Composable
private fun StockActionDropdown(
    selectedAction: String,
    availableActions: List<String>,
    onActionSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember {
        mutableStateOf(false)
    }

    val strings = LocalAppStrings.current

    val displayedAction = if (selectedAction.isBlank()) {
        strings.text("All actions")
    } else {
        formatActionLabel(selectedAction)
    }

    Box(modifier = modifier) {
        OutlinedTextField(
            value = displayedAction,
            onValueChange = {},
            readOnly = true,
            label = {
                Text(strings.text("Action"))
            },
            trailingIcon = {
                Text(
                    text = "▼",
                    color = AppColorPalette.TextSecondary
                )
            },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = AppComponentDefaults.appTextFieldColors()
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable {
                    expanded = true
                }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = {
                expanded = false
            }
        ) {
            DropdownMenuItem(
                text = {
                    Text(strings.text("All actions"))
                },
                onClick = {
                    onActionSelected("")
                    expanded = false
                }
            )

            availableActions.forEach { action ->
                DropdownMenuItem(
                    text = {
                        Text(formatActionLabel(action))
                    },
                    onClick = {
                        onActionSelected(action)
                        expanded = false
                    }
                )
            }
        }
    }
}

private fun formatActionLabel(action: String): String {
    return action
        .replace("_", " ")
        .replace("-", " ")
        .split(" ")
        .filter { word ->
            word.isNotBlank()
        }
        .joinToString(" ") { word ->
            word.lowercase().replaceFirstChar { character ->
                character.uppercase()
            }
        }
}

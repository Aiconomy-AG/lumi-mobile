package org.example.project.presentation.auditlogs

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
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import org.example.project.domain.auditlogs.AuditLog
import org.example.project.presentation.components.AppPaginationBar
import org.example.project.presentation.components.AppSearchField
import org.example.project.presentation.components.DismissKeyboardOnTapOutside
import org.example.project.presentation.localization.LocalAppStrings
import org.example.project.presentation.theme.AppColorPalette
import org.example.project.presentation.theme.AppComponentDefaults
import org.example.project.presentation.theme.AppDimensions
import org.example.project.presentation.theme.AppTextStyles

@Composable
fun AuditLogsScreen(
    viewModel: AuditLogsViewModel,
    modifier: Modifier = Modifier
) {
    val state by viewModel.state.collectAsState()
    val strings = LocalAppStrings.current

    DismissKeyboardOnTapOutside(modifier = modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColorPalette.Background)
                .padding(AppDimensions.ScreenPadding),
        ) {
        Text(
            text = strings.text("Audit Logs"),
            color = AppColorPalette.TextPrimary,
            style = AppTextStyles.PageTitle
        )

        Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

        AuditLogsSearchRow(
            searchQuery = state.moduleFilter,
            availableModules = state.logs.map { it.module }.distinct().sorted(),
            onSearchQueryChanged = viewModel::onModuleFilterChanged,
            onModuleSelected = viewModel::onModuleFilterChanged,
        )

        Spacer(modifier = Modifier.height(AppDimensions.TinySpacing))

        Row(modifier = Modifier.fillMaxWidth()) {
            AuditDateField(
                label = strings.text("From"),
                value = state.fromFilter,
                onDateSelected = viewModel::onFromFilterChanged,
                modifier = Modifier.weight(1f),
            )

            Spacer(modifier = Modifier.width(AppDimensions.TinySpacing))

            AuditDateField(
                label = strings.text("To"),
                value = state.toFilter,
                onDateSelected = viewModel::onToFilterChanged,
                modifier = Modifier.weight(1f),
            )
        }

        Spacer(modifier = Modifier.height(AppDimensions.SectionSpacing))

        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator(color = AppColorPalette.Primary)
            }
        } else {
            state.errorMessage?.let { message ->
                Text(text = message, color = AppColorPalette.Error)
                Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))
            }

            if (state.logs.isEmpty()) {
                Box(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = strings.text("No audit logs found."),
                        color = AppColorPalette.TextSecondary,
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f)
                ) {
                    items(state.logs) { log ->
                        AuditLogRow(
                            log = log,
                            expanded = state.expandedLogId == log.id,
                            onClick = { viewModel.onLogClicked(log.id) }
                        )
                        Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))
                    }
                }
            }

            Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))

            if (state.total != null) {
                Text(
                    text = strings.format(
                        "Page {page} of {total}",
                        "page" to state.currentPage.toString(),
                        "total" to state.lastPage.toString(),
                    ) + " • ${state.total}",
                    color = AppColorPalette.TextSecondary,
                )
                Spacer(modifier = Modifier.height(AppDimensions.TinySpacing))
            }

            AppPaginationBar(
                currentPage = state.currentPage - 1,
                totalPages = state.lastPage,
                onPreviousClick = { viewModel.onPageChanges(state.currentPage - 1) },
                onNextClick = { viewModel.onPageChanges(state.currentPage + 1) },
            )
        }
    }
    }
}

@Composable
private fun AuditLogsSearchRow(
    searchQuery: String,
    availableModules: List<String>,
    onSearchQueryChanged: (String) -> Unit,
    onModuleSelected: (String) -> Unit
) {
    var moduleMenuExpanded by remember { mutableStateOf(false) }
    val strings = LocalAppStrings.current

    Row(
        modifier = Modifier.fillMaxWidth(),
        verticalAlignment = Alignment.CenterVertically
    ) {
        AppSearchField(
            value = searchQuery,
            onValueChange = onSearchQueryChanged,
            placeholder = strings.text("Search by module..."),
            modifier = Modifier.weight(1f),
        )

        Spacer(modifier = Modifier.width(AppDimensions.TinySpacing))

        Box {
            OutlinedTextField(
                value = "",
                onValueChange = {},
                readOnly = true,
                placeholder = { Text("▼", color = AppColorPalette.TextSecondary) },
                modifier = Modifier.width(56.dp),
                singleLine = true,
                colors = AppComponentDefaults.appTextFieldColors()
            )

            Box(
                modifier = Modifier
                    .matchParentSize()
                    .clickable { moduleMenuExpanded = true }
            )

            DropdownMenu(
                expanded = moduleMenuExpanded,
                onDismissRequest = { moduleMenuExpanded = false }
            ) {
                DropdownMenuItem(
                    text = { Text(strings.text("All modules")) },
                    onClick = {
                        onModuleSelected("")
                        moduleMenuExpanded = false
                    }
                )

                availableModules.forEach { module ->
                    DropdownMenuItem(
                        text = { Text(module) },
                        onClick = {
                            onModuleSelected(module)
                            moduleMenuExpanded = false
                        }
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AuditDateField(
    label: String,
    value: String,
    onDateSelected: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var showDialog by remember { mutableStateOf(false) }
    val strings = LocalAppStrings.current

    Box(modifier = modifier) {
        OutlinedTextField(
            value = value,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            placeholder = { Text("YYYY-MM-DD", color = AppColorPalette.TextSecondary) },
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            colors = AppComponentDefaults.appTextFieldColors()
        )

        Box(
            modifier = Modifier
                .matchParentSize()
                .clickable { showDialog = true }
        )
    }

    if (showDialog) {
        val datePickerState = rememberDatePickerState()

        DatePickerDialog(
            onDismissRequest = { showDialog = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        datePickerState.selectedDateMillis?.let { millis ->
                            onDateSelected(isoDateFromEpochMillis(millis))
                        }
                        showDialog = false
                    }
                ) {
                    Text("OK")
                }
            },
            dismissButton = {
                TextButton(onClick = { showDialog = false }) {
                    Text(strings.text("Cancel"))
                }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }
}

@Composable
fun AuditLogRow(
    log: AuditLog,
    expanded: Boolean,
    onClick: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColorPalette.Surface, RoundedCornerShape(AppDimensions.TableCornerRadius))
            .clickable { onClick() }
            .padding(AppDimensions.SmallSpacing)
    ) {
        Text(text = log.occurredAt, color = AppColorPalette.TextSecondary)
        Text(
            text = "${log.actorName} • ${log.module} • ${log.action}",
            color = AppColorPalette.TextPrimary
        )
        Text(
            text = log.entityLabel ?: "${log.entityType} #${log.entityId}",
            color = AppColorPalette.TextSecondary
        )
        Text(
            text = log.description ?: "-",
            color = AppColorPalette.TextSecondary
        )

        if (expanded) {
            Spacer(modifier = Modifier.height(AppDimensions.TinySpacing))
            ChangesDiff(log = log)
        }
    }
}

@Composable
private fun ChangesDiff(log: AuditLog) {
    val oldValues = log.changes?.old.orEmpty()
    val newValues = log.changes?.new.orEmpty()
    val fields = (oldValues.keys + newValues.keys).toSet()

    if (fields.isEmpty()) {
        Text(text = "No changes recorded.", color = AppColorPalette.TextSecondary)
        return
    }

    Column {
        fields.forEach { field ->
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(text = field, color = AppColorPalette.TextSecondary)
                Spacer(modifier = Modifier.width(AppDimensions.TinySpacing))
                Text(
                    text = oldValues[field]?.toString() ?: "—",
                    color = AppColorPalette.Error
                )
                Spacer(modifier = Modifier.width(AppDimensions.TinySpacing))
                Text(
                    text = newValues[field]?.toString() ?: "—",
                    color = AppColorPalette.Success
                )
            }
        }
    }
}

private fun isoDateFromEpochMillis(epochMillis: Long): String {
    val epochDay = floorDiv(epochMillis, 86_400_000L)
    val z = epochDay + 719468
    val era = floorDiv(if (z >= 0) z else z - 146096, 146097)
    val doe = z - era * 146097
    val yoe = (doe - doe / 1460 + doe / 36524 - doe / 146096) / 365
    val y = yoe + era * 400
    val doy = doe - (365 * yoe + yoe / 4 - yoe / 100)
    val mp = (5 * doy + 2) / 153
    val day = (doy - (153 * mp + 2) / 5 + 1).toInt()
    val month = (if (mp < 10) mp + 3 else mp - 9).toInt()
    val year = (if (month <= 2) y + 1 else y).toInt()

    return "${year.toString().padStart(4, '0')}-${month.toString().padStart(2, '0')}-${day.toString().padStart(2, '0')}"
}

private fun floorDiv(x: Long, y: Long): Long {
    val q = x / y
    return if ((x % y != 0L) && ((x xor y) < 0)) q - 1 else q
}

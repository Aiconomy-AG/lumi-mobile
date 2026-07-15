package org.example.project.presentation.search

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.CheckCircle
import androidx.compose.material.icons.outlined.Close
import androidx.compose.material.icons.outlined.Folder
import androidx.compose.material.icons.outlined.History
import androidx.compose.material.icons.outlined.LocalShipping
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material.icons.outlined.Replay
import androidx.compose.material.icons.outlined.Search
import androidx.compose.material.icons.outlined.ShoppingCart
import androidx.compose.material3.Checkbox
import androidx.compose.material3.CheckboxDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import org.example.project.domain.search.RecentSearchEntry
import org.example.project.domain.search.RecentSearchKind
import org.example.project.domain.search.SearchResult
import org.example.project.domain.search.SearchResultType
import org.example.project.presentation.components.PlatformBackHandler
import org.example.project.presentation.theme.AppColorPalette
import org.example.project.presentation.theme.AppComponentDefaults

@Composable
fun GlobalSearchOverlay(
    viewModel: GlobalSearchViewModel,
    onDismiss: () -> Unit,
    onNavigate: (GlobalSearchDestination) -> Unit,
) {
    val state by viewModel.uiState.collectAsState()
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        viewModel.setOpen(true)
        focusRequester.requestFocus()
        keyboardController?.show()
    }

    PlatformBackHandler(enabled = true, onBack = onDismiss)

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false),
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .background(AppColorPalette.Background)
                .windowInsetsPadding(WindowInsets.safeDrawing)
                .imePadding(),
        ) {
            SearchHeader(onDismiss = onDismiss)

            OutlinedTextField(
                value = state.query,
                onValueChange = viewModel::onQueryChanged,
                placeholder = {
                    Text(
                        text = "Search tasks, projects, products...",
                        color = AppColorPalette.TextSecondary,
                    )
                },
                leadingIcon = {
                    Icon(
                        imageVector = Icons.Outlined.Search,
                        contentDescription = null,
                        tint = AppColorPalette.TextSecondary,
                    )
                },
                singleLine = true,
                keyboardActions = KeyboardActions(onSearch = { keyboardController?.hide() }),
                colors = AppComponentDefaults.appSearchFieldColors(),
                shape = RoundedCornerShape(18.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
                    .focusRequester(focusRequester),
            )

            Spacer(modifier = Modifier.height(10.dp))

            FlowRow(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                SearchResultType.ordered.forEach { type ->
                    TypeChip(
                        type = type,
                        selected = state.selectedTypes.contains(type) || state.parsed.types.contains(type),
                        enabled = state.parsed.types.isEmpty(),
                        onClick = { viewModel.toggleType(type) },
                    )
                }
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Checkbox(
                    checked = state.includeCompleted,
                    onCheckedChange = viewModel::onIncludeCompletedChanged,
                    colors = CheckboxDefaults.colors(
                        checkedColor = AppColorPalette.Primary,
                        uncheckedColor = AppColorPalette.TextSecondary,
                        checkmarkColor = AppColorPalette.OnPrimary,
                    ),
                )
                Text(
                    text = "Include completed",
                    color = AppColorPalette.TextPrimarySoft,
                    fontSize = 13.sp,
                )
            }

            Text(
                text = searchPrefixHints.joinToString("  "),
                color = AppColorPalette.TextSecondary,
                fontSize = 11.sp,
                modifier = Modifier.padding(horizontal = 16.dp),
            )

            Spacer(modifier = Modifier.height(8.dp))

            Box(modifier = Modifier.weight(1f)) {
                SearchBody(
                    state = state,
                    onRecentClick = { recent ->
                        val destination = viewModel.applyRecent(recent)
                        if (destination != null) {
                            onDismiss()
                            onNavigate(destination)
                        }
                    },
                    onItemClick = { item ->
                        item.destination?.let { destination ->
                            viewModel.saveItem(item)
                            onDismiss()
                            onNavigate(destination)
                        }
                    },
                    onResultClick = { result ->
                        viewModel.saveResult(result)
                        onDismiss()
                        onNavigate(result.toGlobalSearchDestination())
                    },
                )
            }
        }
    }
}

@Composable
private fun SearchHeader(onDismiss: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .border(width = 0.5.dp, color = AppColorPalette.SubtleBorder)
            .padding(start = 18.dp, end = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Text(
            text = "Search",
            color = AppColorPalette.TextPrimary,
            fontSize = 18.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f),
        )
        IconButton(onClick = onDismiss) {
            Icon(
                imageVector = Icons.Outlined.Close,
                contentDescription = "Close search",
                tint = AppColorPalette.IconPrimary,
            )
        }
    }
}

@Composable
private fun SearchBody(
    state: GlobalSearchUiState,
    onRecentClick: (RecentSearchEntry) -> Unit,
    onItemClick: (GlobalSearchListItem) -> Unit,
    onResultClick: (SearchResult) -> Unit,
) {
    val showHint = state.userQuery.isNotBlank() && !state.hasSearchableQuery
    val showEmpty = state.hasSearchableQuery &&
            !state.isLoading &&
            state.hasSearched &&
            state.errorMessage == null &&
            state.results.isEmpty() &&
            state.pageAndActionItems.isEmpty()

    LazyColumn(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp)
            .fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        if (state.query.isBlank() && state.recent.isNotEmpty()) {
            item { SectionLabel("Recent") }
            items(state.recent, key = { "recent-${it.id}" }) { recent ->
                SearchRow(
                    title = recent.label,
                    subtitle = recent.kind.displayName(),
                    label = "Recent",
                    icon = Icons.Outlined.History,
                    onClick = { onRecentClick(recent) },
                )
            }
        }

        if (state.pageAndActionItems.isNotEmpty()) {
            item { SectionLabel("Pages and actions") }
            items(state.pageAndActionItems, key = { it.id }) { item ->
                SearchRow(
                    title = item.title,
                    subtitle = item.subtitle,
                    label = item.label,
                    icon = iconForItem(item),
                    onClick = { onItemClick(item) },
                )
            }
        }

        if (showHint) {
            item {
                CenterMessage("Type at least 2 characters to search.")
            }
        }

        if (state.errorMessage != null) {
            item {
                CenterMessage(state.errorMessage, color = AppColorPalette.Error)
            }
        }

        if (state.hasSearchableQuery && !state.parsed.pagesOnly && state.results.isNotEmpty()) {
            item { SectionLabel("Results") }
            items(state.results, key = { "${it.type.apiValue}-${it.id}" }) { result ->
                SearchRow(
                    title = result.title,
                    subtitle = result.subtitle ?: result.module,
                    label = result.type.displayName(),
                    icon = iconForType(result.type),
                    onClick = { onResultClick(result) },
                )
            }
        }

        if (state.isLoading) {
            item {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 18.dp),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically,
                ) {
                    CircularProgressIndicator(
                        color = AppColorPalette.Primary,
                        strokeWidth = 2.dp,
                        modifier = Modifier.size(22.dp),
                    )
                    Spacer(modifier = Modifier.width(10.dp))
                    Text("Searching...", color = AppColorPalette.TextSecondary)
                }
            }
        }

        if (showEmpty) {
            item {
                CenterMessage("No results for \"${state.userQuery}\".")
            }
        }
    }
}

@Composable
private fun SectionLabel(label: String) {
    Text(
        text = label,
        color = AppColorPalette.TextSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        modifier = Modifier.padding(top = 8.dp, bottom = 2.dp),
    )
}

@Composable
private fun SearchRow(
    title: String,
    subtitle: String?,
    label: String,
    icon: ImageVector,
    onClick: () -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColorPalette.SurfaceVariant)
            .border(0.5.dp, AppColorPalette.SubtleBorder, RoundedCornerShape(12.dp))
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 11.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Box(
            modifier = Modifier
                .size(34.dp)
                .background(AppColorPalette.SelectionOverlay, CircleShape),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppColorPalette.IconPrimary,
                modifier = Modifier.size(18.dp),
            )
        }

        Spacer(modifier = Modifier.width(12.dp))

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = title,
                color = AppColorPalette.TextPrimary,
                fontSize = 14.sp,
                fontWeight = FontWeight.SemiBold,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            subtitle?.takeIf { it.isNotBlank() }?.let {
                Text(
                    text = it,
                    color = AppColorPalette.TextSecondary,
                    fontSize = 12.sp,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )
            }
        }

        Spacer(modifier = Modifier.width(8.dp))

        Text(
            text = label,
            color = AppColorPalette.Primary,
            fontSize = 11.sp,
            fontWeight = FontWeight.Bold,
            maxLines = 1,
        )
    }
}

@Composable
private fun TypeChip(
    type: SearchResultType,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val borderColor = if (selected) AppColorPalette.Primary else AppColorPalette.SubtleBorder
    val textColor = if (selected) AppColorPalette.Primary else AppColorPalette.TextSecondary
    Row(
        modifier = Modifier
            .clip(RoundedCornerShape(999.dp))
            .background(if (selected) AppColorPalette.SelectionOverlay else Color.Transparent)
            .border(0.5.dp, borderColor, RoundedCornerShape(999.dp))
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Dot(color = textColor)
        Spacer(modifier = Modifier.width(6.dp))
        Text(
            text = type.displayName(),
            color = textColor,
            fontSize = 12.sp,
            fontWeight = FontWeight.SemiBold,
        )
    }
}

@Composable
private fun Dot(color: Color) {
    Canvas(modifier = Modifier.size(6.dp)) {
        drawCircle(color = color)
    }
}

@Composable
private fun CenterMessage(message: String, color: Color = AppColorPalette.TextSecondary) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 22.dp),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = message,
            color = color,
            fontSize = 13.sp,
        )
    }
}

private fun RecentSearchKind.displayName(): String =
    when (this) {
        RecentSearchKind.QUERY -> "Query"
        RecentSearchKind.RESULT -> "Result"
        RecentSearchKind.PAGE -> "Page"
        RecentSearchKind.ACTION -> "Action"
    }

private fun SearchResultType.displayName(): String =
    when (this) {
        SearchResultType.TASK -> "Task"
        SearchResultType.PROJECT -> "Project"
        SearchResultType.PRODUCT -> "Product"
        SearchResultType.ORDER -> "Order"
        SearchResultType.RETURN -> "Return"
        SearchResultType.USER -> "User"
    }

private fun iconForType(type: SearchResultType): ImageVector =
    when (type) {
        SearchResultType.TASK -> Icons.Outlined.CheckCircle
        SearchResultType.PROJECT -> Icons.Outlined.Folder
        SearchResultType.PRODUCT -> Icons.Outlined.ShoppingCart
        SearchResultType.ORDER -> Icons.Outlined.LocalShipping
        SearchResultType.RETURN -> Icons.Outlined.Replay
        SearchResultType.USER -> Icons.Outlined.Person
    }

private fun iconForItem(item: GlobalSearchListItem): ImageVector =
    when (item.destination) {
        is GlobalSearchDestination.CreateTask -> Icons.Outlined.CheckCircle
        is GlobalSearchDestination.CreateProject -> Icons.Outlined.Folder
        is GlobalSearchDestination.DirectMessage -> Icons.Outlined.Person
        is GlobalSearchDestination.ProductDetail -> Icons.Outlined.ShoppingCart
        is GlobalSearchDestination.OrderDetail -> Icons.Outlined.LocalShipping
        is GlobalSearchDestination.ProjectDetail -> Icons.Outlined.Folder
        is GlobalSearchDestination.ReturnDetail -> Icons.Outlined.Replay
        is GlobalSearchDestination.TaskDetail -> Icons.Outlined.CheckCircle
        is GlobalSearchDestination.StartTimer -> Icons.Outlined.History
        is GlobalSearchDestination.UpdateStatus -> Icons.Outlined.Person
        is GlobalSearchDestination.Section,
        null -> Icons.Outlined.Search
    }

package org.example.project.presentation.calls

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import kotlinx.coroutines.flow.distinctUntilChanged
import org.example.project.domain.calls.WorkspaceCall
import org.example.project.presentation.localization.LocalAppStrings
import org.example.project.presentation.theme.AppColorPalette
import org.example.project.presentation.theme.AppDimensions

@Composable
fun CallHistoryScreen(
    viewModel: CallHistoryViewModel,
    modifier: Modifier = Modifier,
) {
    val state by viewModel.state.collectAsState()
    val strings = LocalAppStrings.current
    val listState = rememberLazyListState()

    LaunchedEffect(listState, state.calls.size, state.lastPage) {
        snapshotFlow {
            val layoutInfo = listState.layoutInfo
            val lastVisible = layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            lastVisible to state.calls.size
        }.distinctUntilChanged().collect { (lastVisible, total) ->
            if (total > 0 && lastVisible >= total - 3) {
                viewModel.loadMore()
            }
        }
    }

    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColorPalette.Background)
            .padding(AppDimensions.ScreenPadding),
    ) {
        Text(
            text = strings.text("Call history"),
            color = AppColorPalette.TextPrimary,
            fontWeight = FontWeight.Bold,
        )

        Spacer(modifier = Modifier.height(16.dp))

        when {
            state.isLoading && state.calls.isEmpty() -> {
                Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator(color = AppColorPalette.Primary)
                }
            }

            state.error != null && state.calls.isEmpty() -> {
                Text(state.error!!, color = AppColorPalette.Error)
            }

            state.calls.isEmpty() -> {
                Text(strings.text("No calls found."), color = AppColorPalette.TextSecondary)
            }

            else -> {
                LazyColumn(
                    state = listState,
                    modifier = Modifier.fillMaxSize(),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    items(state.calls, key = { it.id }) { call ->
                        CallHistoryRow(call)
                    }
                    if (state.isLoadingMore) {
                        item {
                            Box(
                                Modifier.fillMaxWidth().padding(16.dp),
                                contentAlignment = Alignment.Center,
                            ) {
                                CircularProgressIndicator(color = AppColorPalette.Primary)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun CallHistoryRow(call: WorkspaceCall) {
    val strings = LocalAppStrings.current
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .background(AppColorPalette.Surface, RoundedCornerShape(12.dp))
            .padding(12.dp),
    ) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                call.caller.name,
                color = AppColorPalette.TextPrimary,
                fontWeight = FontWeight.SemiBold,
            )
            Text(
                call.status.name.lowercase(),
                color = AppColorPalette.TextSecondary,
                fontSize = 12.sp,
            )
        }
        Spacer(Modifier.height(4.dp))
        Text(
            buildString {
                append(if (call.isGroup) strings.text("Group") else strings.text("Direct"))
                append(" · ")
                append(if (call.isVideo) strings.text("Video") else strings.text("Audio"))
            },
            color = AppColorPalette.TextSecondary,
            fontSize = 12.sp,
        )
        if (call.endedAt != null || call.createdAt.isNotBlank()) {
            HorizontalDivider(
                color = AppColorPalette.Border,
                modifier = Modifier.padding(vertical = 6.dp),
            )
            Text(
                call.endedAt ?: call.createdAt,
                color = AppColorPalette.TextSecondary,
                fontSize = 11.sp,
            )
        }
    }
}

package features.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.data.auth.UserSession
import org.example.project.domain.task.Task
import org.example.project.presentation.tasks.ActiveTimerViewModel

@Composable
fun AppTopBar(
    title: String,
    user: UserSession,
    activeTimerViewModel: ActiveTimerViewModel,
    onMenuClick: () -> Unit,
    onOpenActiveTask: (Task) -> Unit = {},
) {
    val colors = MaterialTheme.colorScheme
    val statusBarTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val activeTimer by activeTimerViewModel.uiState.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(statusBarTopPadding + 58.dp)
            .background(colors.background)
            .border(width = 0.5.dp, color = colors.outline)
            .padding(top = statusBarTopPadding)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onMenuClick) {
            MenuGlyph(tint = colors.onSurfaceVariant)
        }

        Text(
            text = title,
            color = colors.onBackground,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )

        val activeTask = activeTimer.activeTask
        if (activeTask != null) {
            ActiveTimerChip(
                taskTitle = activeTask.title,
                elapsedSeconds = activeTimer.elapsedSeconds,
                onOpenTask = { onOpenActiveTask(activeTask) },
            )
            Spacer(modifier = Modifier.width(10.dp))
        }

        Box(
            modifier = Modifier
                .size(34.dp)
                .background(colors.surfaceVariant, CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user.name.take(2).uppercase(),
                color = colors.onSurfaceVariant,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun ActiveTimerChip(taskTitle: String, elapsedSeconds: Int, onOpenTask: () -> Unit) {
    val colors = MaterialTheme.colorScheme
    var expanded by remember { mutableStateOf(false) }

    Box {
        Row(
            modifier = Modifier
                .background(colors.primary.copy(alpha = 0.2f), RoundedCornerShape(999.dp))
                .clickable { expanded = true }
                .padding(horizontal = 10.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = formatChipElapsed(elapsedSeconds),
                color = colors.primary,
                fontSize = 13.sp,
                fontWeight = FontWeight.SemiBold,
            )
        }

        DropdownMenu(expanded = expanded, onDismissRequest = { expanded = false }) {
            Column(
                modifier = Modifier
                    .clickable {
                        expanded = false
                        onOpenTask()
                    }
                    .padding(horizontal = 16.dp, vertical = 8.dp),
            ) {
                Text(text = "Timer activ", color = colors.onSurfaceVariant, fontSize = 12.sp)
                Text(text = taskTitle, color = colors.onBackground, fontSize = 15.sp, fontWeight = FontWeight.SemiBold)
                Text(text = formatChipElapsed(elapsedSeconds), color = colors.primary, fontSize = 14.sp)
            }
        }
    }
}

private fun formatChipElapsed(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return "${hours.pad()}:${minutes.pad()}:${seconds.pad()}"
}

private fun Int.pad(): String = toString().padStart(2, '0')

@Composable
private fun MenuGlyph(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(24.dp)) {
        val strokeWidth = size.width * 0.08f
        listOf(0.3f, 0.5f, 0.7f).forEach { y ->
            drawLine(
                color = tint,
                start = Offset(size.width * 0.22f, size.height * y),
                end = Offset(size.width * 0.78f, size.height * y),
                strokeWidth = strokeWidth,
                cap = StrokeCap.Round
            )
        }
    }
}

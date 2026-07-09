package features.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
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
import org.example.project.presentation.theme.AppColorPalette

@Composable
fun AppTopBar(
    title: String,
    user: UserSession,
    activeTimerViewModel: ActiveTimerViewModel,
    onOpenActiveTask: (Task) -> Unit,
    onMenuClick: () -> Unit,
    onProfileClick: () -> Unit = {}
) {
    val statusBarTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()
    val activeTimerState by activeTimerViewModel.uiState.collectAsState()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(statusBarTopPadding + 58.dp)
            .background(AppColorPalette.OverlayBackground)
            .border(width = 0.5.dp, color = AppColorPalette.SubtleBorder)
            .padding(top = statusBarTopPadding)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onMenuClick) {
            MenuGlyph(tint = AppColorPalette.IconPrimaryTranslucent)
        }

        Text(
            text = title,
            color = AppColorPalette.TextPrimarySoft,
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )

        activeTimerState.activeTask?.let { task ->
            TimerChip(
                elapsedSeconds = activeTimerState.elapsedSeconds,
                onClick = {
                    onOpenActiveTask(task)
                }
            )

            Spacer(modifier = Modifier.width(10.dp))
        }

        Box(
            modifier = Modifier
                .size(34.dp)
                .background(AppColorPalette.SelectionOverlay, CircleShape)
                .clickable(onClick = onProfileClick),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user.name.take(2).uppercase(),
                color = AppColorPalette.IconPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
    }
}

@Composable
private fun TimerChip(
    elapsedSeconds: Int,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .background(
                color = AppColorPalette.Primary,
                shape = RoundedCornerShape(18.dp)
            )
            .clickable(onClick = onClick)
            .padding(horizontal = 10.dp, vertical = 7.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        TimerGlyph(tint = AppColorPalette.OnPrimary)

        Spacer(modifier = Modifier.width(6.dp))

        Text(
            text = formatElapsed(elapsedSeconds),
            color = AppColorPalette.OnPrimary,
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold
        )
    }
}

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

@Composable
private fun TimerGlyph(
    tint: Color,
    modifier: Modifier = Modifier
) {
    Canvas(modifier = modifier.size(14.dp)) {
        val strokeWidth = size.width * 0.1f

        drawCircle(
            color = tint,
            radius = size.width * 0.36f,
            center = Offset(size.width * 0.5f, size.height * 0.56f),
            style = androidx.compose.ui.graphics.drawscope.Stroke(width = strokeWidth)
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.5f, size.height * 0.56f),
            end = Offset(size.width * 0.5f, size.height * 0.36f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.5f, size.height * 0.56f),
            end = Offset(size.width * 0.64f, size.height * 0.62f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
        drawLine(
            color = tint,
            start = Offset(size.width * 0.38f, size.height * 0.12f),
            end = Offset(size.width * 0.62f, size.height * 0.12f),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round
        )
    }
}

private fun formatElapsed(totalSeconds: Int): String {
    val hours = totalSeconds / 3600
    val minutes = (totalSeconds % 3600) / 60
    val seconds = totalSeconds % 60
    return "${hours.pad()}:${minutes.pad()}:${seconds.pad()}"
}

private fun Int.pad(): String = toString().padStart(2, '0')

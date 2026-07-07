package features.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.data.auth.UserSession

@Composable
fun AppTopBar(
    title: String,
    user: UserSession,
    onMenuClick: () -> Unit
) {
    val statusBarTopPadding = WindowInsets.statusBars.asPaddingValues().calculateTopPadding()

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(statusBarTopPadding + 58.dp)
            .background(Color(0xF20B0B0B))
            .border(width = 0.5.dp, color = Color(0x1FE6E6E6))
            .padding(top = statusBarTopPadding)
            .padding(horizontal = 14.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        IconButton(onClick = onMenuClick) {
            MenuGlyph(tint = Color(0xBFE0E0E0))
        }

        Text(
            text = title,
            color = Color(0xFFEAEAEA),
            fontSize = 17.sp,
            fontWeight = FontWeight.SemiBold,
            modifier = Modifier.weight(1f)
        )

        Box(
            modifier = Modifier
                .size(34.dp)
                .background(Color(0x1AE6E6E6), CircleShape),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = user.name.take(2).uppercase(),
                color = Color(0xFFE0E0E0),
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold
            )
        }
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

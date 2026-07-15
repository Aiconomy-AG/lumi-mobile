package features.main

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@Composable
fun AppSectionIcon(
    section: AppSection,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 22.dp
) {
    Canvas(modifier = modifier.size(size)) {
        val stroke = Stroke(width = size.toPx() * 0.08f, cap = StrokeCap.Round)
        val thinStroke = Stroke(width = size.toPx() * 0.06f, cap = StrokeCap.Round)
        val w = this.size.width
        val h = this.size.height

        when (section) {
            AppSection.DASHBOARD -> {
                val cardSize = Size(w * 0.34f, h * 0.28f)
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.12f, h * 0.16f),
                    size = cardSize,
                    cornerRadius = CornerRadius(w * 0.07f),
                    style = stroke
                )
                drawRoundRect(
                    color = tint.copy(alpha = 0.72f),
                    topLeft = Offset(w * 0.54f, h * 0.16f),
                    size = cardSize,
                    cornerRadius = CornerRadius(w * 0.07f),
                    style = stroke
                )
                drawRoundRect(
                    color = tint.copy(alpha = 0.72f),
                    topLeft = Offset(w * 0.12f, h * 0.56f),
                    size = cardSize,
                    cornerRadius = CornerRadius(w * 0.07f),
                    style = stroke
                )
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.54f, h * 0.56f),
                    size = cardSize,
                    cornerRadius = CornerRadius(w * 0.07f),
                    style = stroke
                )
            }

            AppSection.TASKS -> {
                listOf(0.24f, 0.5f, 0.76f).forEach { y ->
                    drawCircle(
                        color = tint.copy(alpha = 0.76f),
                        radius = w * 0.035f,
                        center = Offset(w * 0.18f, h * y)
                    )
                    drawLine(
                        color = tint,
                        start = Offset(w * 0.3f, h * y),
                        end = Offset(w * 0.84f, h * y),
                        strokeWidth = thinStroke.width,
                        cap = StrokeCap.Round
                    )
                }
                drawLine(
                    color = tint,
                    start = Offset(w * 0.12f, h * 0.48f),
                    end = Offset(w * 0.18f, h * 0.56f),
                    strokeWidth = thinStroke.width,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = tint,
                    start = Offset(w * 0.18f, h * 0.56f),
                    end = Offset(w * 0.28f, h * 0.4f),
                    strokeWidth = thinStroke.width,
                    cap = StrokeCap.Round
                )
            }

            AppSection.PROJECTS -> {
                drawLine(
                    color = tint,
                    start = Offset(w * 0.16f, h * 0.28f),
                    end = Offset(w * 0.44f, h * 0.28f),
                    strokeWidth = thinStroke.width,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = tint,
                    start = Offset(w * 0.44f, h * 0.28f),
                    end = Offset(w * 0.52f, h * 0.38f),
                    strokeWidth = thinStroke.width,
                    cap = StrokeCap.Round
                )
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.16f, h * 0.28f),
                    size = Size(w * 0.68f, h * 0.44f),
                    cornerRadius = CornerRadius(w * 0.08f),
                    style = stroke
                )
                drawLine(
                    color = tint.copy(alpha = 0.6f),
                    start = Offset(w * 0.3f, h * 0.5f),
                    end = Offset(w * 0.7f, h * 0.5f),
                    strokeWidth = thinStroke.width,
                    cap = StrokeCap.Round
                )
            }

            AppSection.CHAT -> {
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.14f, h * 0.2f),
                    size = Size(w * 0.72f, h * 0.5f),
                    cornerRadius = CornerRadius(w * 0.16f),
                    style = stroke
                )
                drawLine(
                    color = tint,
                    start = Offset(w * 0.36f, h * 0.7f),
                    end = Offset(w * 0.28f, h * 0.84f),
                    strokeWidth = thinStroke.width,
                    cap = StrokeCap.Round
                )
                listOf(0.34f, 0.5f, 0.66f).forEach { x ->
                    drawCircle(
                        color = tint.copy(alpha = 0.68f),
                        radius = w * 0.035f,
                        center = Offset(w * x, h * 0.46f)
                    )
                }
            }

            AppSection.CALL_HISTORY -> {
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.22f, h * 0.18f),
                    size = Size(w * 0.56f, h * 0.64f),
                    cornerRadius = CornerRadius(w * 0.14f),
                    style = stroke,
                )
                drawArc(
                    color = tint,
                    startAngle = 220f,
                    sweepAngle = 100f,
                    useCenter = false,
                    topLeft = Offset(w * 0.3f, h * 0.52f),
                    size = Size(w * 0.4f, h * 0.22f),
                    style = thinStroke,
                )
                drawLine(
                    color = tint,
                    start = Offset(w * 0.58f, h * 0.68f),
                    end = Offset(w * 0.76f, h * 0.86f),
                    strokeWidth = thinStroke.width,
                    cap = StrokeCap.Round,
                )
            }

            AppSection.STOCK -> {
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.16f, h * 0.34f),
                    size = Size(w * 0.68f, h * 0.44f),
                    cornerRadius = CornerRadius(w * 0.08f),
                    style = stroke
                )
                drawLine(
                    color = tint.copy(alpha = 0.72f),
                    start = Offset(w * 0.34f, h * 0.34f),
                    end = Offset(w * 0.34f, h * 0.2f),
                    strokeWidth = thinStroke.width,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = tint.copy(alpha = 0.72f),
                    start = Offset(w * 0.66f, h * 0.34f),
                    end = Offset(w * 0.66f, h * 0.2f),
                    strokeWidth = thinStroke.width,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = tint.copy(alpha = 0.58f),
                    start = Offset(w * 0.3f, h * 0.54f),
                    end = Offset(w * 0.7f, h * 0.54f),
                    strokeWidth = thinStroke.width,
                    cap = StrokeCap.Round
                )
            }

            AppSection.ORDERS -> {
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.2f, h * 0.16f),
                    size = Size(w * 0.6f, h * 0.68f),
                    cornerRadius = CornerRadius(w * 0.08f),
                    style = stroke
                )
                listOf(0.34f, 0.5f, 0.66f).forEach { y ->
                    drawLine(
                        color = tint.copy(alpha = 0.68f),
                        start = Offset(w * 0.32f, h * y),
                        end = Offset(w * 0.68f, h * y),
                        strokeWidth = thinStroke.width,
                        cap = StrokeCap.Round
                    )
                }
            }

            AppSection.RETURNS -> {
                drawLine(
                    color = tint,
                    start = Offset(w * 0.62f, h * 0.24f),
                    end = Offset(w * 0.38f, h * 0.24f),
                    strokeWidth = thinStroke.width,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = tint,
                    start = Offset(w * 0.38f, h * 0.24f),
                    end = Offset(w * 0.38f, h * 0.34f),
                    strokeWidth = thinStroke.width,
                    cap = StrokeCap.Round
                )
                drawLine(
                    color = tint,
                    start = Offset(w * 0.38f, h * 0.34f),
                    end = Offset(w * 0.28f, h * 0.24f),
                    strokeWidth = thinStroke.width,
                    cap = StrokeCap.Round
                )
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.2f, h * 0.42f),
                    size = Size(w * 0.6f, h * 0.38f),
                    cornerRadius = CornerRadius(w * 0.08f),
                    style = stroke
                )
            }

            AppSection.ADMIN -> {
                drawCircle(
                    color = tint,
                    radius = w * 0.13f,
                    center = Offset(w * 0.5f, h * 0.3f),
                    style = stroke
                )
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.25f, h * 0.56f),
                    size = Size(w * 0.5f, h * 0.26f),
                    cornerRadius = CornerRadius(w * 0.15f),
                    style = stroke
                )
                drawCircle(
                    color = tint.copy(alpha = 0.56f),
                    radius = w * 0.08f,
                    center = Offset(w * 0.22f, h * 0.4f),
                    style = thinStroke
                )
                drawCircle(
                    color = tint.copy(alpha = 0.56f),
                    radius = w * 0.08f,
                    center = Offset(w * 0.78f, h * 0.4f),
                    style = thinStroke
                )
            }

            AppSection.AUDIT_LOGS -> {
                listOf(0.32f, 0.5f, 0.68f).forEach { y ->
                    drawLine(
                        color = tint,
                        start = Offset(w * 0.18f, h * y),
                        end = Offset(w * 0.82f, h * y),
                        strokeWidth = thinStroke.width,
                        cap = StrokeCap.Round
                    )
                }
                drawCircle(
                    color = tint.copy(alpha = 0.7f),
                    radius = w * 0.035f,
                    center = Offset(w * 0.12f, h * 0.32f)
                )
                drawCircle(
                    color = tint.copy(alpha = 0.7f),
                    radius = w * 0.035f,
                    center = Offset(w * 0.12f, h * 0.5f)
                )
                drawCircle(
                    color = tint.copy(alpha = 0.7f),
                    radius = w * 0.035f,
                    center = Offset(w * 0.12f, h * 0.68f)
                )
            }
        }
    }
}

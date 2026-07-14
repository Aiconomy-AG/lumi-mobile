package org.example.project.presentation.calls

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

enum class CallControlIcon {
    Mic,
    MicOff,
    Camera,
    CameraOff,
    EndCall,
    Decline,
    Answer,
}

@Composable
fun CallControlIcon(
    icon: CallControlIcon,
    tint: Color,
    modifier: Modifier = Modifier,
    size: Dp = 24.dp,
) {
    Canvas(modifier = modifier.size(size)) {
        val w = this.size.width
        val h = this.size.height
        val stroke = Stroke(width = w * 0.08f, cap = StrokeCap.Round)

        when (icon) {
            CallControlIcon.Mic -> {
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.35f, h * 0.12f),
                    size = androidx.compose.ui.geometry.Size(w * 0.3f, h * 0.42f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.15f),
                )
                drawArc(
                    color = tint,
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(w * 0.2f, h * 0.42f),
                    size = androidx.compose.ui.geometry.Size(w * 0.6f, h * 0.35f),
                    style = stroke,
                )
                drawLine(
                    color = tint,
                    start = Offset(w * 0.5f, h * 0.72f),
                    end = Offset(w * 0.5f, h * 0.9f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round,
                )
            }

            CallControlIcon.MicOff -> {
                drawRoundRect(
                    color = tint.copy(alpha = 0.5f),
                    topLeft = Offset(w * 0.35f, h * 0.12f),
                    size = androidx.compose.ui.geometry.Size(w * 0.3f, h * 0.42f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.15f),
                )
                drawLine(
                    color = tint,
                    start = Offset(w * 0.2f, h * 0.2f),
                    end = Offset(w * 0.8f, h * 0.8f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round,
                )
            }

            CallControlIcon.Camera -> {
                drawRoundRect(
                    color = tint,
                    topLeft = Offset(w * 0.08f, h * 0.28f),
                    size = androidx.compose.ui.geometry.Size(w * 0.84f, h * 0.5f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.08f),
                    style = stroke,
                )
                drawCircle(color = tint, radius = w * 0.16f, center = Offset(w * 0.5f, h * 0.53f))
            }

            CallControlIcon.CameraOff -> {
                drawRoundRect(
                    color = tint.copy(alpha = 0.5f),
                    topLeft = Offset(w * 0.08f, h * 0.28f),
                    size = androidx.compose.ui.geometry.Size(w * 0.84f, h * 0.5f),
                    cornerRadius = androidx.compose.ui.geometry.CornerRadius(w * 0.08f),
                    style = stroke,
                )
                drawLine(
                    color = tint,
                    start = Offset(w * 0.18f, h * 0.22f),
                    end = Offset(w * 0.82f, h * 0.78f),
                    strokeWidth = stroke.width,
                    cap = StrokeCap.Round,
                )
            }

            CallControlIcon.EndCall, CallControlIcon.Decline -> {
                drawLine(
                    color = tint,
                    start = Offset(w * 0.28f, h * 0.35f),
                    end = Offset(w * 0.72f, h * 0.65f),
                    strokeWidth = stroke.width * 1.2f,
                    cap = StrokeCap.Round,
                )
                drawArc(
                    color = tint,
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(w * 0.15f, h * 0.2f),
                    size = androidx.compose.ui.geometry.Size(w * 0.7f, h * 0.55f),
                    style = Stroke(width = stroke.width * 1.2f, cap = StrokeCap.Round),
                )
            }

            CallControlIcon.Answer -> {
                drawArc(
                    color = tint,
                    startAngle = 200f,
                    sweepAngle = 140f,
                    useCenter = false,
                    topLeft = Offset(w * 0.15f, h * 0.2f),
                    size = androidx.compose.ui.geometry.Size(w * 0.7f, h * 0.55f),
                    style = Stroke(width = stroke.width * 1.2f, cap = StrokeCap.Round),
                )
            }
        }
    }
}

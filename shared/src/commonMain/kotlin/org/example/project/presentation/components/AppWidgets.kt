package org.example.project.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import org.example.project.presentation.localization.LocalAppStrings
import org.example.project.presentation.theme.AppColorPalette
import org.example.project.presentation.theme.AppComponentDefaults
import org.example.project.presentation.theme.AppDimensions

private val SearchFieldShape = RoundedCornerShape(AppDimensions.SearchFieldRadius)
private val TextFieldShape = RoundedCornerShape(AppDimensions.SearchFieldRadius)
private val ButtonShape = RoundedCornerShape(50)

// ─── Search Field ────────────────────────────────────────────────────────────

@Composable
fun AppSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                color = AppColorPalette.TextSecondary,
            )
        },
        modifier = modifier.fillMaxWidth(),
        singleLine = true,
        enabled = enabled,
        shape = SearchFieldShape,
        colors = AppComponentDefaults.appSearchFieldColors(),
        leadingIcon = { SearchIcon() },
    )
}

@Composable
private fun SearchIcon() {
    Canvas(modifier = Modifier.size(18.dp)) {
        val strokeWidth = 1.8.dp.toPx()
        val circleRadius = 5.6.dp.toPx()
        val cx = 6.5.dp.toPx()
        val cy = 6.5.dp.toPx()
        drawCircle(
            color = AppColorPalette.TextSecondary,
            radius = circleRadius,
            center = Offset(cx, cy),
            style = Stroke(width = strokeWidth),
        )
        drawLine(
            color = AppColorPalette.TextSecondary,
            start = Offset(cx + circleRadius * 0.7f, cy + circleRadius * 0.7f),
            end = Offset(15.dp.toPx(), 15.dp.toPx()),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

// ─── Back Button ─────────────────────────────────────────────────────────────

@Composable
fun AppBackButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(44.dp)
            .clip(CircleShape)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
    ) {
        BackChevronIcon(color = AppColorPalette.TextPrimary)
    }
}

@Composable
private fun BackChevronIcon(color: Color) {
    Canvas(modifier = Modifier.size(20.dp)) {
        val strokeWidth = 2.dp.toPx()
        val centerY = size.height / 2f
        val tipX = size.width * 0.32f
        val tailX = size.width * 0.68f
        val armY = size.height * 0.28f

        drawLine(
            color = color,
            start = Offset(tailX, centerY - armY),
            end = Offset(tipX, centerY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
        drawLine(
            color = color,
            start = Offset(tipX, centerY),
            end = Offset(tailX, centerY + armY),
            strokeWidth = strokeWidth,
            cap = StrokeCap.Round,
        )
    }
}

// ─── Text Field ──────────────────────────────────────────────────────────────

@Composable
fun AppTextField(
    value: String,
    onValueChange: (String) -> Unit,
    placeholder: String,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    singleLine: Boolean = true,
    visualTransformation: VisualTransformation = VisualTransformation.None,
) {
    OutlinedTextField(
        value = value,
        onValueChange = onValueChange,
        placeholder = {
            Text(
                text = placeholder,
                color = AppColorPalette.TextSecondary,
            )
        },
        modifier = modifier.fillMaxWidth(),
        singleLine = singleLine,
        enabled = enabled,
        shape = TextFieldShape,
        visualTransformation = visualTransformation,
        colors = AppComponentDefaults.appSearchFieldColors(),
    )
}

// ─── Buttons ─────────────────────────────────────────────────────────────────

@Composable
fun AppButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    content: @Composable RowScope.() -> Unit,
) {
    Button(
        onClick = onClick,
        modifier = modifier,
        enabled = enabled,
        shape = ButtonShape,
        colors = AppComponentDefaults.primaryButtonColors(),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 12.dp),
        content = content,
    )
}

@Composable
fun AppOutlinedButton(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    borderColor: Color = AppColorPalette.Border,
    content: @Composable RowScope.() -> Unit,
) {
    Box(
        modifier = modifier
            .clip(ButtonShape)
            .border(width = 1.dp, color = borderColor, shape = ButtonShape)
            .clickable(
                enabled = enabled,
                interactionSource = remember { MutableInteractionSource() },
                indication = null,
                onClick = onClick,
            )
            .padding(horizontal = 24.dp, vertical = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
            content = content,
        )
    }
}

// ─── Pagination ───────────────────────────────────────────────────────────────

@Composable
fun AppPaginationBar(
    currentPage: Int,
    totalPages: Int,
    onPreviousClick: () -> Unit,
    onNextClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    val strings = LocalAppStrings.current

    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        PaginationArrow(
            symbol = "‹",
            enabled = currentPage > 0,
            onClick = onPreviousClick,
        )

        Text(
            text = strings.format(
                "Page {page} of {total}",
                "page" to (currentPage + 1).toString(),
                "total" to totalPages.toString(),
            ),
            color = AppColorPalette.TextSecondary,
            fontWeight = FontWeight.Medium,
            modifier = Modifier.padding(horizontal = 20.dp),
        )

        PaginationArrow(
            symbol = "›",
            enabled = currentPage < totalPages - 1,
            onClick = onNextClick,
        )
    }
}

@Composable
private fun PaginationArrow(
    symbol: String,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val bgColor = if (enabled) AppColorPalette.SurfaceVariant else AppColorPalette.Border
    val textColor = if (enabled) AppColorPalette.TextPrimary else AppColorPalette.TextSecondary

    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(bgColor)
            .then(
                if (enabled) {
                    Modifier.clickable(
                        interactionSource = remember { MutableInteractionSource() },
                        indication = null,
                        onClick = onClick,
                    )
                } else {
                    Modifier
                },
            ),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = symbol,
            color = textColor,
            fontWeight = FontWeight.Bold,
            style = MaterialTheme.typography.titleLarge,
        )
    }
}

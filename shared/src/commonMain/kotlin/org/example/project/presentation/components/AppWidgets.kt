package org.example.project.presentation.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import org.example.project.presentation.localization.LocalAppStrings
import org.example.project.presentation.theme.AppColorPalette
import org.example.project.presentation.theme.AppComponentDefaults
import org.example.project.presentation.theme.AppDimensions
import org.example.project.presentation.theme.AppTextStyles
import org.example.project.presentation.theme.StatusColor

private val SearchFieldShape = RoundedCornerShape(AppDimensions.SearchFieldRadius)
private val TextFieldShape = RoundedCornerShape(AppDimensions.SearchFieldRadius)
private val ButtonShape = RoundedCornerShape(50)


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

@Composable
fun AppStatusBadge(
    label: String,
    statusColor: StatusColor,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .background(
                color = statusColor.background,
                shape = MaterialTheme.shapes.extraSmall,
            )
            .padding(horizontal = 10.dp, vertical = 4.dp),
    ) {
        Text(
            text = label,
            color = statusColor.content,
            style = MaterialTheme.typography.labelMedium,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun <T> AppListContainer(
    items: List<T>,
    emptyMessage: String,
    modifier: Modifier = Modifier,
    key: ((T) -> Any)? = null,
    rowContent: @Composable (T) -> Unit,
) {
    Box(
        modifier = modifier
            .fillMaxWidth()
            .border(
                width = 1.dp,
                color = AppColorPalette.Border,
                shape = RoundedCornerShape(AppDimensions.TableCornerRadius),
            )
            .background(
                color = AppColorPalette.Surface,
                shape = RoundedCornerShape(AppDimensions.TableCornerRadius),
            ),
    ) {
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(12.dp),
        ) {
            if (items.isEmpty()) {
                item {
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                    ) {
                        Text(
                            text = emptyMessage,
                            color = AppColorPalette.TextSecondary,
                        )
                    }
                }
            } else {
                itemsIndexed(
                    items = items,
                    key = key?.let { stableKey -> { _, item -> stableKey(item) } },
                ) { index, item ->
                    rowContent(item)
                    if (index != items.lastIndex) {
                        HorizontalDivider(color = AppColorPalette.Border)
                    }
                }
            }
        }
    }
}

@Composable
fun AppListRow(
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(vertical = 12.dp),
        content = content,
    )
}

@Composable
fun AppDetailOverlay(
    title: String,
    onBackClick: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    showHeader: Boolean = true,
    trailingContent: @Composable (() -> Unit)? = null,
    bottomContent: @Composable (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .background(AppColorPalette.Background)
            .padding(AppDimensions.ScreenPadding),
    ) {
        if (showHeader) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                onBackClick?.let { AppBackButton(onClick = it) }

                Text(
                    text = title,
                    modifier = Modifier.weight(1f),
                    color = AppColorPalette.TextPrimary,
                    style = AppTextStyles.PageTitle,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                )

                trailingContent?.invoke()
            }

            Spacer(modifier = Modifier.height(AppDimensions.SectionSpacing))
        }

        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
                .verticalScroll(rememberScrollState()),
            content = content,
        )

        bottomContent?.let {
            Spacer(modifier = Modifier.height(AppDimensions.SmallSpacing))
            it()
        }
    }
}

data class AppDetailField(
    val label: String,
    val value: String?,
    val valueFontWeight: FontWeight? = null,
    val valueAlpha: Float = 1f,
)

@Composable
fun AppDetailGrid(
    fields: List<AppDetailField>,
    modifier: Modifier = Modifier,
) {
    BoxWithConstraints(modifier = modifier.fillMaxWidth()) {
        val columns = if (maxWidth >= 560.dp) 2 else 1
        Column(verticalArrangement = Arrangement.spacedBy(AppDimensions.TinySpacing)) {
            fields.chunked(columns).forEach { rowFields ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(AppDimensions.TinySpacing),
                ) {
                    rowFields.forEach { field ->
                        AppDetailFieldView(
                            field = field,
                            modifier = Modifier.weight(1f),
                        )
                    }
                    repeat(columns - rowFields.size) {
                        Spacer(modifier = Modifier.weight(1f))
                    }
                }
            }
        }
    }
}

@Composable
fun AppDetailGridRows(
    rows: List<List<AppDetailField>>,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(AppDimensions.TinySpacing),
    ) {
        rows.forEach { fields ->
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(AppDimensions.TinySpacing),
            ) {
                fields.forEach { field ->
                    AppDetailFieldView(
                        field = field,
                        modifier = Modifier.weight(1f),
                    )
                }
            }
        }
    }
}

@Composable
private fun AppDetailFieldView(
    field: AppDetailField,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .background(
                color = AppColorPalette.SurfaceVariant,
                shape = RoundedCornerShape(AppDimensions.ControlCornerRadius),
            )
            .padding(AppDimensions.SmallSpacing),
    ) {
        Text(
            text = field.label,
            color = AppColorPalette.TextSecondary,
            style = AppTextStyles.Emphasis,
            softWrap = true,
        )

        Spacer(modifier = Modifier.height(4.dp))

        Text(
            text = field.value?.takeIf { it.isNotBlank() } ?: "—",
            color = AppColorPalette.TextPrimary.copy(alpha = field.valueAlpha),
            fontWeight = field.valueFontWeight,
            softWrap = true,
        )
    }
}

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

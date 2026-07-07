package org.example.project.presentation.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ButtonColors
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.RadioButtonColors
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Shapes
import androidx.compose.material3.TextFieldColors
import androidx.compose.material3.darkColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

private val AppDarkColorScheme = darkColorScheme(
    primary = AppColorPalette.Primary,
    onPrimary = AppColorPalette.OnPrimary,
    background = AppColorPalette.Background,
    onBackground = AppColorPalette.TextPrimary,
    surface = AppColorPalette.Surface,
    onSurface = AppColorPalette.TextPrimary,
    surfaceVariant = AppColorPalette.SurfaceVariant,
    onSurfaceVariant = AppColorPalette.TextSecondary,
    outline = AppColorPalette.Border,
    outlineVariant = AppColorPalette.Border,
    error = AppColorPalette.Error,
)

private val AppShapes = Shapes(
    extraSmall = RoundedCornerShape(6.dp),
    small = RoundedCornerShape(8.dp),
    medium = RoundedCornerShape(12.dp),
    large = RoundedCornerShape(18.dp),
)

object AppTextStyles {
    val PageTitle = TextStyle(
        fontSize = 24.sp,
        fontWeight = FontWeight.Bold,
    )

    val Emphasis = TextStyle(
        fontWeight = FontWeight.SemiBold,
    )

    val TableHeader = TextStyle(
        fontWeight = FontWeight.Bold,
    )
}

object AppDimensions {
    val ScreenPadding = 16.dp
    val SectionSpacing = 16.dp
    val LargeSpacing = 20.dp
    val SmallSpacing = 12.dp
    val TinySpacing = 8.dp
    val TableMaxHeight = 390.dp
    val TableCornerRadius = 16.dp
    val ControlCornerRadius = 12.dp
    val ScrollBarHeight = 6.dp
    val ActionButtonSize = 36.dp
    val ActionIconSize = 18.dp
}

object AppComponentDefaults {
    @Composable
    fun primaryButtonColors(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = AppColorPalette.Primary,
        contentColor = AppColorPalette.OnPrimary,
    )

    @Composable
    fun loginButtonColors(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = AppColorPalette.Primary,
        contentColor = AppColorPalette.OnPrimary,
        disabledContainerColor = AppColorPalette.Primary.copy(alpha = 0.5f),
        disabledContentColor = AppColorPalette.OnPrimary.copy(alpha = 0.6f),
    )

    @Composable
    fun paginationButtonColors(): ButtonColors = ButtonDefaults.buttonColors(
        containerColor = AppColorPalette.Primary,
        contentColor = AppColorPalette.OnPrimary,
        disabledContainerColor = AppColorPalette.Border,
        disabledContentColor = AppColorPalette.TextSecondary,
    )

    @Composable
    fun appTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = AppColorPalette.TextPrimary,
        unfocusedTextColor = AppColorPalette.TextPrimary,
        cursorColor = AppColorPalette.Primary,
        focusedBorderColor = AppColorPalette.Primary,
        unfocusedBorderColor = AppColorPalette.Border,
        focusedLabelColor = AppColorPalette.Primary,
        unfocusedLabelColor = AppColorPalette.TextSecondary,
    )

    @Composable
    fun loginTextFieldColors(): TextFieldColors = OutlinedTextFieldDefaults.colors(
        focusedTextColor = AppColorPalette.TextPrimary,
        unfocusedTextColor = AppColorPalette.TextPrimary,
        focusedBorderColor = AppColorPalette.Primary,
        unfocusedBorderColor = AppColorPalette.BorderStrong,
        focusedContainerColor = AppColorPalette.FieldBackground,
        unfocusedContainerColor = AppColorPalette.FieldBackground,
        cursorColor = AppColorPalette.Primary,
    )

    @Composable
    fun appRadioButtonColors(): RadioButtonColors = RadioButtonDefaults.colors(
        selectedColor = AppColorPalette.Primary,
        unselectedColor = AppColorPalette.TextSecondary,
    )
}

@Composable
fun AppTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = AppDarkColorScheme,
        shapes = AppShapes,
        content = content,
    )
}

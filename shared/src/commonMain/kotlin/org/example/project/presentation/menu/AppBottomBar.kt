package features.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.presentation.localization.LocalAppStrings
import org.example.project.presentation.theme.AppColorPalette

@Composable
fun AppBottomBar(
    sections: List<AppSection>,
    selectedSection: AppSection,
    onSectionSelected: (AppSection) -> Unit
) {
    val strings = LocalAppStrings.current

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp)
            .background(AppColorPalette.OverlaySurface)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        sections.forEach { section ->
            val isSelected = section == selectedSection
            val color = if (isSelected) AppColorPalette.IconPrimary else AppColorPalette.IconSecondaryTranslucent

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 2.dp)
                    .background(
                        color = if (isSelected) AppColorPalette.SelectionOverlay else Color.Transparent,
                        shape = RoundedCornerShape(16.dp)
                    )
                    .clickable { onSectionSelected(section) }
                    .padding(vertical = 8.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                AppSectionIcon(section = section, tint = color)

                Spacer(modifier = Modifier.height(4.dp))

                Text(
                    text = strings.text(section.title),
                    fontSize = 12.sp,
                    color = color,
                    fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                )
            }
        }
    }
}

package features.main

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.data.auth.UserSession
import org.example.project.presentation.localization.LocalAppStrings
import org.example.project.presentation.theme.AppColorPalette

@Composable
fun AppDrawer(
    user: UserSession,
    sections: List<AppSection>,
    selectedSection: AppSection,
    onSectionSelected: (AppSection) -> Unit,
) {
    val strings = LocalAppStrings.current
    val workspaceSections = sections.filter { it.drawerGroup == DrawerGroup.WORKSPACE }
    val salesSections = sections.filter { it.drawerGroup == DrawerGroup.SALES }

    ModalDrawerSheet(
        drawerContainerColor = AppColorPalette.Surface,
        modifier = Modifier
            .width(260.dp)
            .border(
                width = 1.dp,
                color = AppColorPalette.Border,
            ),
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(AppColorPalette.SelectionOverlay, CircleShape),
                    contentAlignment = Alignment.Center,
                ) {
                    Text(
                        text = "L",
                        color = AppColorPalette.IconPrimary,
                        fontWeight = FontWeight.Bold,
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Lumi",
                    color = AppColorPalette.TextPrimarySoft,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            DrawerGroupSection(
                title = strings.text("Workspace"),
                sections = workspaceSections,
                selectedSection = selectedSection,
                onSectionSelected = onSectionSelected,
            )

            Spacer(modifier = Modifier.height(20.dp))

            DrawerGroupSection(
                title = strings.text("Sales"),
                sections = salesSections,
                selectedSection = selectedSection,
                onSectionSelected = onSectionSelected,
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = strings.format("Logged in as {name}", "name" to user.name),
                color = AppColorPalette.IconSecondaryTranslucent,
                fontSize = 13.sp,
            )

            Spacer(modifier = Modifier.height(12.dp))
        }
    }
}

@Composable
private fun DrawerGroupSection(
    title: String,
    sections: List<AppSection>,
    selectedSection: AppSection,
    onSectionSelected: (AppSection) -> Unit,
) {
    val strings = LocalAppStrings.current

    Text(
        text = title.uppercase(),
        color = AppColorPalette.TextSecondary,
        fontSize = 12.sp,
        fontWeight = FontWeight.SemiBold,
        letterSpacing = 1.sp,
    )

    Spacer(modifier = Modifier.height(10.dp))

    sections.forEach { section ->
        val isSelected = section == selectedSection
        val color = if (isSelected) AppColorPalette.IconPrimary else AppColorPalette.IconSecondaryTranslucent

        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = if (isSelected) AppColorPalette.SelectionOverlay else Color.Transparent,
                    shape = RoundedCornerShape(14.dp),
                )
                .clickable { onSectionSelected(section) }
                .padding(horizontal = 12.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            AppSectionIcon(section = section, tint = color, size = 20.dp)

            Spacer(modifier = Modifier.width(12.dp))

            Text(
                text = strings.text(section.title),
                color = color,
                fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal,
                fontSize = 14.sp,
            )
        }

        Spacer(modifier = Modifier.height(4.dp))
    }
}

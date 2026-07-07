package features.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
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
import org.example.project.presentation.theme.AppColorPalette

@Composable
fun AppDrawer(
    user: UserSession,
    sections: List<AppSection>,
    selectedSection: AppSection,
    onSectionSelected: (AppSection) -> Unit,
    onLogout: () -> Unit
) {
    ModalDrawerSheet(
        drawerContainerColor = AppColorPalette.OverlaySurface,
        modifier = Modifier.width(260.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(36.dp)
                        .background(AppColorPalette.SelectionOverlay, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "L",
                        color = AppColorPalette.IconPrimary,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Lumi",
                    color = AppColorPalette.TextPrimarySoft,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            sections.forEach { section ->
                val isSelected = section == selectedSection
                val color = if (isSelected) AppColorPalette.IconPrimary else AppColorPalette.IconSecondaryTranslucent

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (isSelected) AppColorPalette.SelectionOverlay else Color.Transparent,
                            shape = RoundedCornerShape(14.dp)
                        )
                        .clickable { onSectionSelected(section) }
                        .padding(horizontal = 14.dp, vertical = 14.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    AppSectionIcon(section = section, tint = color, size = 20.dp)

                    Spacer(modifier = Modifier.width(12.dp))

                    Text(
                        text = section.title,
                        color = color,
                        fontWeight = if (isSelected) FontWeight.SemiBold else FontWeight.Normal
                    )
                }

                Spacer(modifier = Modifier.height(6.dp))
            }

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "Logged in as ${user.name}",
                color = AppColorPalette.IconSecondaryTranslucent,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Logout",
                color = AppColorPalette.Error,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { onLogout() }
                    .padding(vertical = 12.dp)
            )
        }
    }
}

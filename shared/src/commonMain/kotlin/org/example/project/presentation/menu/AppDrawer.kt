package features.main

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
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

@Composable
fun AppDrawer(
    user: UserSession,
    sections: List<AppSection>,
    selectedSection: AppSection,
    onSectionSelected: (AppSection) -> Unit,
    onLogout: () -> Unit
) {
    val colors = MaterialTheme.colorScheme

    ModalDrawerSheet(
        drawerContainerColor = colors.background,
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
                        .background(colors.surfaceVariant, CircleShape),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = "L",
                        color = colors.onSurfaceVariant,
                        fontWeight = FontWeight.Bold
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                Text(
                    text = "Lumi",
                    color = colors.onBackground,
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            sections.forEach { section ->
                val isSelected = section == selectedSection
                val color = if (isSelected) colors.onBackground else colors.onSurfaceVariant

                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .background(
                            color = if (isSelected) colors.surfaceVariant else Color.Transparent,
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
                color = colors.onSurfaceVariant,
                fontSize = 13.sp
            )

            Spacer(modifier = Modifier.height(12.dp))

            Text(
                text = "Logout",
                color = colors.error,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .clickable { onLogout() }
                    .padding(vertical = 12.dp)
            )
        }
    }
}

package org.example.project.presentation.calls

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.presentation.localization.LocalAppStrings
import org.example.project.presentation.theme.AppColorPalette

data class CallInviteCandidate(
    val userId: Int,
    val name: String,
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CallInviteSheet(
    candidates: List<CallInviteCandidate>,
    onInvite: (List<Int>) -> Unit,
    onDismiss: () -> Unit,
) {
    val strings = LocalAppStrings.current
    val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = AppColorPalette.Surface,
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 20.dp, vertical = 12.dp),
        ) {
            Text(
                text = strings.text("Invite"),
                color = AppColorPalette.TextPrimary,
                fontSize = 20.sp,
                fontWeight = FontWeight.Bold,
            )
            Spacer(Modifier.height(4.dp))
            Text(
                text = strings.text("Add participant"),
                color = AppColorPalette.TextSecondary,
                fontSize = 13.sp,
            )
            Spacer(Modifier.height(16.dp))
            if (candidates.isEmpty()) {
                Text(
                    text = strings.text("No participants available to invite."),
                    color = AppColorPalette.TextSecondary,
                    fontSize = 14.sp,
                )
            } else {
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(candidates, key = { it.userId }) { candidate ->
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clip(RoundedCornerShape(12.dp))
                                .background(AppColorPalette.SurfaceVariant)
                                .clickable { onInvite(listOf(candidate.userId)) }
                                .padding(horizontal = 14.dp, vertical = 12.dp),
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.SpaceBetween,
                        ) {
                            Text(
                                text = candidate.name.ifBlank { "User ${candidate.userId}" },
                                color = AppColorPalette.TextPrimary,
                                fontSize = 15.sp,
                            )
                            TextButton(onClick = { onInvite(listOf(candidate.userId)) }) {
                                Text(strings.text("Invite"))
                            }
                        }
                    }
                }
            }
            Spacer(Modifier.height(24.dp))
        }
    }
}

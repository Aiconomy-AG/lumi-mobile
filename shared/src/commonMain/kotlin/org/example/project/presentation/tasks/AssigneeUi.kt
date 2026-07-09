package org.example.project.presentation.tasks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.data.accounts.User
import org.example.project.presentation.theme.AppColorPalette

fun User.avatarColor(): Color =
    AppColorPalette.AvatarPalette[id % AppColorPalette.AvatarPalette.size]

@Composable
fun UserAvatar(
    user: User,
    size: Dp = 28.dp,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier = modifier
            .size(size)
            .background(color = user.avatarColor(), shape = CircleShape),
        contentAlignment = Alignment.Center,
    ) {
        Text(
            text = user.initials,
            color = Color.White,
            fontSize = (size.value * 0.4f).sp,
            fontWeight = FontWeight.Bold,
        )
    }
}

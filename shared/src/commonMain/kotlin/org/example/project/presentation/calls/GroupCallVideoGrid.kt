package org.example.project.presentation.calls

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MicOff
import androidx.compose.material.icons.filled.VideocamOff
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import org.example.project.presentation.theme.AppColorPalette

@Composable
fun GroupCallVideoGrid(
    tiles: List<CallTileParticipant>,
    showVideo: Boolean,
    modifier: Modifier = Modifier,
) {
    if (tiles.isEmpty()) return

    if (!showVideo) {
        AvatarOnlyGrid(tiles = tiles, modifier = modifier)
        return
    }

    if (tiles.count { !it.isLocal } == 1) {
        val remote = tiles.first { !it.isLocal }
        val local = tiles.firstOrNull { it.isLocal }
        Box(modifier = modifier.fillMaxSize()) {
            CallParticipantTile(
                tile = remote,
                showVideo = true,
                modifier = Modifier.fillMaxSize(),
            )
            local?.let {
                CallParticipantTile(
                    tile = it,
                    showVideo = true,
                    modifier = Modifier
                        .align(Alignment.TopEnd)
                        .padding(16.dp)
                        .fillMaxWidth(0.28f)
                        .aspectRatio(0.75f)
                        .clip(RoundedCornerShape(16.dp)),
                )
            }
        }
        return
    }

    val columns = groupCallGridColumns(tiles.size)
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier.fillMaxSize(),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        items(tiles, key = { it.identity }) { tile ->
            CallParticipantTile(
                tile = tile,
                showVideo = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(if (columns == 1) 1.2f else 0.85f)
                    .clip(RoundedCornerShape(12.dp)),
            )
        }
    }
}

@Composable
private fun AvatarOnlyGrid(
    tiles: List<CallTileParticipant>,
    modifier: Modifier,
) {
    val columns = groupCallGridColumns(tiles.size)
    LazyVerticalGrid(
        columns = GridCells.Fixed(columns),
        modifier = modifier.fillMaxSize().padding(16.dp),
        horizontalArrangement = Arrangement.spacedBy(12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp),
    ) {
        items(tiles, key = { it.identity }) { tile ->
            CallParticipantTile(
                tile = tile,
                showVideo = false,
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .clip(RoundedCornerShape(16.dp)),
            )
        }
    }
}

@Composable
fun CallParticipantTile(
    tile: CallTileParticipant,
    showVideo: Boolean,
    modifier: Modifier = Modifier,
) {
    Box(modifier = modifier) {
        if (showVideo) {
            CallVideoRenderer(
                isLocal = tile.isLocal,
                modifier = Modifier.fillMaxSize(),
                participantName = tile.name,
                cameraEnabled = tile.cameraEnabled,
                participantIdentity = tile.identity,
            )
        } else {
            CallVideoRenderer(
                isLocal = tile.isLocal,
                modifier = Modifier.fillMaxSize(),
                participantName = tile.name,
                cameraEnabled = false,
                participantIdentity = tile.identity,
            )
        }

        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .fillMaxWidth()
                .background(AppColorPalette.Background.copy(alpha = 0.55f))
                .padding(horizontal = 8.dp, vertical = 6.dp),
        ) {
            Text(
                text = tile.name,
                color = AppColorPalette.TextPrimary,
                fontSize = 12.sp,
                fontWeight = FontWeight.Medium,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
        }

        RowBadges(
            muted = tile.isMuted,
            cameraOff = showVideo && !tile.cameraEnabled,
            modifier = Modifier
                .align(Alignment.TopEnd)
                .padding(6.dp),
        )
    }
}

@Composable
private fun RowBadges(
    muted: Boolean,
    cameraOff: Boolean,
    modifier: Modifier = Modifier,
) {
    androidx.compose.foundation.layout.Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(4.dp),
    ) {
        if (muted) {
            BadgeIcon(Icons.Filled.MicOff)
        }
        if (cameraOff) {
            BadgeIcon(Icons.Filled.VideocamOff)
        }
    }
}

@Composable
private fun BadgeIcon(imageVector: androidx.compose.ui.graphics.vector.ImageVector) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(8.dp))
            .background(AppColorPalette.Background.copy(alpha = 0.7f))
            .padding(4.dp),
    ) {
        Icon(
            imageVector = imageVector,
            contentDescription = null,
            tint = AppColorPalette.TextPrimary,
            modifier = Modifier.padding(0.dp),
        )
    }
}

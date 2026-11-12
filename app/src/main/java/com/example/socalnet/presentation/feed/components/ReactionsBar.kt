package com.example.socalnet.presentation.feed.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.socalnet.core.model.ReactionSummary
import com.example.socalnet.core.model.ReactionType

private val REACTION_EMOJI = mapOf(
    ReactionType.LIKE to "👍",
    ReactionType.LOVE to "❤️",
    ReactionType.FIRE to "🔥",
    ReactionType.WOW to "😮",
    ReactionType.SAD to "😢",
)

@Composable
fun ReactionBar(
    summary: ReactionSummary,
    myReaction: ReactionType?,
    enabled: Boolean,
    onReactionClick: (ReactionType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(4.dp),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        REACTION_EMOJI.forEach { (type, emoji) ->
            val count = summary.byType[type] ?: 0
            val isMine = myReaction == type
            ReactionChip(
                emoji = emoji,
                count = count,
                selected = isMine,
                enabled = enabled,
                onClick = { onReactionClick(type) },
            )
        }

        if (summary.total > 0) {
            Spacer(Modifier.weight(1f))
            Text(
                text = "${summary.total}",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}

@Composable
private fun ReactionChip(
    emoji: String,
    count: Int,
    selected: Boolean,
    enabled: Boolean,
    onClick: () -> Unit,
) {
    val bg = if (selected) {
        MaterialTheme.colorScheme.primary.copy(alpha = 0.15f)
    } else if (count > 0) {
        MaterialTheme.colorScheme.surfaceVariant
    } else {
        androidx.compose.ui.graphics.Color.Transparent
    }

    Row(
        modifier = Modifier
            .clip(CircleShape)
            .background(bg)
            .clickable(enabled = enabled, onClick = onClick)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(3.dp),
    ) {
        Text(text = emoji, fontSize = 16.sp)
        if (count > 0) {
            Text(
                text = "$count",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = if (selected) FontWeight.SemiBold else FontWeight.Normal,
                color = if (selected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
    }
}
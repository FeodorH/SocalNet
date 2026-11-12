package com.example.socalnet.presentation.feed.components

import androidx.compose.foundation.layout.*
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.socalnet.core.model.Post
import com.example.socalnet.core.model.ReactionType
import com.example.socalnet.presentation.common.UserAvatar
import com.example.socalnet.presentation.common.formatRelativeTime

@Composable
fun PostCard(
    post: Post,
    enabled: Boolean,                    // false для Guest
    onReactionClick: (ReactionType) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(8.dp),
        ) {
            UserAvatar(name = post.author.name, avatarUrl = post.author.avatarUrl)
            Column {
                Text(
                    text = post.author.name,
                    style = MaterialTheme.typography.titleSmall,
                )
                Text(
                    text = formatRelativeTime(post.createdAt),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
            }
        }

        if (post.text.isNotBlank()) {
            Text(
                text = post.text,
                style = MaterialTheme.typography.bodyMedium,
            )
        }

        PostImages(images = post.images)

        ReactionBar(
            summary = post.reactions,
            myReaction = post.myReaction,
            enabled = enabled,
            onReactionClick = onReactionClick,
        )
    }
    HorizontalDivider(thickness = 0.5.dp)
}
package com.example.socalnet.presentation.profile.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.example.socalnet.core.model.User
import com.example.socalnet.presentation.common.UserAvatar

@Composable
fun ProfileHeader(
    user: User,
    postsCount: Int,
    isUploadingAvatar: Boolean,
    onAvatarClick: () -> Unit,
    onEditProfileClick: () -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier
            .fillMaxWidth()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Box {
            Box(
                modifier = Modifier
                    .clip(CircleShape)
                    .clickable(enabled = !isUploadingAvatar, onClick = onAvatarClick),
            ) {
                UserAvatar(
                    name = user.name,
                    avatarUrl = user.avatarUrl,
                    size = 96.dp,
                )
            }
            if (isUploadingAvatar) {
                CircularProgressIndicator(
                    modifier = Modifier
                        .align(Alignment.Center)
                        .size(32.dp),
                    strokeWidth = 2.dp,
                )
            }
        }

        Spacer(Modifier.height(12.dp))

        Text(
            text = user.name,
            style = MaterialTheme.typography.headlineSmall,
        )

        if (!user.bio.isNullOrBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                text = user.bio,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        Spacer(Modifier.height(8.dp))

        Text(
            text = "Постов: $postsCount",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        Spacer(Modifier.height(12.dp))

        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedButton(onClick = onEditProfileClick) {
                Icon(Icons.Default.Edit, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.width(4.dp))
                Text("Изменить")
            }
            TextButton(onClick = onLogoutClick) {
                Text("Выйти", color = MaterialTheme.colorScheme.error)
            }
        }
    }
}
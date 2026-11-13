package com.example.socalnet.presentation.profile.components

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.socalnet.core.model.Post
import com.example.socalnet.core.model.ReactionType
import com.example.socalnet.presentation.feed.components.PostCard

@Composable
fun OwnPostCard(
    post: Post,
    onReactionClick: (ReactionType) -> Unit,
    onEditClick: () -> Unit,
    onDeleteClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var menuExpanded by remember { mutableStateOf(false) }

    Box(modifier.fillMaxWidth()) {
        PostCard(
            post = post,
            enabled = true,
            onReactionClick = onReactionClick,
        )

        // Меню «три точки» в правом верхнем углу
        Box(
            modifier = Modifier.align(Alignment.TopEnd),
        ) {
            IconButton(onClick = { menuExpanded = true }) {
                Icon(Icons.Default.MoreVert, contentDescription = "Действия")
            }
            DropdownMenu(
                expanded = menuExpanded,
                onDismissRequest = { menuExpanded = false },
            ) {
                DropdownMenuItem(
                    text = { Text("Редактировать") },
                    onClick = {
                        menuExpanded = false
                        onEditClick()
                    },
                )
                DropdownMenuItem(
                    text = { Text("Удалить", color = MaterialTheme.colorScheme.error) },
                    onClick = {
                        menuExpanded = false
                        onDeleteClick()
                    },
                )
            }
        }
    }
}
package com.example.socalnet.presentation.profile

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.PickVisualMediaRequest
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.socalnet.core.common.UiState
import com.example.socalnet.core.model.Post
import com.example.socalnet.core.model.ReactionType
import com.example.socalnet.presentation.profile.components.EditProfileDialog
import com.example.socalnet.presentation.profile.components.OwnPostCard
import com.example.socalnet.presentation.profile.components.ProfileHeader
import com.example.socalnet.presentation.profile.model.ProfileUiState

@Composable
fun ProfileScreen(
    state: UiState<ProfileUiState>,
    role: Boolean,                 // false = Guest → показываем GuestProfilePrompt
    onLoginClick: () -> Unit,
    onRegisterClick: () -> Unit,
    onRefresh: () -> Unit,
    onAvatarPicked: (String) -> Unit,
    onRemoveAvatar: () -> Unit,
    onUpdateNameBio: (String, String) -> Unit,
    onEditPostClick: (postId: String) -> Unit,
    onDeletePostClick: (postId: String) -> Unit,
    onReactionClick: (postId: String, type: ReactionType) -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    if (!role) {
        GuestProfilePrompt(
            onLogin = onLoginClick,
            onRegister = onRegisterClick,
            modifier = modifier,
        )
        return
    }

    when (state) {
        UiState.Loading -> FullScreenLoading(modifier)
        UiState.Empty -> FullScreenMessage(
            message = "Профиль недоступен",
            action = "Повторить" to onRefresh,
            modifier = modifier,
        )
        is UiState.Error -> FullScreenMessage(
            message = state.message,
            action = if (state.retryable) "Повторить" to onRefresh else null,
            modifier = modifier,
        )
        is UiState.Content -> ContentState(
            state = state.data,
            onRefresh = onRefresh,
            onAvatarPicked = onAvatarPicked,
            onRemoveAvatar = onRemoveAvatar,
            onUpdateNameBio = onUpdateNameBio,
            onEditPostClick = onEditPostClick,
            onDeletePostClick = onDeletePostClick,
            onReactionClick = onReactionClick,
            onLogoutClick = onLogoutClick,
            modifier = modifier,
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ContentState(
    state: ProfileUiState,
    onRefresh: () -> Unit,
    onAvatarPicked: (String) -> Unit,
    onRemoveAvatar: () -> Unit,
    onUpdateNameBio: (String, String) -> Unit,
    onEditPostClick: (String) -> Unit,
    onDeletePostClick: (String) -> Unit,
    onReactionClick: (String, ReactionType) -> Unit,
    onLogoutClick: () -> Unit,
    modifier: Modifier = Modifier,
) {
    var showEditDialog by remember { mutableStateOf(false) }
    var showRemoveAvatarDialog by remember { mutableStateOf(false) }
    var postToDelete by remember { mutableStateOf<Post?>(null) }

    val avatarPicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.PickVisualMedia()
    ) { uri ->
        uri?.let { onAvatarPicked(it.toString()) }
    }

    PullToRefreshBox(
        isRefreshing = state.isRefreshingPosts,
        onRefresh = onRefresh,
        modifier = modifier.fillMaxSize(),
    ) {
        LazyColumn(Modifier.fillMaxSize()) {

            // Header профиля
            item {
                ProfileHeader(
                    user = state.user,
                    postsCount = state.posts.size,
                    isUploadingAvatar = state.isUploadingAvatar,
                    onAvatarClick = { showRemoveAvatarDialog = true },
                    onEditProfileClick = { showEditDialog = true },
                    onLogoutClick = onLogoutClick,
                )
                HorizontalDivider()
            }

            // Посты
            if (state.posts.isEmpty()) {
                item {
                    Box(
                        Modifier.fillMaxWidth().padding(32.dp),
                        contentAlignment = Alignment.Center,
                    ) {
                        if (state.isRefreshingPosts) {
                            CircularProgressIndicator()
                        } else {
                            Text(
                                "У вас пока нет постов",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                            )
                        }
                    }
                }
            } else {
                items(state.posts, key = { it.id }) { post ->
                    OwnPostCard(
                        post = post,
                        onReactionClick = { type -> onReactionClick(post.id, type) },
                        onEditClick = { onEditPostClick(post.id) },
                        onDeleteClick = { postToDelete = post },
                    )
                }
            }
        }
    }

    // Диалог редактирования профиля
    if (showEditDialog) {
        EditProfileDialog(
            user = state.user,
            isSaving = state.isUpdatingProfile,
            onDismiss = { showEditDialog = false },
            onSave = { name, bio ->
                onUpdateNameBio(name, bio)
                showEditDialog = false
            },
        )
    }

    // Диалог смены/удаления аватара
    if (showRemoveAvatarDialog) {
        AlertDialog(
            onDismissRequest = { showRemoveAvatarDialog = false },
            title = { Text("Аватар") },
            text = { Text("Выберите действие") },
            confirmButton = {
                TextButton(onClick = {
                    showRemoveAvatarDialog = false
                    avatarPicker.launch(
                        PickVisualMediaRequest(ActivityResultContracts.PickVisualMedia.ImageOnly)
                    )
                }) { Text("Загрузить новый") }
            },
            dismissButton = {
                if (state.user.avatarUrl != null) {
                    TextButton(onClick = {
                        showRemoveAvatarDialog = false
                        onRemoveAvatar()
                    }) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
                } else {
                    TextButton(onClick = { showRemoveAvatarDialog = false }) { Text("Отмена") }
                }
            },
        )
    }

    // Подтверждение удаления поста
    postToDelete?.let { post ->
        AlertDialog(
            onDismissRequest = { postToDelete = null },
            title = { Text("Удалить пост?") },
            text = { Text("Это действие нельзя отменить") },
            confirmButton = {
                TextButton(onClick = {
                    onDeletePostClick(post.id)
                    postToDelete = null
                }) { Text("Удалить", color = MaterialTheme.colorScheme.error) }
            },
            dismissButton = {
                TextButton(onClick = { postToDelete = null }) { Text("Отмена") }
            },
        )
    }
}

@Composable
private fun GuestProfilePrompt(
    onLogin: () -> Unit,
    onRegister: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text("Профиль", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))
        Text(
            "Войдите, чтобы увидеть свой профиль, посты и подписки",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = TextAlign.Center,
        )
        Spacer(Modifier.height(24.dp))
        Button(onClick = onLogin, modifier = Modifier.fillMaxWidth()) {
            Text("Войти")
        }
        TextButton(onClick = onRegister) { Text("Зарегистрироваться") }
    }
}

@Composable
private fun FullScreenLoading(modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}

@Composable
private fun FullScreenMessage(
    message: String,
    action: Pair<String, () -> Unit>?,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier = modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        Text(message, style = MaterialTheme.typography.titleMedium, textAlign = TextAlign.Center)
        if (action != null) {
            Spacer(Modifier.height(12.dp))
            Button(onClick = action.second) { Text(action.first) }
        }
    }
}

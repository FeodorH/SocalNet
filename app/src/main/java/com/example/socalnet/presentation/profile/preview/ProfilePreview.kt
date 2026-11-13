package com.example.socalnet.presentation.profile.preview

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.socalnet.core.common.UiState
import com.example.socalnet.core.model.ImageRef
import com.example.socalnet.core.model.Post
import com.example.socalnet.core.model.PostAuthor
import com.example.socalnet.core.model.ReactionSummary
import com.example.socalnet.core.model.ReactionType
import com.example.socalnet.core.model.User
import com.example.socalnet.presentation.profile.ProfileScreen
import com.example.socalnet.presentation.profile.model.ProfileUiState
import kotlin.time.Clock
import kotlin.time.Duration.Companion.days
import kotlin.time.Duration.Companion.hours

// --- Inline fake data ---
private val now = Clock.System.now()

private val fakeUser = User(
    id = "1",
    email = "alice@test.com",
    name = "Alice Wonder",
    avatarUrl = null,
    bio = "Разработчик Android · любитель кофе",
    createdAt = now - 30.days,
)

// Посты в профиле — все от одного автора (самого пользователя).
// Это отличается от ленты, где авторы разные.
private val authorOfUser = PostAuthor(
    id = fakeUser.id,
    name = fakeUser.name,
    avatarUrl = fakeUser.avatarUrl,
)

private val userPosts = listOf(
    Post(
        id = "up1",
        author = authorOfUser,
        text = "Мой первый пост. Привет всем 👋",
        images = emptyList(),
        createdAt = now - 1.hours,
        reactions = ReactionSummary(
            total = 3,
            byType = mapOf(ReactionType.LIKE to 3),
        ),
        myReaction = null,
    ),
    Post(
        id = "up2",
        author = authorOfUser,
        text = "Фото с прогулки",
        images = listOf(ImageRef.Remote("https://example.com/p1.jpg", null)),
        createdAt = now - 2.days,
        reactions = ReactionSummary(
            total = 15,
            byType = mapOf(ReactionType.LOVE to 10, ReactionType.FIRE to 5),
        ),
        myReaction = null,
    ),
)

private val profileContentState = UiState.Content(
    ProfileUiState(
        user = fakeUser,
        posts = userPosts,
    )
)

// --- Previews ---

@Preview(showBackground = true, heightDp = 1000, name = "Profile · с постами")
@Composable
private fun ProfileWithPostsPreview() {
    MaterialTheme {
        ProfileScreen(
            state = profileContentState,
            role = true,
            onLoginClick = {}, onRegisterClick = {},
            onRefresh = {}, onAvatarPicked = {}, onRemoveAvatar = {},
            onUpdateNameBio = { _, _ -> },
            onEditPostClick = {}, onDeletePostClick = {},
            onReactionClick = { _, _ -> }, onLogoutClick = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 800, name = "Profile · без постов")
@Composable
private fun ProfileEmptyPostsPreview() {
    MaterialTheme {
        ProfileScreen(
            state = UiState.Content(
                ProfileUiState(user = fakeUser, posts = emptyList())
            ),
            role = true,
            onLoginClick = {}, onRegisterClick = {},
            onRefresh = {}, onAvatarPicked = {}, onRemoveAvatar = {},
            onUpdateNameBio = { _, _ -> },
            onEditPostClick = {}, onDeletePostClick = {},
            onReactionClick = { _, _ -> }, onLogoutClick = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 600, name = "Profile · Guest")
@Composable
private fun ProfileGuestPreview() {
    MaterialTheme {
        ProfileScreen(
            state = UiState.Loading,
            role = false,
            onLoginClick = {}, onRegisterClick = {},
            onRefresh = {}, onAvatarPicked = {}, onRemoveAvatar = {},
            onUpdateNameBio = { _, _ -> },
            onEditPostClick = {}, onDeletePostClick = {},
            onReactionClick = { _, _ -> }, onLogoutClick = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 600, name = "Profile · Loading")
@Composable
private fun ProfileLoadingPreview() {
    MaterialTheme {
        ProfileScreen(
            state = UiState.Loading,
            role = true,
            onLoginClick = {}, onRegisterClick = {},
            onRefresh = {}, onAvatarPicked = {}, onRemoveAvatar = {},
            onUpdateNameBio = { _, _ -> },
            onEditPostClick = {}, onDeletePostClick = {},
            onReactionClick = { _, _ -> }, onLogoutClick = {},
        )
    }
}
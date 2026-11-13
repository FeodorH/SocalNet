package com.example.socalnet.presentation.profile.model

import com.example.socalnet.core.model.Post
import com.example.socalnet.core.model.User

data class ProfileUiState(
    val user: User,
    val posts: List<Post> = emptyList(),

    val isRefreshingPosts: Boolean = false,  // загрузка постов постов
    val isUpdatingProfile: Boolean = false,  // сохранение name/bio
    val isUploadingAvatar: Boolean = false,  // загрузка аватара
)
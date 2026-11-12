package com.example.socalnet.domain.repository.remote

import com.example.socalnet.core.common.AppResult
import com.example.socalnet.core.model.User
import kotlinx.coroutines.flow.Flow

interface UserRepository {
    fun observeUser(userId: String): Flow<User?>

    suspend fun getUser(userId: String): AppResult<User>

    suspend fun updateProfile(
        userId: String,
        name: String? = null,
        bio: String? = null,
        avatarUrl: String? = null,
        avatarPublicId: String? = null,
        removeAvatar: Boolean = false
    ): AppResult<User>
}
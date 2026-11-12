package com.example.socalnet.domain.repository.common

import com.example.socalnet.core.common.AppResult
import com.example.socalnet.core.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Оффлайн-фёрст обёртка над профилями пользователей.
 *
 * Стратегия:
 * - observeUser: реактивно из Room, мгновенно, работает без сети.
 * - getUser: сначала Room, если пусто — сеть + запись в Room.
 * - updateProfile: сеть → при успехе запись в Room + обновление автора в постах.
 * - syncUser: принудительное обновление кэша из Firestore.
 */
interface OfflineFirstUserRepository {

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

    suspend fun syncUser(userId: String): AppResult<User>
}
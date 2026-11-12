package com.example.socalnet.domain.repository.remote

import com.example.socalnet.core.common.AppResult
import com.example.socalnet.core.model.User
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    /** null — пользователь не авторизован. */
    val currentUserId: Flow<String?>

    suspend fun register(email: String, password: String, name: String): AppResult<User>
    suspend fun login(email: String, password: String): AppResult<User>
    suspend fun logout(): AppResult<Unit>
    suspend fun sendPasswordReset(email: String): AppResult<Unit>
}
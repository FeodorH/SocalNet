package com.example.socalnet.domain.repository.common

import com.example.socalnet.core.common.AppResult
import com.example.socalnet.core.model.User
import kotlinx.coroutines.flow.Flow

/**
 * Оффлайн-фёрст обёртка над аутентификацией.
 *
 * Стратегия:
 * - register/login: сначала Firebase, затем сохранение профиля в Room.
 * - observeCurrentUser: реактивно из Room — работает без сети.
 * - syncCurrentUser: фоновая синхронизация профиля с Firestore.
 * - logout: очистка локальных данных текущего пользователя.
 *
 * Не заменяет AuthRepository, а дополняет его.
 */
interface OfflineFirstAuthRepository {

    /** ID текущего пользователя из Firebase Auth (работает из кэша Firebase даже без сети). */
    val currentUserId: Flow<String?>

    /** Профиль текущего пользователя из Room. Мгновенно, без сети. */
    fun observeCurrentUser(): Flow<User?>

    /** Синхронно получить кэшированный профиль (или null). */
    suspend fun getCachedCurrentUser(): User?

    /** Регистрация: Firebase Auth → сохранение профиля в Room. */
    suspend fun register(email: String, password: String, name: String): AppResult<User>

    /** Логин: Firebase Auth → сохранение профиля в Room. */
    suspend fun login(email: String, password: String): AppResult<User>

    /** Logout: очистка локальных данных текущего пользователя. */
    suspend fun logout(): AppResult<Unit>

    /**
     * Фоновая синхронизация профиля из Firestore в Room.
     * При ошибке сети кэш не трогается — данные остаются валидными.
     */
    suspend fun syncCurrentUser(): AppResult<User>

    /** Сброс пароля - через сеть */
    suspend fun sendPasswordReset(email: String): AppResult<Unit>
}
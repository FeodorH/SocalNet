package com.example.socalnet.data.repository.common

import com.example.socalnet.core.common.AppError
import com.example.socalnet.core.common.AppResult
import com.example.socalnet.core.model.User
import com.example.socalnet.core.utils.AppDispatchers
import com.example.socalnet.data.local.dao.SubscriptionDao
import com.example.socalnet.data.local.dao.UserDao
import com.example.socalnet.data.mapper.toDomain
import com.example.socalnet.data.mapper.toEntity
import com.example.socalnet.domain.repository.remote.AuthRepository
import com.example.socalnet.domain.repository.remote.UserRepository
import com.example.socalnet.domain.repository.common.OfflineFirstAuthRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class OfflineFirstAuthRepositoryImpl @Inject constructor(
    private val firebaseAuthRepository: AuthRepository,
    private val firebaseUserRepository: UserRepository,
    private val userDao: UserDao,
    private val subscriptionDao: SubscriptionDao,
    private val dispatchers: AppDispatchers,
) : OfflineFirstAuthRepository {

    override val currentUserId: Flow<String?> =
        firebaseAuthRepository.currentUserId

    @OptIn(ExperimentalCoroutinesApi::class)
    override fun observeCurrentUser(): Flow<User?> =
        firebaseAuthRepository.currentUserId.flatMapLatest { userId ->
            if (userId == null) {
                flowOf(null)
            } else {
                userDao.observeUser(userId).map { it?.toDomain() }
            }
        }

    override suspend fun getCachedCurrentUser(): User? = withContext(dispatchers.io) {
        val userId = firebaseAuthRepository.currentUserId.first() ?: return@withContext null
        userDao.getUser(userId)?.toDomain()
    }

    override suspend fun register(
        email: String,
        password: String,
        name: String
    ): AppResult<User> = withContext(dispatchers.io) {
        when (val result = firebaseAuthRepository.register(email, password, name)) {
            is AppResult.Success -> {
                userDao.upsertUser(result.data.toEntity())
                result
            }
            is AppResult.Failure -> result
        }
    }

    override suspend fun login(
        email: String,
        password: String
    ): AppResult<User> = withContext(dispatchers.io) {
        when (val result = firebaseAuthRepository.login(email, password)) {
            is AppResult.Success -> {
                userDao.upsertUser(result.data.toEntity())
                result
            }
            is AppResult.Failure -> result
        }
    }

    override suspend fun logout(): AppResult<Unit> = withContext(dispatchers.io) {
        val userId = firebaseAuthRepository.currentUserId.first()

        when (val result = firebaseAuthRepository.logout()) {
            is AppResult.Success -> {
                if (userId != null) {
                    subscriptionDao.clearForUser(userId)
                    userDao.deleteUser(userId)
                }
                result
            }
            is AppResult.Failure -> result
        }
    }

    override suspend fun syncCurrentUser(): AppResult<User> = withContext(dispatchers.io) {
        val userId = firebaseAuthRepository.currentUserId.first()
            ?: return@withContext AppResult.Failure(AppError.Unauthorized)

        when (val result = firebaseUserRepository.getUser(userId)) {
            is AppResult.Success -> {
                userDao.upsertUser(result.data.toEntity())
                result
            }
            is AppResult.Failure -> result
        }
    }

    override suspend fun sendPasswordReset(email: String): AppResult<Unit> =
        firebaseAuthRepository.sendPasswordReset(email)
}
package com.example.socalnet.data.repository.common

import com.example.socalnet.core.common.AppResult
import com.example.socalnet.core.utils.AppDispatchers
import com.example.socalnet.data.local.dao.SubscriptionDao
import com.example.socalnet.data.local.entity.SubscriptionEntity
import com.example.socalnet.domain.repository.common.OfflineFirstSubscriptionRepository
import com.example.socalnet.domain.repository.remote.SubscriptionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import javax.inject.Inject

class OfflineFirstSubscriptionRepositoryImpl @Inject constructor(
    private val remoteSubscriptionRepository: SubscriptionRepository,
    private val subscriptionDao: SubscriptionDao,
    private val dispatchers: AppDispatchers,
) : OfflineFirstSubscriptionRepository {

    override fun observeIsSubscribed(userId: String, authorId: String): Flow<Boolean> =
        subscriptionDao.observeIsSubscribed(userId, authorId)

    override suspend fun getSubscriptionIds(userId: String): AppResult<List<String>> =
        withContext(dispatchers.io) {
            // Если в Room пусто, возможно это первый вход.
            // Но ViewModel должен вызывать syncSubscriptions при старте.
            // Поэтому просто отдаем то, что есть в Room.
            AppResult.Success(subscriptionDao.getSubscriptionIds(userId))
        }

    override suspend fun subscribe(userId: String, authorId: String): AppResult<Unit> =
        withContext(dispatchers.io) {
            when (val result = remoteSubscriptionRepository.subscribe(userId, authorId)) {
                is AppResult.Success -> {
                    subscriptionDao.upsert(
                        SubscriptionEntity(
                            userId = userId,
                            authorId = authorId,
                            createdAtEpochMillis = System.currentTimeMillis()
                        )
                    )
                    result
                }
                is AppResult.Failure -> result
            }
        }

    override suspend fun unsubscribe(userId: String, authorId: String): AppResult<Unit> =
        withContext(dispatchers.io) {
            when (val result = remoteSubscriptionRepository.unsubscribe(userId, authorId)) {
                is AppResult.Success -> {
                    subscriptionDao.delete(userId, authorId)
                    result
                }
                is AppResult.Failure -> result
            }
        }

    override suspend fun syncSubscriptions(userId: String): AppResult<Unit> =
        withContext(dispatchers.io) {
            when (val result = remoteSubscriptionRepository.getSubscriptionIds(userId)) {
                is AppResult.Success -> {
                    val remoteIds = result.data.toSet()

                    // Очищаем старые подписки в Room, которых больше нет на сервере
                    subscriptionDao.clearForUser(userId)

                    val now = System.currentTimeMillis()
                    val entities = remoteIds.map { authorId ->
                        SubscriptionEntity(userId, authorId, createdAtEpochMillis = now)
                    }
                    entities.forEach { subscriptionDao.upsert(it) }

                    AppResult.Success(Unit)
                }
                is AppResult.Failure -> result
            }
        }
}
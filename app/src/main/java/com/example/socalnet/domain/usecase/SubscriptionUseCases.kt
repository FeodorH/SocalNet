package com.example.socalnet.domain.usecase

import com.example.socalnet.core.common.AppError
import com.example.socalnet.core.common.AppResult
import com.example.socalnet.domain.repository.common.OfflineFirstAuthRepository
import com.example.socalnet.domain.repository.common.OfflineFirstSubscriptionRepository
import jakarta.inject.Inject
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf

class ToggleSubscriptionUseCase @Inject constructor(
    private val auth: OfflineFirstAuthRepository,
    private val subscriptionRepository: OfflineFirstSubscriptionRepository
) {
    suspend operator fun invoke(
        toId: String,
        isSubscribed: Boolean
    ): AppResult<Unit> {
        val fromId = auth.currentUserId.first()
            ?: return AppResult.Failure(AppError.Unauthorized)

        if (fromId == toId) {
            return AppResult.Failure(
                AppError.Validation(
                    AppError.Validation.Field.NAME,
                    "Нельзя подписаться на самого себя"
                )
            )
        }

        return if (isSubscribed) {
            subscriptionRepository.unsubscribe(fromId, toId)
        } else {
            subscriptionRepository.subscribe(fromId, toId)
        }
    }
}

/**
 * Подписан ли текущий пользователь на автора.
 * Используется на экране чужого профиля (кнопка «Подписаться/Отписаться»).
 */
class ObserveIsSubscribedUseCase @Inject constructor(
    private val auth: OfflineFirstAuthRepository,
    private val subscriptions: OfflineFirstSubscriptionRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(authorId: String): Flow<Boolean> =
        auth.currentUserId.flatMapLatest { userId ->
            if (userId == null) flowOf(false)
            else subscriptions.observeIsSubscribed(userId, authorId)
        }
}

/**
 * Синхронизация подписок (pull-to-refresh на списке подписок).
 */
class SyncSubscriptionsUseCase @Inject constructor(
    private val auth: OfflineFirstAuthRepository,
    private val subscriptions: OfflineFirstSubscriptionRepository
) {
    suspend operator fun invoke(): AppResult<Unit> {
        val userId = auth.currentUserId.first()
            ?: return AppResult.Failure(
                com.example.socalnet.core.common.AppError.Unauthorized
            )
        return subscriptions.syncSubscriptions(userId)
    }
}
package com.example.socalnet.domain.usecase

import com.example.socalnet.core.common.AppError
import com.example.socalnet.core.common.AppResult
import com.example.socalnet.domain.repository.common.OfflineFirstAuthRepository
import com.example.socalnet.domain.repository.common.OfflineFirstSubscriptionRepository
import jakarta.inject.Inject
import kotlinx.coroutines.flow.first

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
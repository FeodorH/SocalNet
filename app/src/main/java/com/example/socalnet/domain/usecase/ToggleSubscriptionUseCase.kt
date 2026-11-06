package com.example.socalnet.domain.usecase

import com.example.socalnet.core.common.AppError
import com.example.socalnet.core.common.AppResult
import com.example.socalnet.domain.repository.AuthRepository
import com.example.socalnet.domain.repository.SubscriptionRepository
import kotlinx.coroutines.flow.first

class ToggleSubscriptionUseCase(
    private val auth: AuthRepository,
    private val subscriptionRepository: SubscriptionRepository
) {
    suspend operator fun invoke(toId: String, isSubscribed: Boolean): AppResult<Unit> {
        val fromId = auth.currentUserId.first() ?: return AppResult.Failure(AppError.Unauthorized)
        if (fromId == toId) return AppResult.Failure(
            AppError.Validation(
                AppError.Validation.Field.NAME,
                "Can't subscribed to yourself!"
            )
        )

        return if (isSubscribed) subscriptionRepository.unsubscribe(fromId, toId)
        else subscriptionRepository.subscribe(fromId, toId)
    }
}
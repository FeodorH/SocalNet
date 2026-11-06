package com.example.socalnet.domain.usecase

import com.example.socalnet.core.common.AppError
import com.example.socalnet.core.common.AppResult
import com.example.socalnet.core.model.ReactionSummary
import com.example.socalnet.core.model.ReactionType
import com.example.socalnet.domain.repository.AuthRepository
import com.example.socalnet.domain.repository.ReactionRepository
import kotlinx.coroutines.flow.first

class ToggleReactionUseCase(
    private val auth: AuthRepository,
    private val reactionRepository: ReactionRepository
) {
    suspend operator fun invoke(postId: String, type: ReactionType, current: ReactionType?): AppResult<ReactionSummary>{
        val userId = auth.currentUserId.first() ?: return AppResult.Failure(AppError.Unauthorized)
        return if(current == type) reactionRepository.removeReaction(userId,postId)
        else reactionRepository.setReaction(userId,postId,type)
    }
}
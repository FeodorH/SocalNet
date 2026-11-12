package com.example.socalnet.domain.repository.common

import com.example.socalnet.core.common.AppResult
import com.example.socalnet.core.model.ReactionSummary
import com.example.socalnet.core.model.ReactionType
import kotlinx.coroutines.flow.Flow

interface OfflineFirstReactionRepository {

    /** Реакция текущего пользователя (из Room) */
    fun observeMyReaction(postId: String): Flow<ReactionType?>

    /** Общий счетчик реакций на посте (из Room) */
    fun observeSummary(postId: String): Flow<ReactionSummary>

    /** Поставить/изменить реакцию: сеть (транзакция) -> обновление Room */
    suspend fun setReaction(userId: String, postId: String, type: ReactionType): AppResult<ReactionSummary>

    /** Удалить реакцию: сеть (транзакция) -> обновление Room */
    suspend fun removeReaction(userId: String, postId: String): AppResult<ReactionSummary>
}
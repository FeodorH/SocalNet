package com.example.socalnet.domain.repository

import com.example.socalnet.core.common.AppResult
import com.example.socalnet.core.model.ReactionSummary
import com.example.socalnet.core.model.ReactionType
import kotlinx.coroutines.flow.Flow

interface ReactionRepository {

    suspend fun setReaction(userId: String, postId: String, type: ReactionType): AppResult<ReactionSummary>

    suspend fun removeReaction(userId: String, postId: String): AppResult<ReactionSummary>

    fun observeMyReaction(userId: String, postId: String): Flow<ReactionType?>

    fun observeSummary(postId: String): Flow<ReactionSummary>
}
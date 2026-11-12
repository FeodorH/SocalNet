package com.example.socalnet.data.repository.common

import com.example.socalnet.core.common.AppResult
import com.example.socalnet.core.model.ReactionSummary
import com.example.socalnet.core.model.ReactionType
import com.example.socalnet.core.utils.AppDispatchers
import com.example.socalnet.data.local.dao.PostDao
import com.example.socalnet.domain.repository.common.OfflineFirstReactionRepository
import com.example.socalnet.domain.repository.remote.ReactionRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class OfflineFirstReactionRepositoryImpl @Inject constructor(
    private val remoteReactionRepository: ReactionRepository,
    private val postDao: PostDao,
    private val dispatchers: AppDispatchers,
) : OfflineFirstReactionRepository {

    override fun observeMyReaction(postId: String): Flow<ReactionType?> =
        postDao.observePost(postId).map { entity ->
            entity?.myReactionType?.let { runCatching { ReactionType.valueOf(it) }.getOrNull() }
        }

    override fun observeSummary(postId: String): Flow<ReactionSummary> =
        postDao.observePost(postId).map { entity ->
            if (entity == null) return@map ReactionSummary.EMPTY

            val byType = entity.reactionsByType.mapNotNull { (key, value) ->
                runCatching { ReactionType.valueOf(key) }.getOrNull()?.let { it to value }
            }.toMap()

            ReactionSummary(
                total = entity.reactionsTotal,
                byType = byType
            )
        }

    override suspend fun setReaction(
        userId: String,
        postId: String,
        type: ReactionType
    ): AppResult<ReactionSummary> = withContext(dispatchers.io) {
        when (val result = remoteReactionRepository.setReaction(userId, postId, type)) {
            is AppResult.Success -> {
                updateRoom(postId, result.data, type.name)
                result
            }
            is AppResult.Failure -> result
        }
    }

    override suspend fun removeReaction(
        userId: String,
        postId: String
    ): AppResult<ReactionSummary> = withContext(dispatchers.io) {
        when (val result = remoteReactionRepository.removeReaction(userId, postId)) {
            is AppResult.Success -> {
                updateRoom(postId, result.data, null)
                result
            }
            is AppResult.Failure -> result
        }
    }

    private suspend fun updateRoom(postId: String, summary: ReactionSummary, myReaction: String?) {
        val byTypeStringKeys = summary.byType.mapKeys { it.key.name }.mapValues { it.value }
        postDao.updateReactions(
            postId = postId,
            total = summary.total,
            byType = byTypeStringKeys,
            myReaction = myReaction
        )
    }
}
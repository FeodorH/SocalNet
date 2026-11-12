package com.example.socalnet.data.repository.common

import com.example.socalnet.core.common.AppResult
import com.example.socalnet.core.common.WorkingRules.FEED_WINDOW
import com.example.socalnet.core.model.FeedType
import com.example.socalnet.core.model.PageCursor
import com.example.socalnet.core.model.Post
import com.example.socalnet.core.utils.AppDispatchers
import com.example.socalnet.data.local.dao.PostDao
import com.example.socalnet.data.mapper.toDomain
import com.example.socalnet.data.mapper.toEntity
import com.example.socalnet.domain.repository.remote.FeedRepository
import com.example.socalnet.domain.repository.common.OfflineFirstFeedRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class OfflineFirstFeedRepositoryImpl @Inject constructor(
    private val remoteFeedRepository: FeedRepository,
    private val postDao: PostDao,
    private val dispatchers: AppDispatchers,
) : OfflineFirstFeedRepository {

    override fun observeFeed(type: FeedType, userId: String?): Flow<List<Post>> {
        val source = when (type) {
            FeedType.GENERAL -> postDao.observeGeneralFeed(limit = FEED_WINDOW)
            FeedType.SUBSCRIPTIONS -> userId
                ?.let { postDao.observeSubscriptionFeed(it, limit = FEED_WINDOW) }
                ?: emptyFlow()
        }
        return source.map { entities -> entities.map { it.toDomain() } }
    }

    override suspend fun syncFeed(
        type: FeedType,
        cursor: PageCursor?,
        pageSize: Int
    ): AppResult<PageCursor?> = withContext(dispatchers.io) {
        when (val result = remoteFeedRepository.loadFeed(type, cursor, pageSize)) {
            is AppResult.Success -> {
                val entities = result.data.posts.map { it.toEntity() }
                if (entities.isNotEmpty()) {
                    postDao.upsertPosts(entities)
                }
                AppResult.Success(result.data.nextCursor)
            }
            is AppResult.Failure -> result
        }
    }
}
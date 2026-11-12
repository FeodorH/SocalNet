package com.example.socalnet.domain.usecase

import com.example.socalnet.core.common.AppResult
import com.example.socalnet.core.common.ValidationRules
import com.example.socalnet.core.model.FeedType
import com.example.socalnet.core.model.PageCursor
import com.example.socalnet.core.model.Post
import com.example.socalnet.domain.repository.common.OfflineFirstAuthRepository
import com.example.socalnet.domain.repository.common.OfflineFirstFeedRepository
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import javax.inject.Inject

/**
 * Наблюдение за лентой из Room. Мгновенно, без сети.
 * Подписка на смену пользователя — через flatMapLatest.
 */
class ObserveFeedUseCase @Inject constructor(
    private val authRepository: OfflineFirstAuthRepository,
    private val feeds: OfflineFirstFeedRepository
) {
    @OptIn(ExperimentalCoroutinesApi::class)
    operator fun invoke(type: FeedType): Flow<List<Post>> {
        return authRepository.currentUserId.flatMapLatest { userId ->
            if (userId == null) flowOf(emptyList())
            else feeds.observeFeed(type, userId)
        }
    }
}

/**
 * Загрузка страницы ленты из сети с записью в Room.
 * Возвращает курсор следующей страницы (null = конец).
 */
class SyncFeedUseCase @Inject constructor(
    private val feeds: OfflineFirstFeedRepository
) {
    suspend operator fun invoke(
        type: FeedType,
        cursor: PageCursor? = null
    ): AppResult<PageCursor?> = feeds.syncFeed(
        type = type,
        cursor = cursor,
        pageSize = ValidationRules.FEED_PAGE_SIZE
    )
}
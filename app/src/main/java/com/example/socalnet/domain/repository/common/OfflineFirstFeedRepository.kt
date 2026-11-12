package com.example.socalnet.domain.repository.common

import com.example.socalnet.core.common.AppResult
import com.example.socalnet.core.model.FeedType
import com.example.socalnet.core.model.PageCursor
import com.example.socalnet.core.model.Post
import kotlinx.coroutines.flow.Flow

interface OfflineFirstFeedRepository {

    /**
     * Реактивный поток постов из Room.
     * - GENERAL — все посты из кэша.
     * - SUBSCRIPTIONS — посты авторов, на которых подписан userId
     *   (SQL-подзапрос по таблице subscriptions).
     * Мгновенно, без сети.
     */
    fun observeFeed(type: FeedType, userId: String?): Flow<List<Post>>

    /**
     * Загрузка следующей страницы из сети с записью в Room.
     * Возвращает курсор для следующей страницы (null если достигнут конец).
     */
    suspend fun syncFeed(
        type: FeedType,
        cursor: PageCursor? = null,
        pageSize: Int = 20
    ): AppResult<PageCursor?>
}
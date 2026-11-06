package com.example.socalnet.domain.usecase

import com.example.socalnet.core.common.AppResult
import com.example.socalnet.core.common.ValidationRules
import com.example.socalnet.core.model.FeedType
import com.example.socalnet.core.model.PageCursor
import com.example.socalnet.core.model.PostPage
import com.example.socalnet.domain.repository.FeedRepository

class LoadFeedUseCase(private val feeds: FeedRepository) {
    suspend operator fun invoke(
        type: FeedType,
        cursor: PageCursor? = null
    ): AppResult<PostPage>{
        return feeds.loadFeed(type, cursor, ValidationRules.FEED_PAGE_SIZE)
    }
}
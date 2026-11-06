package com.example.socalnet.domain.repository

import com.example.socalnet.core.common.AppResult
import com.example.socalnet.core.model.FeedType
import com.example.socalnet.core.model.PageCursor
import com.example.socalnet.core.model.PostPage

interface FeedRepository {
    suspend fun loadFeed(
        type: FeedType,
        cursor: PageCursor?,
        pageSize: Int,
    ): AppResult<PostPage>
}
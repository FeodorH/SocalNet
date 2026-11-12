package com.example.socalnet.presentation.feed.model

import com.example.socalnet.core.model.FeedType
import com.example.socalnet.core.model.Post

data class FeedUiState(
    val selectedTab: FeedType = FeedType.GENERAL,
    val posts: List<Post> = emptyList(),
    val isLoadingMore: Boolean = false,
    val hasMore: Boolean = true,
    val isRefreshing: Boolean = false,
)
package com.example.socalnet.core.model

enum class FeedType { GENERAL, SUBSCRIPTIONS }

/**
 * Курсор пагинации — непрозрачная строка для домена.
 * Кодирование/декодирование (например, "millis|postId") — забота data-слоя.
 */
@JvmInline
value class PageCursor(val value: String)

data class PostPage(
    val posts: List<Post>,
    val nextCursor: PageCursor?,
    val hasMore: Boolean,
)

data class UserPage(
    val users: List<User>,
    val nextCursor: PageCursor?,
    val hasMore: Boolean,
)
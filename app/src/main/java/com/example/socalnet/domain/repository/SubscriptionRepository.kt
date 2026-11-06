package com.example.socalnet.domain.repository

import com.example.socalnet.core.common.AppResult
import com.example.socalnet.core.model.PageCursor
import com.example.socalnet.core.model.UserPage
import kotlinx.coroutines.flow.Flow

interface SubscriptionRepository {
    suspend fun subscribe(fromId: String, toId: String): AppResult<Unit>
    suspend fun unsubscribe(fromId: String, toId: String): AppResult<Unit>

    fun observeIsSubscribed(authorId: String): Flow<Boolean>

    suspend fun getFollowing(userId: String, cursor: PageCursor?, pageSize: Int): AppResult<UserPage>
    suspend fun getFollowers(userId: String, cursor: PageCursor?, pageSize: Int): AppResult<UserPage>

    /** ID авторов, на которых подписан пользователь — для ленты подписок. */
    suspend fun getSubscriptionIds(userId: String): AppResult<List<String>>
}
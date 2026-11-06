package com.example.socalnet.domain.repository

import com.example.socalnet.core.common.AppResult
import com.example.socalnet.core.model.ImageRef
import com.example.socalnet.core.model.PageCursor
import com.example.socalnet.core.model.Post
import com.example.socalnet.core.model.PostDraft
import com.example.socalnet.core.model.PostPage
import kotlinx.coroutines.flow.Flow

interface PostRepository {
    suspend fun createPost(
        userId: String,
        text: String,
        images: List<ImageRef.Remote>): AppResult<Post>

    suspend fun updatePost(
        userId: String,
        postId: String,
        text: String,
        addedImages: List<ImageRef.Remote>,
        removedImageUrls: List<String>,
    ): AppResult<Post>

    suspend fun deletePost(postId: String): AppResult<Unit>
    suspend fun getPost(postId: String): AppResult<Post>

    suspend fun getUserPosts(
        userId: String,
        cursor: PageCursor?,
        pageSize: Int,
    ): AppResult<PostPage>

    /** Realtime для экрана профиля (без пагинации, ограниченный лимит). */
    fun observeUserPosts(userId: String, limit: Long): Flow<List<Post>>
}
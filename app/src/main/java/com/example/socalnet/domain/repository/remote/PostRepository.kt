package com.example.socalnet.domain.repository.remote

import com.example.socalnet.core.common.AppResult
import com.example.socalnet.core.model.ImageRef
import com.example.socalnet.core.model.PageCursor
import com.example.socalnet.core.model.Post
import com.example.socalnet.core.model.PostPage

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

}
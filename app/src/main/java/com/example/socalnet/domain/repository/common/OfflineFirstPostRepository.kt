package com.example.socalnet.domain.repository.common

import com.example.socalnet.core.common.AppResult
import com.example.socalnet.core.model.ImageRef
import com.example.socalnet.core.model.PageCursor
import com.example.socalnet.core.model.Post
import kotlinx.coroutines.flow.Flow

interface OfflineFirstPostRepository {

    /** Посты конкретного пользователя (для профиля). Мгновенно из Room. */
    fun observeUserPosts(userId: String): Flow<List<Post>>

    /** Один пост по ID. Мгновенно из Room. */
    fun observePost(postId: String): Flow<Post?>

    suspend fun createPost(userId: String, text: String, images: List<ImageRef.Remote>): AppResult<Post>

    suspend fun updatePost(userId: String, postId: String, text: String, addedImages: List<ImageRef.Remote>, removedImageUrls: List<String>): AppResult<Post>

    suspend fun deletePost(postId: String): AppResult<Unit>

    /**
     * Загружает посты пользователя из сети и обновляет Room.
     * Возвращает пагинацию (курсор), чтобы UI мог запросить следующую страницу.
     */
    suspend fun syncUserPosts(userId: String, cursor: PageCursor? = null, pageSize: Int = 20): AppResult<PageCursor?>

    /** Синхронизация одного поста (например, при переходе по ссылке). */
    suspend fun syncPost(postId: String): AppResult<Post>
}
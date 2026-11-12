package com.example.socalnet.data.repository.common

import com.example.socalnet.core.common.AppResult
import com.example.socalnet.core.model.ImageRef
import com.example.socalnet.core.model.PageCursor
import com.example.socalnet.core.model.Post
import com.example.socalnet.core.utils.AppDispatchers
import com.example.socalnet.data.local.dao.PostDao
import com.example.socalnet.data.mapper.toDomain
import com.example.socalnet.data.mapper.toEntity
import com.example.socalnet.domain.repository.remote.PostRepository
import com.example.socalnet.domain.repository.common.OfflineFirstPostRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class OfflineFirstPostRepositoryImpl @Inject constructor(
    private val remotePostRepository: PostRepository,
    private val postDao: PostDao,
    private val dispatchers: AppDispatchers,
) : OfflineFirstPostRepository {

    override fun observeUserPosts(userId: String): Flow<List<Post>> =
        postDao.observeUserPosts(userId).map { entities ->
            entities.map { it.toDomain() }
        }

    override fun observePost(postId: String): Flow<Post?> =
        postDao.observePost(postId).map { it?.toDomain() }

    override suspend fun createPost(
        userId: String,
        text: String,
        images: List<ImageRef.Remote>
    ): AppResult<Post> = withContext(dispatchers.io) {
        when (val result = remotePostRepository.createPost(userId, text, images)) {
            is AppResult.Success -> {
                // Сохраняем в Room, чтобы UI сразу обновился
                postDao.upsertPost(result.data.toEntity())
                result
            }
            is AppResult.Failure -> result
        }
    }

    override suspend fun updatePost(
        userId: String,
        postId: String,
        text: String,
        addedImages: List<ImageRef.Remote>,
        removedImageUrls: List<String>
    ): AppResult<Post> = withContext(dispatchers.io) {
        when (val result = remotePostRepository.updatePost(userId, postId, text, addedImages, removedImageUrls)) {
            is AppResult.Success -> {
                postDao.upsertPost(result.data.toEntity())
                result
            }
            is AppResult.Failure -> result
        }
    }

    override suspend fun deletePost(postId: String): AppResult<Unit> = withContext(dispatchers.io) {
        when (val result = remotePostRepository.deletePost(postId)) {
            is AppResult.Success -> {
                postDao.deletePost(postId)
                result
            }
            is AppResult.Failure -> result
        }
    }

    override suspend fun syncUserPosts(
        userId: String,
        cursor: PageCursor?,
        pageSize: Int
    ): AppResult<PageCursor?> = withContext(dispatchers.io) {
        when (val result = remotePostRepository.getUserPosts(userId, cursor, pageSize)) {
            is AppResult.Success -> {
                // Сохраняем пачку постов в Room
                val entities = result.data.posts.map { it.toEntity() }
                postDao.upsertPosts(entities)

                // Возвращаем курсор, чтобы UI знал, как загрузить следующую страницу
                AppResult.Success(result.data.nextCursor)
            }
            is AppResult.Failure -> result
        }
    }

    override suspend fun syncPost(postId: String): AppResult<Post> = withContext(dispatchers.io) {
        when (val result = remotePostRepository.getPost(postId)) {
            is AppResult.Success -> {
                postDao.upsertPost(result.data.toEntity())
                result
            }
            is AppResult.Failure -> {
                // Если сети нет, пробуем отдать из Room
                val cached = postDao.getPost(postId)
                if (cached != null) {
                    AppResult.Success(cached.toDomain())
                } else {
                    result
                }
            }
        }
    }
}
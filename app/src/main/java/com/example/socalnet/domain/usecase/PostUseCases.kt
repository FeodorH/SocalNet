package com.example.socalnet.domain.usecase

import com.example.socalnet.core.common.AppError
import com.example.socalnet.core.common.AppResult
import com.example.socalnet.core.common.onFailure
import com.example.socalnet.core.model.ImageRef
import com.example.socalnet.core.model.Post
import com.example.socalnet.core.model.PostDraft
import com.example.socalnet.core.utils.IdGenerator
import com.example.socalnet.domain.repository.AuthRepository
import com.example.socalnet.domain.repository.MediaRepository
import com.example.socalnet.domain.repository.PostRepository
import com.example.socalnet.domain.vaildation.PostValidator
import kotlinx.coroutines.flow.first
import java.util.UUID

class CreatePostUseCase(
    private val auth: AuthRepository,
    private val posts: PostRepository,
    private val media: MediaRepository
) {
    suspend operator fun invoke(draft: PostDraft): AppResult<Post> {
        PostValidator.validate(draft.text, draft.images.size)?.let { return AppResult.Failure(it) }

        val userId = auth.currentUserId.first() ?: return AppResult.Failure(AppError.Unauthorized)

        val uploadBatchId = IdGenerator.generate()
        val uploadedRefs = mutableListOf<ImageRef.Remote>()
        val uploadedTokens = mutableListOf<String>()

        for ((index, image) in draft.images.withIndex()) {
            val cloudinaryPath = "socialnet/posts/$uploadBatchId/$index"
            when (val r = media.upload(image, cloudinaryPath)) {
                is AppResult.Success -> {
                    uploadedRefs += r.data.imageRef
                    r.data.deleteToken?.let { uploadedTokens += it }
                }
                is AppResult.Failure -> {
                    // Компенсация по токенам
                    media.deleteByTokens(uploadedTokens)
                    return r
                }
            }
        }

        val postResult = posts.createPost(userId, draft.text, uploadedRefs)
        if (postResult is AppResult.Failure) {
            media.deleteByTokens(uploadedTokens)
        }

        return postResult
    }
}

class UpdatePostUseCase(
    private val auth: AuthRepository,
    private val posts: PostRepository,
    private val media: MediaRepository
) {
    suspend operator fun invoke(
        postId: String,
        text: String,
        existingImages: List<ImageRef.Remote>,
        addedImages: List<ImageRef.Local>
    ): AppResult<Post> {
        val userId = auth.currentUserId.first() ?: return AppResult.Failure(AppError.Unauthorized)

        val originalPost = when (val r = posts.getPost(postId)) {
            is AppResult.Success -> r.data
            is AppResult.Failure -> return r
        }

        if (originalPost.author.id != userId) {
            return AppResult.Failure(AppError.Unauthorized)
        }

        val existingUrls = existingImages.map { it.url }.toSet()
        val removedImageUrls = originalPost.images
            .map { it.url }
            .filter { it !in existingUrls }

        PostValidator.validate(text, existingImages.size + addedImages.size)
            ?.let { return AppResult.Failure(it) }

        val uploadedRefs = mutableListOf<ImageRef.Remote>()
        val uploadedTokens = mutableListOf<String>()
        val uploadBatchId = IdGenerator.generate()

        for ((index, image) in addedImages.withIndex()) {
            val path = "socialnet/posts/$postId/${uploadBatchId}_$index"
            when (val r = media.upload(image, path)) {
                is AppResult.Success -> {
                    uploadedRefs += r.data.imageRef
                    r.data.deleteToken?.let { uploadedTokens += it }
                }
                is AppResult.Failure -> {
                    media.deleteByTokens(uploadedTokens)
                    return r
                }
            }
        }

        val finalImages = existingImages + uploadedRefs
        val updateResult = posts.updatePost(
            userId = userId,
            postId = postId,
            text = text,
            addedImages = finalImages,
            removedImageUrls = removedImageUrls
        )

        if (updateResult is AppResult.Failure) {
            media.deleteByTokens(uploadedTokens)
            return updateResult
        }

        return updateResult
    }
}

class DeletePostUseCase(private val posts: PostRepository) {
    suspend operator fun invoke(postId: String): AppResult<Unit> = posts.deletePost(postId)
}

class GetPostUseCase(private val posts: PostRepository) {
    suspend operator fun invoke(postId: String): AppResult<Post> = posts.getPost(postId)
}
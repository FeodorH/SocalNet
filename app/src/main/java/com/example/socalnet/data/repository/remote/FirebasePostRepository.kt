package com.example.socalnet.data.repository.remote

import com.example.socalnet.core.common.AppError
import com.example.socalnet.core.common.AppResult
import com.example.socalnet.core.model.ImageRef
import com.example.socalnet.core.model.PageCursor
import com.example.socalnet.core.model.Post
import com.example.socalnet.core.model.PostAuthor
import com.example.socalnet.core.model.PostPage
import com.example.socalnet.core.model.ReactionType
import com.example.socalnet.core.utils.AppDispatchers
import com.example.socalnet.core.utils.CursorCodec
import com.example.socalnet.data.dto.PostResponse
import com.example.socalnet.data.dto.UserResponse
import com.example.socalnet.data.mapper.toDomain
import com.example.socalnet.domain.repository.remote.AuthRepository
import com.example.socalnet.domain.repository.remote.PostRepository
import com.google.firebase.Firebase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebasePostRepository @Inject constructor(
    private val authRepository: AuthRepository,
    private val dispatchers: AppDispatchers
) : PostRepository {

    private val firestore = Firebase.firestore
    private val postsCollection = firestore.collection("posts")
    private val usersCollection = firestore.collection("users")
    /** Основные методы */
    override suspend fun createPost(
        userId: String,
        text: String,
        images: List<ImageRef.Remote>
    ): AppResult<Post> = withContext(dispatchers.io) {
        try {
            val postRef = postsCollection.document()

            val postData = mapOf(
                "authorId" to userId,
                "text" to text,
                "imageUrls" to images.map { it.url },
                "imagePublicIds" to images.map { it.publicId },
                "createdAt" to FieldValue.serverTimestamp(),
                "updatedAt" to FieldValue.serverTimestamp(),
                "reactionsTotal" to 0L,
                "reactionsByType" to emptyMap<String, Long>()
            )

            postRef.set(postData).await()

            val snapshot = postRef.get().await()
            val postResponse = snapshot.toObject(PostResponse::class.java)
                ?: return@withContext AppResult.Failure(AppError.NotFound)

            val author =
                getAuthor(userId) ?: return@withContext AppResult.Failure(AppError.NotFound)

            AppResult.Success(postResponse.toDomain(author))
        } catch (e: Exception) {
            AppResult.Failure(AppError.Unknown(e))
        }
    }

    override suspend fun updatePost(
        userId: String,
        postId: String,
        text: String,
        addedImages: List<ImageRef.Remote>,
        removedImageUrls: List<String>
    ): AppResult<Post> = withContext(dispatchers.io) {
        try {
            val postRef = postsCollection.document(postId)

            val updates = mapOf(
                "text" to text,
                "imageUrls" to addedImages.map { it.url },
                "imagePublicIds" to addedImages.map { it.publicId },
                "updatedAt" to FieldValue.serverTimestamp()
            )

            postRef.update(updates).await()

            val snapshot = postRef.get().await()
            val postResponse = snapshot.toObject(PostResponse::class.java)
                ?: return@withContext AppResult.Failure(AppError.NotFound)

            val author =
                getAuthor(userId) ?: return@withContext AppResult.Failure(AppError.NotFound)

            AppResult.Success(postResponse.toDomain(author))
        } catch (e: Exception) {
            AppResult.Failure(AppError.Unknown(e))
        }
    }

    override suspend fun deletePost(postId: String): AppResult<Unit> = withContext(dispatchers.io) {
        try {
            postsCollection.document(postId).delete().await()
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Failure(AppError.Unknown(e))
        }
    }

    override suspend fun getPost(postId: String): AppResult<Post> = withContext(dispatchers.io) {
        try {
            val snapshot = postsCollection.document(postId).get().await()
            val postResponse = snapshot.toObject(PostResponse::class.java)
                ?: return@withContext AppResult.Failure(AppError.NotFound)

            val author = getAuthor(postResponse.authorId)
                ?: return@withContext AppResult.Failure(AppError.NotFound)

            val myReaction = getMyReaction(postId)

            AppResult.Success(postResponse.toDomain(author, myReaction))
        } catch (e: Exception) {
            AppResult.Failure(AppError.Unknown(e))
        }
    }

    override suspend fun getUserPosts(
        userId: String,
        cursor: PageCursor?,
        pageSize: Int
    ): AppResult<PostPage> = withContext(dispatchers.io) {
        try {
            var query = postsCollection
                .whereEqualTo("authorId", userId)
                .orderBy("createdAt", Query.Direction.DESCENDING)
                .limit(pageSize.toLong())

            if (cursor != null) {
                val (timestamp, postId) = CursorCodec.decode(cursor)
                query = query.startAfter(timestamp, postId)
            }

            val snapshot = query.get().await()
            val postResponses = snapshot.documents.mapNotNull {
                it.toObject(PostResponse::class.java)
            }

            val author = getAuthor(userId)
                ?: return@withContext AppResult.Failure(AppError.NotFound)

            val posts = postResponses.mapNotNull { response ->
                val myReaction = getMyReaction(response.id)
                response.toDomain(author, myReaction)
            }

            val lastDoc = snapshot.documents.lastOrNull()
            val nextCursor = if (lastDoc != null && snapshot.documents.size == pageSize) {
                val timestamp = lastDoc.getTimestamp("createdAt")
                if (timestamp != null) CursorCodec.encode(timestamp, lastDoc.id) else null
            } else null

            AppResult.Success(
                PostPage(
                    posts = posts,
                    nextCursor = nextCursor,
                    hasMore = nextCursor != null
                )
            )
        } catch (e: Exception) {
            AppResult.Failure(AppError.Unknown(e))
        }
    }

    private suspend fun getAuthor(authorId: String): PostAuthor? {
        val snapshot = usersCollection.document(authorId).get().await()
        val userResponse = snapshot.toObject(UserResponse::class.java) ?: return null
        return PostAuthor(
            id = userResponse.id,
            name = userResponse.name,
            avatarUrl = userResponse.avatarUrl
        )
    }

    private suspend fun getMyReaction(postId: String): ReactionType? {
        val userId = authRepository.currentUserId.first() ?: return null
        val snapshot = postsCollection.document(postId)
            .collection("reactions").document(userId)
            .get().await()

        val type = snapshot.getString("type") ?: return null
        return runCatching { ReactionType.valueOf(type) }.getOrNull()
    }
}
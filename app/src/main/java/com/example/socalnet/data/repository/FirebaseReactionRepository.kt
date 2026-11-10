package com.example.socalnet.data.repository

import com.example.socalnet.core.common.AppError
import com.example.socalnet.core.common.AppResult
import com.example.socalnet.core.model.ReactionSummary
import com.example.socalnet.core.model.ReactionType
import com.example.socalnet.core.utils.AppDispatchers
import com.example.socalnet.data.mapper.toReactionSummary
import com.example.socalnet.domain.repository.ReactionRepository
import com.google.firebase.Firebase
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebaseReactionRepository @Inject constructor(
    private val dispatchers: AppDispatchers
) : ReactionRepository {

    private val firestore = Firebase.firestore
    private val postsCollection = firestore.collection("posts")

    override suspend fun setReaction(
        userId: String,
        postId: String,
        type: ReactionType
    ): AppResult<ReactionSummary> = withContext(dispatchers.io) {
        try {
            val postRef = postsCollection.document(postId)
            val reactionRef = postRef.collection("reactions").document(userId)

            firestore.runTransaction { transaction ->
                val reactionSnapshot = transaction.get(reactionRef)
                val previousType = reactionSnapshot
                    .getString("type")
                    ?.let { runCatching { ReactionType.valueOf(it) }.getOrNull() }

                if (previousType == type) {
                    return@runTransaction
                }

                transaction.set(
                    reactionRef,
                    mapOf(
                        "type" to type.name,
                        "createdAt" to FieldValue.serverTimestamp()
                    )
                )

                val updates = mutableMapOf<String, Any>(
                    "reactionsByType.${type.name}" to FieldValue.increment(1L)
                )
                if (previousType == null) {
                    updates["reactionsTotal"] = FieldValue.increment(1L)
                } else {
                    updates["reactionsByType.${previousType.name}"] = FieldValue.increment(-1L)
                }
                transaction.update(postRef, updates)
            }.await()

            fetchSummary(postId)
        } catch (e: Exception) {
            AppResult.Failure(mapFirestoreException(e))
        }
    }

    override suspend fun removeReaction(
        userId: String,
        postId: String
    ): AppResult<ReactionSummary> = withContext(dispatchers.io) {
        try {
            val postRef = postsCollection.document(postId)
            val reactionRef = postRef.collection("reactions").document(userId)

            firestore.runTransaction { transaction ->
                val reactionSnapshot = transaction.get(reactionRef)
                val previousType = reactionSnapshot
                    .getString("type")
                    ?.let { runCatching { ReactionType.valueOf(it) }.getOrNull() }

                if (previousType == null) {
                    return@runTransaction
                }

                transaction.delete(reactionRef)

                transaction.update(
                    postRef,
                    mapOf(
                        "reactionsTotal" to FieldValue.increment(-1L),
                        "reactionsByType.${previousType.name}" to FieldValue.increment(-1L)
                    )
                )
            }.await()

            fetchSummary(postId)
        } catch (e: Exception) {
            AppResult.Failure(mapFirestoreException(e))
        }
    }

    override fun observeMyReaction(
        userId: String,
        postId: String
    ): Flow<ReactionType?> = callbackFlow {
        val ref = postsCollection.document(postId)
            .collection("reactions").document(userId)

        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            val type = snapshot
                ?.getString("type")
                ?.let { runCatching { ReactionType.valueOf(it) }.getOrNull() }
            trySend(type)
        }

        awaitClose { listener.remove() }
    }.flowOn(dispatchers.io)

    override fun observeSummary(postId: String): Flow<ReactionSummary> = callbackFlow {
        val ref = postsCollection.document(postId)

        val listener = ref.addSnapshotListener { snapshot, error ->
            if (error != null) {
                close(error)
                return@addSnapshotListener
            }
            if (snapshot != null) {
                trySend(extractSummary(snapshot))
            }
        }

        awaitClose { listener.remove() }
    }.flowOn(dispatchers.io)

    private suspend fun fetchSummary(postId: String): AppResult<ReactionSummary> {
        val snapshot = postsCollection.document(postId).get().await()
        if (!snapshot.exists()) return AppResult.Failure(AppError.NotFound)
        return AppResult.Success(extractSummary(snapshot))
    }

    private fun extractSummary(snapshot: DocumentSnapshot): ReactionSummary {
        val total = snapshot.getLong("reactionsTotal") ?: 0L
        @Suppress("UNCHECKED_CAST")
        val byTypeRaw = snapshot.get("reactionsByType") as? Map<String, Number>
            ?: emptyMap()
        val byType = byTypeRaw.mapValues { it.value.toLong() }
        return byType.toReactionSummary(total)
    }

    private fun mapFirestoreException(e: Exception): AppError = when {
        e.message?.contains("PERMISSION_DENIED", ignoreCase = true) == true ->
            AppError.Unauthorized
        e.message?.contains("NOT_FOUND", ignoreCase = true) == true ->
            AppError.NotFound
        e.message?.contains("UNAVAILABLE", ignoreCase = true) == true ||
                e.message?.contains("network", ignoreCase = true) == true ->
            AppError.Network
        else -> AppError.Unknown(e)
    }
}
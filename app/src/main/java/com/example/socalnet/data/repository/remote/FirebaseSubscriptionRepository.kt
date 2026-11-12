package com.example.socalnet.data.repository.remote

import com.example.socalnet.core.common.AppError
import com.example.socalnet.core.common.AppResult
import com.example.socalnet.core.model.PageCursor
import com.example.socalnet.core.model.User
import com.example.socalnet.core.model.UserPage
import com.example.socalnet.core.utils.AppDispatchers
import com.example.socalnet.core.utils.CursorCodec
import com.example.socalnet.data.dto.UserResponse
import com.example.socalnet.data.mapper.toDomain
import com.example.socalnet.domain.repository.remote.AuthRepository
import com.example.socalnet.domain.repository.remote.SubscriptionRepository
import com.google.firebase.Firebase
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebaseSubscriptionRepository @Inject constructor(
    private val authRepository: AuthRepository,
    private val dispatchers: AppDispatchers
) : SubscriptionRepository {

    private val firestore = Firebase.firestore
    private val usersCollection = firestore.collection("users")

    // --- ПОДПИСКА / ОТПИСКА ---

    override suspend fun subscribe(fromId: String, toId: String): AppResult<Unit> =
        withContext(dispatchers.io) {
            try {
                if (fromId == toId) {
                    return@withContext AppResult.Failure(
                        AppError.Validation(
                            AppError.Validation.Field.NAME,
                            "Нельзя подписаться на самого себя"
                        )
                    )
                }

                val batch = firestore.batch()
                val data = mapOf("createdAt" to FieldValue.serverTimestamp())

                val followingRef = usersCollection.document(fromId)
                    .collection("following").document(toId)
                val followersRef = usersCollection.document(toId)
                    .collection("followers").document(fromId)

                batch.set(followingRef, data)
                batch.set(followersRef, data)
                batch.commit().await()

                AppResult.Success(Unit)
            } catch (e: Exception) {
                AppResult.Failure(mapFirestoreException(e))
            }
        }

    override suspend fun unsubscribe(fromId: String, toId: String): AppResult<Unit> =
        withContext(dispatchers.io) {
            try {
                val batch = firestore.batch()

                val followingRef = usersCollection.document(fromId)
                    .collection("following").document(toId)
                val followersRef = usersCollection.document(toId)
                    .collection("followers").document(fromId)

                batch.delete(followingRef)
                batch.delete(followersRef)
                batch.commit().await()

                AppResult.Success(Unit)
            } catch (e: Exception) {
                AppResult.Failure(mapFirestoreException(e))
            }
        }

    // --- REALTIME-СТАТУС ПОДПИСКИ ---

    override fun observeIsSubscribed(authorId: String): Flow<Boolean> =
        authRepository.currentUserId
            .flatMapLatest { currentUserId ->
                if (currentUserId == null) {
                    flowOf(false)
                } else {
                    observeSubscriptionDoc(currentUserId, authorId)
                }
            }
            .flowOn(dispatchers.io)

    private fun observeSubscriptionDoc(userId: String, authorId: String): Flow<Boolean> =
        callbackFlow {
            val ref = usersCollection.document(userId)
                .collection("following").document(authorId)

            val listener = ref.addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }
                trySend(snapshot?.exists() == true)
            }

            awaitClose { listener.remove() }
        }

    // --- СПИСКИ ПОДПИСОК / ПОДПИСЧИКОВ ---

    override suspend fun getFollowing(
        userId: String,
        cursor: PageCursor?,
        pageSize: Int
    ): AppResult<UserPage> = withContext(dispatchers.io) {
        try {
            fetchUserPage(
                collectionPath = usersCollection.document(userId).collection("following"),
                cursor = cursor,
                pageSize = pageSize
            )
        } catch (e: Exception) {
            AppResult.Failure(mapFirestoreException(e))
        }
    }

    override suspend fun getFollowers(
        userId: String,
        cursor: PageCursor?,
        pageSize: Int
    ): AppResult<UserPage> = withContext(dispatchers.io) {
        try {
            fetchUserPage(
                collectionPath = usersCollection.document(userId).collection("followers"),
                cursor = cursor,
                pageSize = pageSize
            )
        } catch (e: Exception) {
            AppResult.Failure(mapFirestoreException(e))
        }
    }

    // --- ID ПОДПИСОК (ДЛЯ ЛЕНТЫ) ---

    override suspend fun getSubscriptionIds(userId: String): AppResult<List<String>> =
        withContext(dispatchers.io) {
            try {
                val snapshot = usersCollection.document(userId)
                    .collection("following")
                    .get().await()
                AppResult.Success(snapshot.documents.map { it.id })
            } catch (e: Exception) {
                AppResult.Failure(mapFirestoreException(e))
            }
        }

    // --- ВСПОМОГАТЕЛЬНЫЕ ---

    /**
     * Общая логика пагинации для following/followers.
     * Оба списка хранят только ID пользователей + createdAt.
     * Полные User-объекты подгружаются параллельно.
     */
    private suspend fun fetchUserPage(
        collectionPath: CollectionReference,
        cursor: PageCursor?,
        pageSize: Int
    ): AppResult<UserPage> {
        // pageSize + 1 — чтобы точно определить, есть ли следующая страница
        var query = collectionPath
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .limit((pageSize + 1).toLong())

        if (cursor != null) {
            val (timestamp, id) = CursorCodec.decode(cursor)
            query = query.startAfter(timestamp, id)
        }

        val snapshot = query.get().await()
        val docs = snapshot.documents
        val hasMore = docs.size > pageSize
        val pageDocs = if (hasMore) docs.subList(0, pageSize) else docs

        val userIds = pageDocs.map { it.id }
        val users = fetchUsers(userIds)

        val nextCursor = if (hasMore && pageDocs.isNotEmpty()) {
            val last = pageDocs.last()
            val ts = last.getTimestamp("createdAt")
            if (ts != null) CursorCodec.encode(ts, last.id) else null
        } else null

        return AppResult.Success(
            UserPage(
                users = users,
                nextCursor = nextCursor,
                hasMore = hasMore
            )
        )
    }

    /**
     * Параллельная загрузка User по списку ID.
     * Использует async вместо getAll — тот же паттерн, что в FeedRepository.
     */
    private suspend fun fetchUsers(userIds: List<String>): List<User> = coroutineScope {
        if (userIds.isEmpty()) return@coroutineScope emptyList()

        userIds.map { id ->
            async {
                val snapshot = usersCollection.document(id).get().await()
                snapshot.toObject(UserResponse::class.java)?.toDomain()
            }
        }.awaitAll().filterNotNull()
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
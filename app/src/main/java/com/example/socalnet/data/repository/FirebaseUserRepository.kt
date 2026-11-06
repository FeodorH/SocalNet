package com.example.socalnet.data.repository

import com.example.socalnet.core.common.AppError
import com.example.socalnet.core.common.AppResult
import com.example.socalnet.core.model.User
import com.example.socalnet.core.utils.AppDispatchers
import com.example.socalnet.data.dto.UserResponse
import com.example.socalnet.data.mapper.toDomain
import com.example.socalnet.domain.repository.AuthRepository
import com.example.socalnet.domain.repository.UserRepository
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebaseUserRepository @Inject constructor(
    private val authRepository: AuthRepository,
    private val dispatchers: AppDispatchers
) : UserRepository {

    private val firestore = Firebase.firestore
    private val usersCollection = firestore.collection("users")

    override suspend fun getUser(userId: String): AppResult<User> =
        withContext(dispatchers.io) {
            try {
                val snapshot = usersCollection.document(userId).get().await()
                val userResponse = snapshot.toObject(UserResponse::class.java)

                if (userResponse != null) {
                    AppResult.Success(userResponse.toDomain())
                } else {
                    AppResult.Failure(AppError.NotFound)
                }
            } catch (e: Exception) {
                AppResult.Failure(AppError.Unknown(e))
            }
        }

    override fun observeUser(userId: String): Flow<User?> = callbackFlow {
        val registration = usersCollection.document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    close(error)
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val userResponse = snapshot.toObject(UserResponse::class.java)
                    trySend(userResponse?.toDomain())
                } else {
                    trySend(null)
                }
            }

        awaitClose { registration.remove() }
    }.flowOn(dispatchers.io)

    override suspend fun updateProfile(
        userId: String,
        name: String?,
        bio: String?,
        avatarUrl: String?
    ): AppResult<User> = withContext(dispatchers.io) {
        try {
            val updates = mutableMapOf<String, Any>()
            name?.let { updates["name"] = it }
            bio?.let { updates["bio"] = it }
            avatarUrl?.let { updates["avatarUrl"] = it }

            if (updates.isEmpty()) {
                return@withContext getUser(userId)
            }

            val userRef = usersCollection.document(userId)
            userRef.update(updates).await()

            val snapshot = userRef.get().await()
            val userResponse = snapshot.toObject(UserResponse::class.java)
                ?: return@withContext AppResult.Failure(AppError.NotFound)

            AppResult.Success(userResponse.toDomain())
        } catch (e: Exception) {
            AppResult.Failure(AppError.Unknown(e))
        }
    }
}
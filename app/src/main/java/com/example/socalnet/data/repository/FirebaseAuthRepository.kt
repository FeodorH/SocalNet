package com.example.socalnet.data.repository

import com.example.socalnet.core.common.AppError
import com.example.socalnet.core.common.AppResult
import com.example.socalnet.core.model.User
import com.example.socalnet.core.utils.AppDispatchers
import com.example.socalnet.data.dto.CreateUserRequest
import com.example.socalnet.data.dto.UserResponse
import com.example.socalnet.data.mapper.toDomain
import com.example.socalnet.domain.repository.AuthRepository
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.FirebaseAuthUserCollisionException
import com.google.firebase.auth.FirebaseAuthWeakPasswordException
import com.google.firebase.auth.auth
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FirebaseAuthRepository @Inject constructor(
    private val dispatchers: AppDispatchers
) : AuthRepository {

    private val auth = Firebase.auth
    private val firestore = Firebase.firestore

    override val currentUserId: Flow<String?> = callbackFlow {
        val listener = FirebaseAuth.AuthStateListener { auth ->
            trySend(auth.currentUser?.uid)
        }
        auth.addAuthStateListener(listener)

        awaitClose { auth.removeAuthStateListener(listener) }
    }.flowOn(dispatchers.io)

    override suspend fun register(email: String, password: String, name: String): AppResult<User> =
        withContext(dispatchers.io) {
            try {
                val authResult = auth.createUserWithEmailAndPassword(email, password).await()
                val uid = authResult.user?.uid ?: return@withContext AppResult.Failure(AppError.Unknown())

                val request = CreateUserRequest(email = email, name = name)
                val userData = mapOf(
                    "email" to request.email,
                    "name" to request.name,
                    "createdAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
                )
                val userRef = firestore.collection("users").document(uid)
                userRef.set(userData).await()

                val snapshot = userRef.get().await()
                val userResponse = snapshot.toObject(UserResponse::class.java)
                    ?: return@withContext AppResult.Failure(AppError.NotFound)

                AppResult.Success(userResponse.toDomain())
            } catch (e: Exception) {
                AppResult.Failure(mapAuthException(e))
            }
        }

    override suspend fun login(email: String, password: String): AppResult<User> =
        withContext(dispatchers.io) {
            try {
                val authResult = auth.signInWithEmailAndPassword(email, password).await()
                val uid = authResult.user?.uid ?: return@withContext AppResult.Failure(AppError.Unknown())

                // Получаем профиль из Firestore
                val snapshot = firestore.collection("users").document(uid).get().await()
                val userResponse = snapshot.toObject(UserResponse::class.java)

                if (userResponse != null) {
                    AppResult.Success(userResponse.toDomain())
                } else {
                    AppResult.Failure(AppError.NotFound)
                }
            } catch (e: Exception) {
                AppResult.Failure(mapAuthException(e))
            }
        }

    override suspend fun logout(): AppResult<Unit> = withContext(dispatchers.io) {
        try {
            auth.signOut()
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Failure(AppError.Unknown(e))
        }
    }

    override suspend fun sendPasswordReset(email: String): AppResult<Unit> = withContext(dispatchers.io) {
        try {
            auth.sendPasswordResetEmail(email).await()
            AppResult.Success(Unit)
        } catch (e: Exception) {
            AppResult.Failure(mapAuthException(e))
        }
    }

    // Маппинг ошибок Firebase в наши AppError
    private fun mapAuthException(e: Exception): AppError {
        return when (e) {
            is FirebaseAuthWeakPasswordException -> AppError.Validation(AppError.Validation.Field.PASSWORD, "Слишком слабый пароль")
            is FirebaseAuthUserCollisionException -> AppError.Validation(AppError.Validation.Field.EMAIL, "Пользователь с таким email уже существует")
            is FirebaseAuthInvalidCredentialsException -> AppError.Unauthorized
            is java.io.IOException -> AppError.Network
            else -> AppError.Unknown(e)
        }
    }
}
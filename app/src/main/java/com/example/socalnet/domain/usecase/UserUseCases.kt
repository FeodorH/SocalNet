package com.example.socalnet.domain.usecase

import com.example.socalnet.core.common.AppError
import com.example.socalnet.core.common.AppResult
import com.example.socalnet.core.model.User
import com.example.socalnet.domain.repository.common.OfflineFirstAuthRepository
import com.example.socalnet.domain.repository.common.OfflineFirstUserRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import javax.inject.Inject

class GetCurrentUserUseCase @Inject constructor(
    private val authRepository: OfflineFirstAuthRepository,
    private val userRepository: OfflineFirstUserRepository
) {
    suspend operator fun invoke(): AppResult<User> {
        val userId = authRepository.currentUserId.first()
            ?: return AppResult.Failure(AppError.Unauthorized)
        return userRepository.getUser(userId)  // network-first + fallback
    }
}

class ObserveCurrentUserUseCase @Inject constructor(
    private val authRepository: OfflineFirstAuthRepository
) {
    /** Реактивный поток из Room. Внутри — flatMapLatest по currentUserId. */
    operator fun invoke(): Flow<User?> = authRepository.observeCurrentUser()
}

class UpdateProfileUseCase @Inject constructor(
    private val authRepository: OfflineFirstAuthRepository,
    private val userRepository: OfflineFirstUserRepository
) {
    suspend operator fun invoke(
        name: String?,
        bio: String?,
        avatarUrl: String?,
        avatarPublicId: String?,
        removeAvatar: Boolean = false
    ): AppResult<User> {
        val userId = authRepository.currentUserId.first()
            ?: return AppResult.Failure(AppError.Unauthorized)

        if (name != null && name.isBlank()) {
            return AppResult.Failure(
                AppError.Validation(AppError.Validation.Field.NAME, "Имя не может быть пустым")
            )
        }

        return userRepository.updateProfile(
            userId = userId,
            name = name,
            bio = bio,
            avatarUrl = avatarUrl,
            avatarPublicId = avatarPublicId,
            removeAvatar = removeAvatar
        )
    }
}
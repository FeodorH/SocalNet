package com.example.socalnet.data.mapper

import com.example.socalnet.core.model.User
import com.example.socalnet.data.dto.CreateUserRequest
import com.example.socalnet.data.dto.UpdateUserRequest
import com.example.socalnet.data.dto.UserResponse

/**
 * Маппинг из ответа Firestore в доменную модель.
 */
fun UserResponse.toDomain(): User {
    return User(
        id = id,
        email = email,
        name = name,
        avatarUrl = avatarUrl,
        avatarPublicId = avatarPublicId,
        bio = bio,
        createdAt = createdAt.toDomainInstant()
    )
}

/**
 * Маппинг доменной модели в Map для записи при создании.
 */
fun User.toCreateMap(): Map<String, Any?> {
    return mapOf(
        "email" to email,
        "name" to name,
        "avatarUrl" to avatarUrl,
        "avatarPublicId" to avatarPublicId,
        "bio" to bio
    )
}

/**
 * Маппинг Request DTO для создания.
 */
fun CreateUserRequest.toMap(): Map<String, Any?> {
    return mapOf(
        "email" to email,
        "name" to name
    )
}

/**
 * Маппинг Request DTO для обновления.
 */
fun UpdateUserRequest.toMap(): Map<String, Any?> {
    val map = mutableMapOf<String, Any?>()
    name?.let { map["name"] = it }
    bio?.let { map["bio"] = it }

    // Обновляем аватарку только если она передана
    if (avatarUrl != null) {
        map["avatarUrl"] = avatarUrl
        map["avatarPublicId"] = avatarPublicId
    }
    return map
}
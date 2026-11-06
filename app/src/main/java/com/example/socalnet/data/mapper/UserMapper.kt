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
        bio = bio,
        createdAt = createdAt.toDomainInstant()
    )
}

/**
 * Маппинг доменной модели в Map для записи при создании.
 * Заметьте: мы НЕ передаем id и createdAt.
 * createdAt добавится в репозитории через FieldValue.serverTimestamp().
 */
fun User.toCreateMap(): Map<String, Any?> {
    return mapOf(
        "email" to email,
        "name" to name,
        "avatarUrl" to avatarUrl,
        "bio" to bio
    )
}

/**
 * Маппинг доменной модели в Map для обновления.
 * Включаем только те поля, которые не null.
 */
fun User.toUpdateMap(): Map<String, Any?> {
    val map = mutableMapOf<String, Any?>()
    if (name.isNotBlank()) map["name"] = name
    if (bio != null) map["bio"] = bio
    if (avatarUrl != null) map["avatarUrl"] = avatarUrl
    return map
}

/**
 * Вспомогательные маппер для Request DTO.
 */
fun CreateUserRequest.toMap(): Map<String, Any?> {
    return mapOf(
        "email" to email,
        "name" to name
    )
}

fun UpdateUserRequest.toMap(): Map<String, Any?> {
    val map = mutableMapOf<String, Any?>()
    name?.let { map["name"] = it }
    bio?.let { map["bio"] = it }
    avatarUrl?.let { map["avatarUrl"] = it }
    return map
}
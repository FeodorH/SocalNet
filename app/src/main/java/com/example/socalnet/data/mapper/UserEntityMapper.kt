package com.example.socalnet.data.mapper

import com.example.socalnet.core.model.User
import com.example.socalnet.data.local.entity.UserEntity
import kotlin.time.Instant

fun UserEntity.toDomain(): User = User(
    id = id,
    email = email,
    name = name,
    avatarUrl = avatarUrl,
    avatarPublicId = avatarPublicId,
    bio = bio,
    createdAt = Instant.fromEpochMilliseconds(createdAtEpochMillis)
)

fun User.toEntity(): UserEntity = UserEntity(
    id = id,
    email = email,
    name = name,
    avatarUrl = avatarUrl,
    avatarPublicId = avatarPublicId,
    bio = bio,
    createdAtEpochMillis = createdAt.toEpochMilliseconds(),
    cachedAt = System.currentTimeMillis()
)
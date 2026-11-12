package com.example.socalnet.data.local.entity

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey val id: String,
    val email: String,
    val name: String,
    val avatarUrl: String?,
    val avatarPublicId: String?,
    val bio: String?,
    val createdAtEpochMillis: Long,
    val cachedAt: Long = System.currentTimeMillis()
)
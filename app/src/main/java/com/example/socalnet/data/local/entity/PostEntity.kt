package com.example.socalnet.data.local.entity

import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "posts",
    indices = [
        Index("authorId"),
        Index("createdAtEpochMillis")
    ]
)
data class PostEntity(
    @PrimaryKey val id: String,
    val authorId: String,
    val authorName: String,
    val authorAvatarUrl: String?,
    val text: String,
    val imageUrls: List<String>,
    val imagePublicIds: List<String>,
    val createdAtEpochMillis: Long,
    val updatedAtEpochMillis: Long?,
    val reactionsTotal: Int,
    val reactionsByType: Map<String, Int>,
    val myReactionType: String?,
    val cachedAt: Long = System.currentTimeMillis()
)
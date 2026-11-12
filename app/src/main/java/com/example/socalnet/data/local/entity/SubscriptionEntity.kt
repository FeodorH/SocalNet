package com.example.socalnet.data.local.entity

import androidx.room.Entity
import androidx.room.Index

@Entity(
    tableName = "subscriptions",
    primaryKeys = ["userId", "authorId"],
    indices = [Index("userId")]
)
data class SubscriptionEntity(
    val userId: String,
    val authorId: String,
    val createdAtEpochMillis: Long
)
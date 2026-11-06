package com.example.socalnet.data.dto

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import kotlin.time.Instant

data class CreateUserRequest(
    val email: String,
    val name: String,
)

data class UpdateUserRequest(
    val name: String? = null,
    val bio: String? = null,
    val avatarUrl: String? = null
)

data class UserResponse(
    @DocumentId val id: String = "",
    val email: String = "",
    val name: String = "",
    val avatarUrl: String? = null,
    val bio: String? = null,
    val createdAt: Timestamp? = null
)
package com.example.socalnet.core.model

import kotlin.time.Instant

data class User (
    val id: String,
    val email: String,
    val name: String,
    val avatarUrl: String? = null,
    val avatarPublicId: String? = null,
    val bio: String? = null,
    val createdAt: Instant,
)
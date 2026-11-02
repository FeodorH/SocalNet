package com.example.socalnet.core.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class User @OptIn(ExperimentalTime::class) constructor(
    val id: String,
    val email: String,
    val name: String,
    val avatarUrl: String? = null,
    val bio: String? = null,
    val createdAt: Instant,
)
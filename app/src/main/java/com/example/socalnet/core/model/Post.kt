package com.example.socalnet.core.model

import kotlin.time.ExperimentalTime
import kotlin.time.Instant

data class PostAuthor(
    val id: String,
    val name: String,
    val avatarUrl: String?,
)

data class Post @OptIn(ExperimentalTime::class) constructor(
    val id: String,
    val author: PostAuthor,
    val text: String,
    val images: List<ImageRef.Remote>,
    val createdAt: Instant,
    val updatedAt: Instant? = null,
    val reactions: ReactionSummary = ReactionSummary.EMPTY,
    val myReaction: ReactionType? = null,
)

data class PostDraft(
    val text: String,
    val images: List<ImageRef.Local>,
)
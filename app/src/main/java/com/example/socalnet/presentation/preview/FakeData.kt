package com.example.socalnet.presentation.preview

import com.example.socalnet.core.model.*
import kotlin.time.Clock

private val NOW = Clock.System.now()

private val alice = PostAuthor("1", "Alice Wonder", null)
private val bob = PostAuthor("2", "Bob Builder", null)
private val charlie = PostAuthor("3", "Charlie C", null)

val fakePostNoImages = Post(
    id = "p1",
    author = alice,
    text = "Сегодня отличный день для нового поста. Пишу из своего любимого кафе ☕",
    images = emptyList(),
    createdAt = NOW - kotlin.time.Duration.parse("5m"),
    reactions = ReactionSummary(
        total = 12,
        byType = mapOf(
            ReactionType.LIKE to 8,
            ReactionType.LOVE to 4,
        ),
    ),
    myReaction = ReactionType.LIKE,
)

val fakePostOneImage = Post(
    id = "p2",
    author = bob,
    text = "Посмотрите на этот закат!",
    images = listOf(ImageRef.Remote("https://example.com/img1.jpg", null)),
    createdAt = NOW - kotlin.time.Duration.parse("2h"),
    reactions = ReactionSummary(
        total = 42,
        byType = mapOf(ReactionType.LIKE to 30, ReactionType.FIRE to 12),
    ),
    myReaction = null,
)

val fakePostTwoImages = Post(
    id = "p3",
    author = charlie,
    text = "Свежие фото с прогулки 🌳",
    images = listOf(
        ImageRef.Remote("https://example.com/img1.jpg", null),
        ImageRef.Remote("https://example.com/img2.jpg", null),
    ),
    createdAt = NOW - kotlin.time.Duration.parse("1d"),
    reactions = ReactionSummary(
        total = 5,
        byType = mapOf(ReactionType.LOVE to 5),
    ),
    myReaction = ReactionType.LOVE,
)

val fakePostFourImages = Post(
    id = "p4",
    author = alice,
    text = "Много фото с поездки",
    images = (1..4).map {
        ImageRef.Remote("https://example.com/img$it.jpg", null)
    },
    createdAt = NOW - kotlin.time.Duration.parse("3d"),
    reactions = ReactionSummary.EMPTY,
    myReaction = null,
)

val fakeFeed = listOf(
    fakePostNoImages,
    fakePostOneImage,
    fakePostTwoImages,
    fakePostFourImages,
)
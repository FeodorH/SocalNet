package com.example.socalnet.core.common

object ValidationRules {
    const val MIN_PASSWORD = 8
    const val MAX_PASSWORD = 32
    const val MAX_NAME = 50
    const val MAX_EMAIL = 100

    const val MAX_POST_TEXT = 5_000
    const val MAX_POST_IMAGES = 10
    const val MAX_IMAGE_BYTES = 10L * 1024 * 1024
    val ALLOWED_IMAGE_MIME = setOf("image/jpeg", "image/png", "image/gif")

    const val FEED_PAGE_SIZE = 10
    const val MAX_CACHED_POSTS = 100
}
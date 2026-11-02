package com.example.socalnet.core.model

sealed interface ImageRef {
    data class Local(val uri: String) : ImageRef
    data class Remote(val url: String) : ImageRef
}

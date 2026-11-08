package com.example.socalnet.core.model

data class ImageUploadResult(
    val imageRef: ImageRef.Remote,
    val deleteToken: String? = null
)
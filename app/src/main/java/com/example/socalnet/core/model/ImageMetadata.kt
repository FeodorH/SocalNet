package com.example.socalnet.core.model

/**
 * Метаданные изображения, полученные до загрузки.
 * Специально не содержит Context/Uri — это чистые данные.
 */
data class ImageMetadata(
    val mimeType: String?,
    val sizeBytes: Long,
)
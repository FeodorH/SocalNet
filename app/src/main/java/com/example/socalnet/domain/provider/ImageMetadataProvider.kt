package com.example.socalnet.domain.provider

import com.example.socalnet.core.common.AppResult
import com.example.socalnet.core.model.ImageMetadata

/**
 * Порт для получения MIME и размера изображения по URI.
 * Реализация — единственное место, где используется Android ContentResolver.
 */
interface ImageMetadataProvider {
    suspend fun read(uri: String): AppResult<ImageMetadata>
}
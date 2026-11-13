package com.example.socalnet.domain.vaildation

import com.example.socalnet.core.common.AppError
import com.example.socalnet.core.common.ValidationRules
import com.example.socalnet.core.model.ImageMetadata

object ImageValidator {

    fun validate(metadata: ImageMetadata): AppError.Validation? = when {
        metadata.sizeBytes <= 0L ->
            AppError.Validation(
                AppError.Validation.Field.IMAGES,
                "Не удалось прочитать файл",
            )
        metadata.sizeBytes > ValidationRules.MAX_IMAGE_BYTES ->
            AppError.Validation(
                AppError.Validation.Field.IMAGES,
                "Файл больше ${ValidationRules.MAX_IMAGE_BYTES / (1024 * 1024)} МБ",
            )
        metadata.mimeType == null ->
            AppError.Validation(
                AppError.Validation.Field.IMAGES,
                "Неизвестный формат файла",
            )
        metadata.mimeType !in ValidationRules.ALLOWED_IMAGE_MIME ->
            AppError.Validation(
                AppError.Validation.Field.IMAGES,
                "Поддерживаются только JPG, PNG и GIF",
            )
        else -> null
    }
}
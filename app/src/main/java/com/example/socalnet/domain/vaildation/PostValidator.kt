package com.example.socalnet.domain.vaildation

import com.example.socalnet.core.common.AppError
import com.example.socalnet.core.common.ValidationRules

object PostValidator {
    fun validate(
        text: String,
        imageCount: Int) : AppError.Validation? =
        when{
            text.isBlank() ->
                AppError.Validation(AppError.Validation.Field.TEXT, "Название поста не может быть пустым")
            text.length > ValidationRules.MAX_POST_TEXT ->
                AppError.Validation(AppError.Validation.Field.TEXT, "Максимальная длина поста: ${ValidationRules.MAX_POST_TEXT}")
            imageCount > ValidationRules.MAX_POST_IMAGES ->
                AppError.Validation(AppError.Validation.Field.IMAGES, "Не более ${ValidationRules.MAX_POST_IMAGES} изображений")
            else ->
                null
        }
}
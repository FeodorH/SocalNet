package com.example.socalnet.domain.vaildation

import com.example.socalnet.core.common.AppError
import com.example.socalnet.core.common.ValidationRules

object PostValidator {
    fun validate(
        text: String,
        imageCount: Int) : AppError.Validation? =
        when{
            text.isBlank() ->
                AppError.Validation(AppError.Validation.Field.TEXT, "Post can't been blank!")
            text.length > ValidationRules.MAX_POST_TEXT ->
                AppError.Validation(AppError.Validation.Field.TEXT, "Maximum post's length is ${ValidationRules.MAX_POST_TEXT}!")
            imageCount > ValidationRules.MAX_POST_IMAGES ->
                AppError.Validation(AppError.Validation.Field.IMAGES, "No more ${ValidationRules.MAX_POST_IMAGES} images!")
            else ->
                null
        }
}
package com.example.socalnet.domain.vaildation

import com.example.socalnet.core.common.AppError
import com.example.socalnet.core.common.ValidationRules

object AuthValidator {
    fun validateEmail(email: String) : AppError.Validation? =
        when{
            email.isBlank() ->
                return AppError.Validation(AppError.Validation.Field.EMAIL, "Email не может быть пустым")
            !email.contains('@')||email.length > ValidationRules.MAX_EMAIL ->
                return AppError.Validation(AppError.Validation.Field.EMAIL, "Некорректный email")
            else -> null
        }

    fun validatePassword(password: String): AppError.Validation? = when {
        password.length < ValidationRules.MIN_PASSWORD -> AppError.Validation(
            AppError.Validation.Field.PASSWORD,
            "Пароль должен быть не короче ${ValidationRules.MIN_PASSWORD} символов",
        )
        password.length > ValidationRules.MAX_PASSWORD -> AppError.Validation(
            AppError.Validation.Field.PASSWORD,
            "Пароль должен быть не длиннее ${ValidationRules.MAX_PASSWORD} символов",
        )
        else -> null
    }

    fun validateName(name: String) : AppError.Validation? =
        when{
            name.isBlank() ->
                return AppError.Validation(AppError.Validation.Field.NAME, "Имя не может быть пустым")
            name.length > ValidationRules.MAX_NAME->
                return AppError.Validation(AppError.Validation.Field.NAME, "Максимальная длина имени: ${ValidationRules.MAX_NAME}")
            else -> null
        }
}
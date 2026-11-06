package com.example.socalnet.domain.vaildation

import com.example.socalnet.core.common.AppError
import com.example.socalnet.core.common.ValidationRules

object AuthValidator {
    fun validateEmail(email: String) : AppError.Validation?{
        when{
            email.isBlank() ->
                return AppError.Validation(AppError.Validation.Field.EMAIL, "Email can't been blank!")
            !email.contains('@')||email.length > ValidationRules.MAX_EMAIL ->
                return AppError.Validation(AppError.Validation.Field.EMAIL, "Incorrect email!")
        }
        return null
    }

    fun validatePassword(password: String) : AppError.Validation?{
        when{
            password.length < ValidationRules.MIN_PASSWORD ->
                return AppError.Validation(AppError.Validation.Field.PASSWORD, "Password need to been > than ${ValidationRules.MIN_PASSWORD} chars!")
            password.length > ValidationRules.MAX_PASSWORD ->
                return AppError.Validation(AppError.Validation.Field.PASSWORD, "Password need to be < than ${ValidationRules.MAX_PASSWORD} chars!")
        }
        return null
    }

    fun validateName(name: String) : AppError.Validation?{
        when{
            name.isBlank() ->
                return AppError.Validation(AppError.Validation.Field.NAME, "Name can't been blank!")
            name.length > ValidationRules.MAX_NAME->
                return AppError.Validation(AppError.Validation.Field.NAME, "Name need to be < than ${ValidationRules.MAX_NAME} chars!")
        }
        return null
    }
}
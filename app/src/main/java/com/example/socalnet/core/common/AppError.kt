package com.example.socalnet.core.common

sealed class AppError(message: String? = null, cause: Throwable? = null) : Exception(message, cause) {

    data object Network : AppError("Нет соединения с интернетом")

    data object Unauthorized : AppError("Требуется повторный вход")

    data object NotFound : AppError("Данные не найдены")

    data class Validation(val field: Field, val reason: String) : AppError("Некорректные данные: $field") {
        enum class Field { EMAIL, PASSWORD, NAME, TEXT, IMAGES }
    }

    data class Server(val code: String? = null, val details: String? = null) :
        AppError("Ошибка сервера${code?.let { " ($it)" } ?: ""}")

    data class Unknown(override val cause: Throwable? = null) : AppError("Неизвестная ошибка", cause)
}
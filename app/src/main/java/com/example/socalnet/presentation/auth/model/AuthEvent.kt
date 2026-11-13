package com.example.socalnet.presentation.auth.model

sealed interface AuthEvent {
    /** Письмо сброса пароля отправлено — показать snackbar и вернуться на логин. */
    data object PasswordResetSent : AuthEvent
}
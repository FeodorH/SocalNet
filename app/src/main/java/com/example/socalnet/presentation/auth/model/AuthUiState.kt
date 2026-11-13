package com.example.socalnet.presentation.auth.model

data class AuthUiState(
    val email: String = "",
    val password: String = "",
    val name: String = "",
    val isLoading: Boolean = false,

    val emailError: String? = null,
    val passwordError: String? = null,
    val nameError: String? = null,

    val generalError: String? = null,
) {
    val canSubmitLogin: Boolean
        get() = !isLoading && email.isNotBlank() && password.isNotBlank()

    val canSubmitRegister: Boolean
        get() = !isLoading && name.isNotBlank() && email.isNotBlank() && password.isNotBlank()

    val canSubmitReset: Boolean
        get() = !isLoading && email.isNotBlank()
}
package com.example.socalnet.presentation.auth.preview

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import com.example.socalnet.presentation.auth.LoginScreen
import com.example.socalnet.presentation.auth.RegisterScreen
import com.example.socalnet.presentation.auth.ResetPasswordScreen
import com.example.socalnet.presentation.auth.model.AuthUiState

@Preview(showBackground = true, heightDp = 700, name = "Login · пусто")
@Composable
private fun LoginEmptyPreview() {
    MaterialTheme {
        LoginScreen(
            state = AuthUiState(),
            onEmailChange = {}, onPasswordChange = {},
            onSubmit = {}, onForgotPassword = {}, onGoToRegister = {}, onBack = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 700, name = "Login · ошибка")
@Composable
private fun LoginErrorPreview() {
    MaterialTheme {
        LoginScreen(
            state = AuthUiState(
                email = "user@test.com",
                password = "wrong",
                generalError = "Неверный email или пароль",
            ),
            onEmailChange = {}, onPasswordChange = {},
            onSubmit = {}, onForgotPassword = {}, onGoToRegister = {}, onBack = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 700, name = "Login · загрузка")
@Composable
private fun LoginLoadingPreview() {
    MaterialTheme {
        LoginScreen(
            state = AuthUiState(email = "a@b.com", password = "12345678", isLoading = true),
            onEmailChange = {}, onPasswordChange = {},
            onSubmit = {}, onForgotPassword = {}, onGoToRegister = {}, onBack = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 800, name = "Register · пусто")
@Composable
private fun RegisterEmptyPreview() {
    MaterialTheme {
        RegisterScreen(
            state = AuthUiState(),
            onNameChange = {}, onEmailChange = {}, onPasswordChange = {},
            onSubmit = {}, onGoToLogin = {}, onBack = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 800, name = "Register · ошибки полей")
@Composable
private fun RegisterErrorPreview() {
    MaterialTheme {
        RegisterScreen(
            state = AuthUiState(
                name = "A",
                email = "not-an-email",
                password = "123",
                nameError = "Минимум 2 символа",
                emailError = "Некорректный email",
                passwordError = "Минимум 8 символов",
            ),
            onNameChange = {}, onEmailChange = {}, onPasswordChange = {},
            onSubmit = {}, onGoToLogin = {}, onBack = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 700, name = "Reset · пусто")
@Composable
private fun ResetEmptyPreview() {
    MaterialTheme {
        ResetPasswordScreen(
            state = AuthUiState(),
            onEmailChange = {}, onSubmit = {}, onBack = {},
        )
    }
}

@Preview(showBackground = true, heightDp = 700, name = "Reset · с email")
@Composable
private fun ResetFilledPreview() {
    MaterialTheme {
        ResetPasswordScreen(
            state = AuthUiState(email = "user@test.com"),
            onEmailChange = {}, onSubmit = {}, onBack = {},
        )
    }
}
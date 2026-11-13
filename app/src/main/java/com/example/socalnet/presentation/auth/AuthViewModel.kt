package com.example.socalnet.presentation.auth

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.socalnet.core.common.AppError
import com.example.socalnet.core.common.AppResult
import com.example.socalnet.domain.usecase.LoginUseCase
import com.example.socalnet.domain.usecase.RegisterUseCases
import com.example.socalnet.domain.usecase.ResetPasswordUseCase
import com.example.socalnet.presentation.auth.model.AuthEvent
import com.example.socalnet.presentation.auth.model.AuthUiState
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class AuthViewModel @Inject constructor(
    private val loginUseCase: LoginUseCase,
    private val registerUseCase: RegisterUseCases,
    private val resetPasswordUseCase: ResetPasswordUseCase,
) : ViewModel() {

    private val _state = MutableStateFlow(AuthUiState())
    val state: StateFlow<AuthUiState> = _state.asStateFlow()

    private val _events = Channel<AuthEvent>(Channel.BUFFERED)
    val events: Flow<AuthEvent> = _events.receiveAsFlow()

    fun onEmailChange(email: String) {
        _state.update { it.copy(email = email, emailError = null, generalError = null) }
    }

    fun onPasswordChange(password: String) {
        _state.update { it.copy(password = password, passwordError = null, generalError = null) }
    }

    fun onNameChange(name: String) {
        _state.update { it.copy(name = name, nameError = null, generalError = null) }
    }

    fun submitLogin() {
        val s = _state.value
        if (!s.canSubmitLogin) return

        viewModelScope.launch {
            _state.update {
                it.copy(isLoading = true, emailError = null, passwordError = null, generalError = null)
            }

            when (val r = loginUseCase(s.email, s.password)) {
                is AppResult.Success -> {
                    // Экран уничтожится автоматически при смене authState.
                    // isLoading не сбрасываем — чтобы кнопка не нажалась повторно
                    // в эти 100–300 мс до переключения графа.
                }
                is AppResult.Failure -> applyError(r.error)
            }
        }
    }

    fun submitRegister() {
        val s = _state.value
        if (!s.canSubmitRegister) return

        viewModelScope.launch {
            _state.update {
                it.copy(isLoading = true, emailError = null, passwordError = null, nameError = null, generalError = null)
            }

            when (val r = registerUseCase(s.email, s.password, s.name)) {
                is AppResult.Success -> {
                    // экран уничтожится сам
                }
                is AppResult.Failure -> applyError(r.error)
            }
        }
    }

    fun submitReset() {
        val s = _state.value
        if (!s.canSubmitReset) return

        viewModelScope.launch {
            _state.update {
                it.copy(isLoading = true, emailError = null, generalError = null)
            }

            when (val r = resetPasswordUseCase(s.email)) {
                is AppResult.Success -> {
                    _state.update { it.copy(isLoading = false) }
                    _events.send(AuthEvent.PasswordResetSent)
                }
                is AppResult.Failure -> applyError(r.error)
            }
        }
    }

    private fun applyError(error: AppError) {
        _state.update { current ->
            val next = when (error) {
                is AppError.Validation -> when (error.field) {
                    AppError.Validation.Field.EMAIL -> current.copy(emailError = error.reason)
                    AppError.Validation.Field.PASSWORD -> current.copy(passwordError = error.reason)
                    AppError.Validation.Field.NAME -> current.copy(nameError = error.reason)
                    else -> current.copy(generalError = error.reason)
                }
                AppError.Unauthorized -> current.copy(generalError = "Неверный email или пароль")
                AppError.Network -> current.copy(generalError = "Нет подключения к интернету")
                is AppError.Server -> current.copy(generalError = "Ошибка сервера. Попробуйте позже")
                else -> current.copy(generalError = "Что-то пошло не так")
            }
            next.copy(isLoading = false)
        }
    }
}
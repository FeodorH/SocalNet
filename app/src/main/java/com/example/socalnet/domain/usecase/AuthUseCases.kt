package com.example.socalnet.domain.usecase

import com.example.socalnet.core.common.AppResult
import com.example.socalnet.core.model.User
import com.example.socalnet.domain.repository.AuthRepository
import com.example.socalnet.domain.vaildation.AuthValidator

class RegisterUseCases(private val auth: AuthRepository) {
    suspend operator fun invoke(email: String, password: String, name:String) : AppResult<User>{
        AuthValidator.validateEmail(email)?.let { return AppResult.Failure(it) }
        AuthValidator.validatePassword(password)?.let { return AppResult.Failure(it) }
        AuthValidator.validateName(name)?.let { return AppResult.Failure(it) }
        return auth.register(email.trim(),password,name.trim())
    }
}

class LoginUseCase(private val auth: AuthRepository){
    suspend operator fun invoke(email: String, password: String): AppResult<User>{
        AuthValidator.validateEmail(email)?.let { return AppResult.Failure(it) }
        AuthValidator.validatePassword(password)?.let{ return AppResult.Failure(it) }
        return auth.login(email.trim(),password)
    }
}

class ResetPasswordUseCase(private val auth: AuthRepository){
    suspend operator fun invoke(email: String): AppResult<Unit>{
        AuthValidator.validateEmail(email)?.let { return AppResult.Failure(it) }
        return auth.sendPasswordReset(email.trim())
    }
}

class LogoutUseCase(private val auth: AuthRepository){
    suspend operator fun invoke() : AppResult<Unit>{
        return auth.logout()
    }
}
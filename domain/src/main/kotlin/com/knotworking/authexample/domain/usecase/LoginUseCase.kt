package com.knotworking.authexample.domain.usecase

import com.knotworking.authexample.domain.AppResult
import com.knotworking.authexample.domain.model.AuthSession
import com.knotworking.authexample.domain.model.Credentials
import com.knotworking.authexample.domain.repository.AuthRepository

class LoginUseCase(private val authRepository: AuthRepository) {
    suspend operator fun invoke(credentials: Credentials): AppResult<AuthSession> =
        authRepository.login(credentials)
}

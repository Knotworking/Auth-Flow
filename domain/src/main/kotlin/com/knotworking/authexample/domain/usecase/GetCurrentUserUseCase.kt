package com.knotworking.authexample.domain.usecase

import com.knotworking.authexample.domain.model.AuthState
import com.knotworking.authexample.domain.model.User
import com.knotworking.authexample.domain.repository.AuthStateObserver

class GetCurrentUserUseCase(private val authStateObserver: AuthStateObserver) {
    operator fun invoke(): User? {
        val state = authStateObserver.authState.value
        return if (state is AuthState.Authenticated) state.user else null
    }
}

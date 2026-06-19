package com.knotworking.authexample.domain.usecase

import com.knotworking.authexample.domain.model.AuthState
import com.knotworking.authexample.domain.repository.AuthStateObserver
import kotlinx.coroutines.flow.StateFlow

class ObserveAuthStateUseCase(private val authStateObserver: AuthStateObserver) {
    operator fun invoke(): StateFlow<AuthState> = authStateObserver.authState
}

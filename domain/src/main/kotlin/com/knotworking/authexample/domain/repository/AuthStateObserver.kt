package com.knotworking.authexample.domain.repository

import com.knotworking.authexample.domain.model.AuthState
import kotlinx.coroutines.flow.StateFlow

interface AuthStateObserver {
    val authState: StateFlow<AuthState>
    suspend fun forceLogout()
}

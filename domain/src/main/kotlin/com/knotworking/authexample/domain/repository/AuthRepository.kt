package com.knotworking.authexample.domain.repository

import com.knotworking.authexample.domain.AppResult
import com.knotworking.authexample.domain.model.AuthSession
import com.knotworking.authexample.domain.model.Credentials

interface AuthRepository {
    suspend fun login(credentials: Credentials): AppResult<AuthSession>
    suspend fun logout(): AppResult<Unit>
    suspend fun currentSession(): AuthSession?
}

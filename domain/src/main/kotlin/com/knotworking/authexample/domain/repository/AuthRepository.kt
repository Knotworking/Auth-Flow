package com.knotworking.authexample.domain.repository

import com.knotworking.authexample.domain.Result
import com.knotworking.authexample.domain.model.AuthSession
import com.knotworking.authexample.domain.model.Credentials

interface AuthRepository {
    suspend fun login(credentials: Credentials): Result<AuthSession>
    suspend fun logout(): Result<Unit>
    suspend fun currentSession(): AuthSession?
}

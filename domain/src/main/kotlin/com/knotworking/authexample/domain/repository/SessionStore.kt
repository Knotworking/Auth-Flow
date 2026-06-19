package com.knotworking.authexample.domain.repository

import com.knotworking.authexample.domain.model.AuthSession
import kotlinx.coroutines.flow.Flow

interface SessionStore {
    val sessionFlow: Flow<AuthSession?>
    suspend fun save(session: AuthSession)
    suspend fun read(): AuthSession?
    suspend fun clear()
}

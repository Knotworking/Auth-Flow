package com.knotworking.authexample.data.repository

import com.knotworking.authexample.data.network.api.AuthApi
import com.knotworking.authexample.data.network.dto.LoginRequestDto
import com.knotworking.authexample.data.network.mapper.toAuthSession
import com.knotworking.authexample.data.network.util.safeApiCall
import com.knotworking.authexample.domain.AppError
import com.knotworking.authexample.domain.AppResult
import com.knotworking.authexample.core.Logger
import com.knotworking.authexample.domain.model.AuthSession
import com.knotworking.authexample.domain.model.Credentials
import com.knotworking.authexample.domain.repository.AuthRepository
import com.knotworking.authexample.domain.repository.SessionStore

class AuthRepositoryImpl(
    private val authApi: AuthApi,
    private val sessionStore: SessionStore,
    private val logger: Logger,
) : AuthRepository {

    override suspend fun login(credentials: Credentials): AppResult<AuthSession> =
        safeApiCall {
            val response = authApi.login(LoginRequestDto(credentials.username, credentials.password))
            val session = response.toAuthSession(credentials.username)
            sessionStore.save(session)
            logger.i(TAG, "Login successful for ${credentials.username}")
            session
        }

    override suspend fun logout(): AppResult<Unit> = try {
        sessionStore.clear()
        logger.i(TAG, "Logout: session cleared")
        AppResult.Success(Unit)
    } catch (e: Exception) {
        logger.e(TAG, "Logout failed", e)
        AppResult.Failure(AppError.Unknown)
    }

    override suspend fun currentSession(): AuthSession? = sessionStore.read()

    companion object {
        private const val TAG = "AuthRepositoryImpl"
    }
}

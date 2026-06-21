package com.knotworking.authexample.data.network.authenticator

import com.knotworking.authexample.data.network.api.AuthApi
import com.knotworking.authexample.data.network.dto.RefreshRequestDto
import com.knotworking.authexample.data.network.mapper.withRefreshedToken
import com.knotworking.authexample.core.Logger
import com.knotworking.authexample.domain.repository.AuthStateObserver
import com.knotworking.authexample.domain.repository.SessionStore
import kotlinx.coroutines.runBlocking
import okhttp3.Authenticator
import okhttp3.Request
import okhttp3.Response
import okhttp3.Route

class TokenAuthenticator(
    private val authApi: AuthApi,
    private val sessionStore: SessionStore,
    private val authStateObserver: AuthStateObserver,
    private val logger: Logger,
) : Authenticator {

    // @Synchronized ensures only one thread refreshes at a time (single-flight).
    @Synchronized
    override fun authenticate(route: Route?, response: Response): Request? {
        val currentSession = runBlocking { sessionStore.read() }
        val failedToken = response.request.header("Authorization")?.removePrefix("Bearer ")

        // If another thread already refreshed the token, retry immediately with the new one.
        if (failedToken != null && failedToken != currentSession?.accessToken) {
            logger.d(TAG, "Token already refreshed by another thread — retrying")
            return currentSession?.let {
                response.request.newBuilder()
                    .header("Authorization", "Bearer ${it.accessToken}")
                    .build()
            }
        }

        val session = currentSession ?: run {
            logger.w(TAG, "401 with no active session — cannot refresh")
            return null
        }

        logger.i(TAG, "401 → attempting refresh for ${session.username}")

        return try {
            val refreshResponse = runBlocking {
                authApi.refresh(RefreshRequestDto(session.refreshToken))
            }
            val newSession = session.withRefreshedToken(refreshResponse)
            runBlocking { sessionStore.save(newSession) }
            logger.i(TAG, "Refresh succeeded for ${session.username}")
            response.request.newBuilder()
                .header("Authorization", "Bearer ${newSession.accessToken}")
                .build()
        } catch (e: Exception) {
            logger.e(TAG, "Refresh failed: ${e.message}", e)
            runBlocking {
                sessionStore.clear()
                authStateObserver.forceLogout()
            }
            logger.i(TAG, "Forced logout after refresh failure")
            null
        }
    }

    companion object {
        private const val TAG = "TokenAuthenticator"
    }
}

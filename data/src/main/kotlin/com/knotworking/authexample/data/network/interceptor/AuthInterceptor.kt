package com.knotworking.authexample.data.network.interceptor

import com.knotworking.authexample.domain.Logger
import com.knotworking.authexample.domain.repository.SessionStore
import kotlinx.coroutines.runBlocking
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(
    private val sessionStore: SessionStore,
    private val logger: Logger,
) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val session = runBlocking { sessionStore.read() }
        val request = if (session != null) {
            logger.d(TAG, "Attaching Bearer token for ${session.username}")
            chain.request().newBuilder()
                .header("Authorization", "Bearer ${session.accessToken}")
                .build()
        } else {
            chain.request()
        }
        return chain.proceed(request)
    }

    companion object {
        private const val TAG = "AuthInterceptor"
    }
}

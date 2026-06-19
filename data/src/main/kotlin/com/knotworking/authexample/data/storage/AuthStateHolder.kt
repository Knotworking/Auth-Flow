package com.knotworking.authexample.data.storage

import com.knotworking.authexample.domain.Logger
import com.knotworking.authexample.domain.model.AuthState
import com.knotworking.authexample.domain.model.User
import com.knotworking.authexample.domain.repository.AuthStateObserver
import com.knotworking.authexample.domain.repository.SessionStore
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

class AuthStateHolder(
    private val sessionStore: SessionStore,
    private val logger: Logger,
) : AuthStateObserver {

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override val authState: StateFlow<AuthState> = sessionStore.sessionFlow
        .map { session ->
            if (session != null) {
                AuthState.Authenticated(User(session.username))
            } else {
                AuthState.Unauthenticated
            }
        }
        .stateIn(
            scope = scope,
            started = SharingStarted.Eagerly,
            initialValue = AuthState.Unknown,
        )

    override suspend fun forceLogout() {
        logger.i(TAG, "Force logout — clearing session")
        sessionStore.clear()
    }

    companion object {
        private const val TAG = "AuthStateHolder"
    }
}

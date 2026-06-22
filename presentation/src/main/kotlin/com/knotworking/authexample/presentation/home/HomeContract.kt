package com.knotworking.authexample.presentation.home

import java.time.Instant

object HomeContract {

    data class State(
        val username: String = "",
        val accessToken: String = "",
        val accessExpiresAt: Instant? = null,
        val operationResult: String? = null,
        val isOperationLoading: Boolean = false,
        val error: String? = null,
    )

    sealed interface Intent {
        data object PerformAuthOperation : Intent
        data object Logout : Intent
    }
}

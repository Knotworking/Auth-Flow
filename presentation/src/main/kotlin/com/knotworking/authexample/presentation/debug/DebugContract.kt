package com.knotworking.authexample.presentation.debug

import com.knotworking.authexample.domain.model.AuthSession
import com.knotworking.authexample.domain.model.Credentials
import com.knotworking.authexample.domain.model.TokenInfo

object DebugContract {

    data class State(
        val users: List<Credentials> = emptyList(),
        val tokens: List<TokenInfo> = emptyList(),
        val currentSession: AuthSession? = null,
        val isLoading: Boolean = false,
        val newUsername: String = "",
        val newPassword: String = "",
        val error: String? = null,
    )

    sealed interface Intent {
        data object Refresh : Intent
        data class UpdateNewUsername(val value: String) : Intent
        data class UpdateNewPassword(val value: String) : Intent
        data object AddUser : Intent
        data class RemoveUser(val username: String) : Intent
        data class RevokeToken(val token: String) : Intent
    }

    sealed interface Effect
}

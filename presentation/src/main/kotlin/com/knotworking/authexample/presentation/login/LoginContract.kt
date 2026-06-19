package com.knotworking.authexample.presentation.login

object LoginContract {

    data class State(
        val usernameInput: String = "",
        val passwordInput: String = "",
        val isLoading: Boolean = false,
        val error: String? = null,
    )

    sealed interface Intent {
        data class UpdateUsername(val value: String) : Intent
        data class UpdatePassword(val value: String) : Intent
        data object Submit : Intent
        data object DebugClicked : Intent
    }

    sealed interface Effect {
        data object NavigateToDebug : Effect
    }
}

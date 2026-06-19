package com.knotworking.authexample.presentation.debug

import androidx.lifecycle.viewModelScope
import com.knotworking.authexample.domain.AppResult
import com.knotworking.authexample.domain.model.Credentials
import com.knotworking.authexample.domain.repository.SessionStore
import com.knotworking.authexample.domain.usecase.AddUserUseCase
import com.knotworking.authexample.domain.usecase.GetTokensUseCase
import com.knotworking.authexample.domain.usecase.GetUsersUseCase
import com.knotworking.authexample.domain.usecase.RemoveUserUseCase
import com.knotworking.authexample.domain.usecase.RevokeTokenUseCase
import com.knotworking.authexample.presentation.mvi.BaseMviViewModel
import kotlinx.coroutines.launch

class DebugViewModel(
    private val getUsers: GetUsersUseCase,
    private val addUser: AddUserUseCase,
    private val removeUser: RemoveUserUseCase,
    private val getTokens: GetTokensUseCase,
    private val revokeToken: RevokeTokenUseCase,
    private val sessionStore: SessionStore,
) : BaseMviViewModel<DebugContract.State, DebugContract.Intent, DebugContract.Effect>(
    DebugContract.State()
) {

    init {
        load()
    }

    override fun onIntent(intent: DebugContract.Intent) {
        when (intent) {
            DebugContract.Intent.Refresh -> load()
            is DebugContract.Intent.UpdateNewUsername -> updateState { copy(newUsername = intent.value) }
            is DebugContract.Intent.UpdateNewPassword -> updateState { copy(newPassword = intent.value) }
            DebugContract.Intent.AddUser -> handleAddUser()
            is DebugContract.Intent.RemoveUser -> handleRemoveUser(intent.username)
            is DebugContract.Intent.RevokeToken -> handleRevokeToken(intent.token)
        }
    }

    private fun load() {
        viewModelScope.launch {
            updateState { copy(isLoading = true, error = null) }
            val users = when (val result = getUsers()) {
                is AppResult.Success -> result.data
                is AppResult.Failure -> emptyList()
            }
            val tokens = when (val result = getTokens()) {
                is AppResult.Success -> result.data
                is AppResult.Failure -> emptyList()
            }
            val session = sessionStore.read()
            updateState { copy(isLoading = false, users = users, tokens = tokens, currentSession = session) }
        }
    }

    private fun handleAddUser() {
        val s = state.value
        if (s.newUsername.isBlank() || s.newPassword.isBlank()) return
        viewModelScope.launch {
            when (addUser(Credentials(s.newUsername, s.newPassword))) {
                is AppResult.Success -> {
                    updateState { copy(newUsername = "", newPassword = "") }
                    load()
                }
                is AppResult.Failure -> updateState { copy(error = "Failed to add user") }
            }
        }
    }

    private fun handleRemoveUser(username: String) {
        viewModelScope.launch {
            when (removeUser(username)) {
                is AppResult.Success -> load()
                is AppResult.Failure -> updateState { copy(error = "Failed to remove user") }
            }
        }
    }

    private fun handleRevokeToken(token: String) {
        viewModelScope.launch {
            when (revokeToken(token)) {
                is AppResult.Success -> load()
                is AppResult.Failure -> updateState { copy(error = "Failed to revoke token") }
            }
        }
    }
}

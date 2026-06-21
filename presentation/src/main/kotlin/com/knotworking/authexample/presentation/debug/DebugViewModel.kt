package com.knotworking.authexample.presentation.debug

import androidx.lifecycle.viewModelScope
import com.knotworking.authexample.domain.AppResult
import com.knotworking.authexample.domain.model.Credentials
import com.knotworking.authexample.domain.repository.SessionStore
import com.knotworking.authexample.domain.usecase.AddUserUseCase
import com.knotworking.authexample.domain.usecase.GetTokensUseCase
import com.knotworking.authexample.domain.usecase.GetUsersUseCase
import com.knotworking.authexample.domain.usecase.ObserveTokensUseCase
import com.knotworking.authexample.domain.usecase.ObserveUsersUseCase
import com.knotworking.authexample.domain.usecase.RemoveUserUseCase
import com.knotworking.authexample.domain.usecase.RevokeTokenUseCase
import com.knotworking.authexample.presentation.mvi.BaseMviViewModel
import kotlinx.coroutines.coroutineScope
import kotlinx.coroutines.launch

class DebugViewModel(
    private val getUsers: GetUsersUseCase,
    private val observeUsers: ObserveUsersUseCase,
    private val addUser: AddUserUseCase,
    private val removeUser: RemoveUserUseCase,
    private val getTokens: GetTokensUseCase,
    private val observeTokens: ObserveTokensUseCase,
    private val revokeToken: RevokeTokenUseCase,
    private val sessionStore: SessionStore,
) : BaseMviViewModel<DebugContract.State, DebugContract.Intent, DebugContract.Effect>(
    DebugContract.State()
) {

    init {
        viewModelScope.launch {
            observeUsers().collect { result ->
                updateState {
                    copy(users = when (result) {
                        is AppResult.Success -> result.data
                        is AppResult.Failure -> emptyList()
                    })
                }
            }
        }
        viewModelScope.launch {
            observeTokens().collect { result ->
                updateState {
                    copy(tokens = when (result) {
                        is AppResult.Success -> result.data
                        is AppResult.Failure -> emptyList()
                    })
                }
            }
        }
        viewModelScope.launch {
            sessionStore.sessionFlow.collect { session ->
                updateState { copy(currentSession = session) }
            }
        }
        fetch()
    }

    override fun onIntent(intent: DebugContract.Intent) {
        when (intent) {
            DebugContract.Intent.Refresh -> fetch()
            is DebugContract.Intent.UpdateNewUsername -> updateState { copy(newUsername = intent.value) }
            is DebugContract.Intent.UpdateNewPassword -> updateState { copy(newPassword = intent.value) }
            DebugContract.Intent.AddUser -> handleAddUser()
            is DebugContract.Intent.RemoveUser -> handleRemoveUser(intent.username)
            is DebugContract.Intent.RevokeToken -> handleRevokeToken(intent.token)
        }
    }

    private fun fetch() {
        viewModelScope.launch {
            updateState { copy(isLoading = true, error = null) }
            coroutineScope {
                launch { getUsers() }
                launch { getTokens() }
            }
            updateState { copy(isLoading = false) }
        }
    }

    private fun handleAddUser() {
        val s = state.value
        if (s.newUsername.isBlank() || s.newPassword.isBlank()) return
        viewModelScope.launch {
            when (addUser(Credentials(s.newUsername, s.newPassword))) {
                is AppResult.Success -> updateState { copy(newUsername = "", newPassword = "") }
                is AppResult.Failure -> updateState { copy(error = "Failed to add user") }
            }
        }
    }

    private fun handleRemoveUser(username: String) {
        viewModelScope.launch {
            when (removeUser(username)) {
                is AppResult.Success -> Unit
                is AppResult.Failure -> updateState { copy(error = "Failed to remove user") }
            }
        }
    }

    private fun handleRevokeToken(token: String) {
        viewModelScope.launch {
            when (revokeToken(token)) {
                is AppResult.Success -> Unit
                is AppResult.Failure -> updateState { copy(error = "Failed to revoke token") }
            }
        }
    }
}

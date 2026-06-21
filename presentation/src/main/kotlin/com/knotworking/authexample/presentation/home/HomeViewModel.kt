package com.knotworking.authexample.presentation.home

import androidx.lifecycle.viewModelScope
import com.knotworking.authexample.domain.AppError
import com.knotworking.authexample.domain.Result
import com.knotworking.authexample.domain.repository.SessionStore
import com.knotworking.authexample.domain.usecase.GetCurrentUserUseCase
import com.knotworking.authexample.domain.usecase.LogoutUseCase
import com.knotworking.authexample.domain.usecase.PerformProtectedOperationUseCase
import com.knotworking.authexample.presentation.mvi.BaseMviViewModel
import kotlinx.coroutines.launch

class HomeViewModel(
    private val performProtectedOperation: PerformProtectedOperationUseCase,
    private val logout: LogoutUseCase,
    private val getCurrentUser: GetCurrentUserUseCase,
    private val sessionStore: SessionStore,
) : BaseMviViewModel<HomeContract.State, HomeContract.Intent, HomeContract.Effect>(
    HomeContract.State()
) {

    init {
        observeSession()
    }

    override fun onIntent(intent: HomeContract.Intent) {
        when (intent) {
            HomeContract.Intent.PerformAuthOperation -> handlePerformOperation()
            HomeContract.Intent.Logout -> handleLogout()
            HomeContract.Intent.NavigateToDebug -> sendEffect(HomeContract.Effect.NavigateToDebug)
        }
    }

    private fun observeSession() {
        viewModelScope.launch {
            sessionStore.sessionFlow.collect { session ->
                val username = getCurrentUser()?.username ?: ""
                updateState {
                    copy(
                        username = username,
                        accessToken = session?.accessToken ?: "",
                        accessExpiresAt = session?.accessExpiresAt,
                    )
                }
            }
        }
    }

    private fun handlePerformOperation() {
        if (state.value.isOperationLoading) return
        updateState { copy(isOperationLoading = true, error = null, operationResult = null) }
        viewModelScope.launch {
            when (val result = performProtectedOperation()) {
                is Result.Success -> updateState { copy(isOperationLoading = false, operationResult = result.data) }
                is Result.Failure -> {
                    val message = when (result.error) {
                        AppError.Auth.Unauthorized,
                        AppError.Auth.SessionExpired,
                        AppError.Auth.RefreshFailed -> "Session expired — forced logout"
                        AppError.Auth.Forbidden -> "Access forbidden"
                        AppError.Network.NoConnectivity -> "No internet connection"
                        AppError.Network.Timeout -> "Request timed out"
                        else -> "Operation failed. Please try again."
                    }
                    updateState { copy(isOperationLoading = false, error = message) }
                }
            }
        }
    }

    private fun handleLogout() {
        viewModelScope.launch {
            logout() // AuthState flip drives navigation — no Effect needed
        }
    }
}

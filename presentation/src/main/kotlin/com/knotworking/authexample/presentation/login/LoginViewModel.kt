package com.knotworking.authexample.presentation.login

import androidx.lifecycle.viewModelScope
import com.knotworking.authexample.domain.AppError
import com.knotworking.authexample.domain.Result
import com.knotworking.authexample.domain.model.Credentials
import com.knotworking.authexample.domain.usecase.LoginUseCase
import com.knotworking.authexample.presentation.mvi.BaseMviViewModel
import kotlinx.coroutines.launch

class LoginViewModel(
    private val login: LoginUseCase,
) : BaseMviViewModel<LoginContract.State, LoginContract.Intent>(
    LoginContract.State()
) {

    override fun onIntent(intent: LoginContract.Intent) {
        when (intent) {
            is LoginContract.Intent.UpdateUsername -> updateState { copy(usernameInput = intent.value) }
            is LoginContract.Intent.UpdatePassword -> updateState { copy(passwordInput = intent.value) }
            LoginContract.Intent.Submit -> handleSubmit()
        }
    }

    private fun handleSubmit() {
        val current = state.value
        if (current.isLoading) return
        updateState { copy(isLoading = true, error = null) }
        viewModelScope.launch {
            when (val result = login(Credentials(current.usernameInput, current.passwordInput))) {
                is Result.Success -> updateState { copy(isLoading = false) }
                is Result.Failure -> {
                    val message = when (result.error) {
                        AppError.Auth.Unauthorized,
                        AppError.Auth.InvalidCredentials -> "Invalid username or password"
                        AppError.Network.NoConnectivity -> "No internet connection"
                        AppError.Network.Timeout -> "Request timed out"
                        else -> "Login failed. Please try again."
                    }
                    updateState { copy(isLoading = false, error = message) }
                }
            }
        }
    }
}

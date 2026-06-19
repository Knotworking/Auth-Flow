package com.knotworking.authexample.presentation

import androidx.lifecycle.ViewModel
import com.knotworking.authexample.domain.model.AuthState
import com.knotworking.authexample.domain.usecase.ObserveAuthStateUseCase
import kotlinx.coroutines.flow.StateFlow

class AppViewModel(observeAuthState: ObserveAuthStateUseCase) : ViewModel() {
    val authState: StateFlow<AuthState> = observeAuthState()
}

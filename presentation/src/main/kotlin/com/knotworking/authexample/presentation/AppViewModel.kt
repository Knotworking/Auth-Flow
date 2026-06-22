package com.knotworking.authexample.presentation

import androidx.lifecycle.ViewModel
import com.knotworking.authexample.domain.model.AuthState
import com.knotworking.authexample.domain.usecase.GetAuthStateFlowUseCase
import kotlinx.coroutines.flow.StateFlow

class AppViewModel(observeAuthState: GetAuthStateFlowUseCase) : ViewModel() {
    val authState: StateFlow<AuthState> = observeAuthState()
}

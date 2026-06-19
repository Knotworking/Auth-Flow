package com.knotworking.authexample.domain.model

sealed interface AuthState {
    data class Authenticated(val user: User) : AuthState
    data object Unauthenticated : AuthState
    data object Unknown : AuthState
}

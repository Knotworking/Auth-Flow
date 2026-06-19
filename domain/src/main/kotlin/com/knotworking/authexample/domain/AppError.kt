package com.knotworking.authexample.domain

sealed interface AppError {
    sealed interface Network : AppError {
        data object NoConnectivity : Network
        data object Timeout : Network
        data object Unknown : Network
    }
    sealed interface Auth : AppError {
        data object InvalidCredentials : Auth
        data object SessionExpired : Auth
        data object RefreshFailed : Auth
        data object Unauthorized : Auth
        data object Forbidden : Auth
    }
    data class Server(val code: Int, val message: String) : AppError
    data object Unknown : AppError
}

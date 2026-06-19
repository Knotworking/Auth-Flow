package com.knotworking.authexample.data.network.util

import com.knotworking.authexample.domain.AppError
import com.knotworking.authexample.domain.AppResult
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

suspend fun <T> safeApiCall(call: suspend () -> T): AppResult<T> = try {
    AppResult.Success(call())
} catch (e: HttpException) {
    AppResult.Failure(e.toAppError())
} catch (e: SocketTimeoutException) {
    AppResult.Failure(AppError.Network.Timeout)
} catch (e: IOException) {
    AppResult.Failure(AppError.Network.Unknown)
} catch (e: Exception) {
    AppResult.Failure(AppError.Unknown)
}

private fun HttpException.toAppError(): AppError = when (code()) {
    401 -> AppError.Auth.Unauthorized
    403 -> AppError.Auth.Forbidden
    in 500..599 -> AppError.Server(code(), message())
    else -> AppError.Unknown
}

package com.knotworking.authexample.data.network.util

import com.knotworking.authexample.domain.AppError
import com.knotworking.authexample.domain.Result
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

suspend fun <T> safeApiCall(call: suspend () -> T): Result<T> = try {
    Result.Success(call())
} catch (e: HttpException) {
    Result.Failure(e.toAppError())
} catch (e: SocketTimeoutException) {
    Result.Failure(AppError.Network.Timeout)
} catch (e: IOException) {
    Result.Failure(AppError.Network.Unknown)
} catch (e: Exception) {
    Result.Failure(AppError.Unknown)
}

private fun HttpException.toAppError(): AppError = when (code()) {
    401 -> AppError.Auth.Unauthorized
    403 -> AppError.Auth.Forbidden
    in 500..599 -> AppError.Server(code(), message())
    else -> AppError.Unknown
}

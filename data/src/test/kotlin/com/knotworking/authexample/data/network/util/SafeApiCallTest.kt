package com.knotworking.authexample.data.network.util

import com.knotworking.authexample.domain.AppError
import com.knotworking.authexample.domain.Result
import kotlinx.coroutines.runBlocking
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.ResponseBody.Companion.toResponseBody
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test
import retrofit2.HttpException
import java.io.IOException
import java.net.SocketTimeoutException

class SafeApiCallTest {

    @Test
    fun `success value is wrapped in Result Success`() = runBlocking {
        val result = safeApiCall { "hello" }
        assertEquals(Result.Success("hello"), result)
    }

    @Test
    fun `HttpException 401 maps to Auth Unauthorized`() = runBlocking {
        val result = safeApiCall<String> { throw httpException(401) }
        assertEquals(Result.Failure(AppError.Auth.Unauthorized), result)
    }

    @Test
    fun `HttpException 403 maps to Auth Forbidden`() = runBlocking {
        val result = safeApiCall<String> { throw httpException(403) }
        assertEquals(Result.Failure(AppError.Auth.Forbidden), result)
    }

    @Test
    fun `HttpException 500 maps to Server error with code`() = runBlocking {
        val result = safeApiCall<String> { throw httpException(500) }
        assertTrue(result is Result.Failure && result.error is AppError.Server)
        assertEquals(500, (result as Result.Failure).error.let { (it as AppError.Server).code })
    }

    @Test
    fun `SocketTimeoutException maps to Network Timeout`() = runBlocking {
        val result = safeApiCall<String> { throw SocketTimeoutException() }
        assertEquals(Result.Failure(AppError.Network.Timeout), result)
    }

    @Test
    fun `IOException maps to Network Unknown`() = runBlocking {
        val result = safeApiCall<String> { throw IOException("network error") }
        assertEquals(Result.Failure(AppError.Network.Unknown), result)
    }

    @Test
    fun `unexpected exception maps to Unknown error`() = runBlocking {
        val result = safeApiCall<String> { throw IllegalStateException("unexpected") }
        assertEquals(Result.Failure(AppError.Unknown), result)
    }

    private fun httpException(code: Int): HttpException {
        val body = "{}".toResponseBody("application/json".toMediaType())
        return HttpException(retrofit2.Response.error<String>(code, body))
    }
}

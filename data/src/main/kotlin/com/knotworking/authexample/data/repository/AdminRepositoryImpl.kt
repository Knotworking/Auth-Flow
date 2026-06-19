package com.knotworking.authexample.data.repository

import com.knotworking.authexample.data.network.api.AuthApi
import com.knotworking.authexample.data.network.mapper.toDomain
import com.knotworking.authexample.data.network.mapper.toDto
import com.knotworking.authexample.data.network.util.safeApiCall
import com.knotworking.authexample.domain.AppResult
import com.knotworking.authexample.domain.Logger
import com.knotworking.authexample.domain.model.Credentials
import com.knotworking.authexample.domain.model.TokenInfo
import com.knotworking.authexample.domain.repository.AdminRepository
import retrofit2.HttpException

class AdminRepositoryImpl(
    private val authApi: AuthApi,
    private val logger: Logger,
) : AdminRepository {

    override suspend fun getUsers(): AppResult<List<Credentials>> =
        safeApiCall { authApi.getUsers().map { it.toDomain() } }

    override suspend fun addUser(credentials: Credentials): AppResult<Unit> =
        safeApiCall {
            val response = authApi.addUser(credentials.toDto())
            if (!response.isSuccessful) throw HttpException(response)
            logger.i(TAG, "User added: ${credentials.username}")
        }

    override suspend fun removeUser(username: String): AppResult<Unit> =
        safeApiCall {
            val response = authApi.removeUser(username)
            if (!response.isSuccessful) throw HttpException(response)
            logger.i(TAG, "User removed: $username")
        }

    override suspend fun getTokens(): AppResult<List<TokenInfo>> =
        safeApiCall { authApi.getTokens().map { it.toDomain() } }

    override suspend fun revokeToken(token: String): AppResult<Unit> =
        safeApiCall {
            val response = authApi.revokeToken(token)
            if (!response.isSuccessful) throw HttpException(response)
            logger.i(TAG, "Token revoked: ${token.take(8)}...")
        }

    companion object {
        private const val TAG = "AdminRepositoryImpl"
    }
}

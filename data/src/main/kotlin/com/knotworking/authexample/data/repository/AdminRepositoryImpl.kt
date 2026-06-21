package com.knotworking.authexample.data.repository

import com.knotworking.authexample.data.network.api.AuthApi
import com.knotworking.authexample.data.network.mapper.toDomain
import com.knotworking.authexample.data.network.mapper.toDto
import com.knotworking.authexample.data.network.util.safeApiCall
import com.knotworking.authexample.domain.AppResult
import com.knotworking.authexample.core.Logger
import com.knotworking.authexample.domain.model.Credentials
import com.knotworking.authexample.domain.model.TokenInfo
import com.knotworking.authexample.domain.repository.AdminRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import retrofit2.HttpException

class AdminRepositoryImpl(
    private val authApi: AuthApi,
    private val logger: Logger,
) : AdminRepository {

    private val _users = MutableStateFlow<AppResult<List<Credentials>>>(AppResult.Success(emptyList()))
    private val _tokens = MutableStateFlow<AppResult<List<TokenInfo>>>(AppResult.Success(emptyList()))

    override val usersFlow: Flow<AppResult<List<Credentials>>> = _users.asStateFlow()
    override val tokensFlow: Flow<AppResult<List<TokenInfo>>> = _tokens.asStateFlow()

    override suspend fun getUsers(): AppResult<List<Credentials>> {
        val result = safeApiCall { authApi.getUsers().map { it.toDomain() } }
        _users.value = result
        return result
    }

    override suspend fun addUser(credentials: Credentials): AppResult<Unit> {
        val result = safeApiCall {
            val response = authApi.addUser(credentials.toDto())
            if (!response.isSuccessful) throw HttpException(response)
            logger.i(TAG, "User added: ${credentials.username}")
        }
        if (result is AppResult.Success) getUsers()
        return result
    }

    override suspend fun removeUser(username: String): AppResult<Unit> {
        val result = safeApiCall {
            val response = authApi.removeUser(username)
            if (!response.isSuccessful) throw HttpException(response)
            logger.i(TAG, "User removed: $username")
        }
        if (result is AppResult.Success) getUsers()
        return result
    }

    override suspend fun getTokens(): AppResult<List<TokenInfo>> {
        val result = safeApiCall { authApi.getTokens().map { it.toDomain() } }
        _tokens.value = result
        return result
    }

    override suspend fun revokeToken(token: String): AppResult<Unit> {
        val result = safeApiCall {
            val response = authApi.revokeToken(token)
            if (!response.isSuccessful) throw HttpException(response)
            logger.i(TAG, "Token revoked: ${token.take(8)}...")
        }
        if (result is AppResult.Success) getTokens()
        return result
    }

    companion object {
        private const val TAG = "AdminRepositoryImpl"
    }
}

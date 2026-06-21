package com.knotworking.authexample.data.repository

import com.knotworking.authexample.data.network.api.AuthApi
import com.knotworking.authexample.data.network.mapper.toDomain
import com.knotworking.authexample.data.network.mapper.toDto
import com.knotworking.authexample.data.network.util.safeApiCall
import com.knotworking.authexample.domain.Result
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

    private val _users = MutableStateFlow<Result<List<Credentials>>>(Result.Success(emptyList()))
    private val _tokens = MutableStateFlow<Result<List<TokenInfo>>>(Result.Success(emptyList()))

    override val usersFlow: Flow<Result<List<Credentials>>> = _users.asStateFlow()
    override val tokensFlow: Flow<Result<List<TokenInfo>>> = _tokens.asStateFlow()

    override suspend fun getUsers(): Result<List<Credentials>> {
        val result = safeApiCall { authApi.getUsers().map { it.toDomain() } }
        _users.value = result
        return result
    }

    override suspend fun addUser(credentials: Credentials): Result<Unit> {
        val result = safeApiCall {
            val response = authApi.addUser(credentials.toDto())
            if (!response.isSuccessful) throw HttpException(response)
            logger.i(TAG, "User added: ${credentials.username}")
        }
        if (result is Result.Success) getUsers()
        return result
    }

    override suspend fun removeUser(username: String): Result<Unit> {
        val result = safeApiCall {
            val response = authApi.removeUser(username)
            if (!response.isSuccessful) throw HttpException(response)
            logger.i(TAG, "User removed: $username")
        }
        if (result is Result.Success) getUsers()
        return result
    }

    override suspend fun getTokens(): Result<List<TokenInfo>> {
        val result = safeApiCall { authApi.getTokens().map { it.toDomain() } }
        _tokens.value = result
        return result
    }

    override suspend fun revokeToken(token: String): Result<Unit> {
        val result = safeApiCall {
            val response = authApi.revokeToken(token)
            if (!response.isSuccessful) throw HttpException(response)
            logger.i(TAG, "Token revoked: ${token.take(8)}...")
        }
        if (result is Result.Success) getTokens()
        return result
    }

    companion object {
        private const val TAG = "AdminRepositoryImpl"
    }
}

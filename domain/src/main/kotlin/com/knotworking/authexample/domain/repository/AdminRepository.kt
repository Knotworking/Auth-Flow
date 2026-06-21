package com.knotworking.authexample.domain.repository

import com.knotworking.authexample.domain.Result
import com.knotworking.authexample.domain.model.Credentials
import com.knotworking.authexample.domain.model.TokenInfo
import kotlinx.coroutines.flow.Flow

interface AdminRepository {
    val usersFlow: Flow<Result<List<Credentials>>>
    val tokensFlow: Flow<Result<List<TokenInfo>>>
    suspend fun getUsers(): Result<List<Credentials>>
    suspend fun addUser(credentials: Credentials): Result<Unit>
    suspend fun removeUser(username: String): Result<Unit>
    suspend fun getTokens(): Result<List<TokenInfo>>
    suspend fun revokeToken(token: String): Result<Unit>
}

package com.knotworking.authexample.domain.repository

import com.knotworking.authexample.domain.AppResult
import com.knotworking.authexample.domain.model.Credentials
import com.knotworking.authexample.domain.model.TokenInfo

interface AdminRepository {
    suspend fun getUsers(): AppResult<List<Credentials>>
    suspend fun addUser(credentials: Credentials): AppResult<Unit>
    suspend fun removeUser(username: String): AppResult<Unit>
    suspend fun getTokens(): AppResult<List<TokenInfo>>
    suspend fun revokeToken(token: String): AppResult<Unit>
}

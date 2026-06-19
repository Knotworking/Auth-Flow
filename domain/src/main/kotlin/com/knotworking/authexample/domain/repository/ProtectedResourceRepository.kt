package com.knotworking.authexample.domain.repository

import com.knotworking.authexample.domain.AppResult

interface ProtectedResourceRepository {
    suspend fun performOperation(): AppResult<String>
}

package com.knotworking.authexample.domain.repository

import com.knotworking.authexample.domain.Result

interface ProtectedResourceRepository {
    suspend fun performOperation(): Result<String>
}

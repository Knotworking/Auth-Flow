package com.knotworking.authexample.data.repository

import com.knotworking.authexample.data.network.api.ProtectedApi
import com.knotworking.authexample.data.network.util.safeApiCall
import com.knotworking.authexample.domain.AppResult
import com.knotworking.authexample.domain.Logger
import com.knotworking.authexample.domain.repository.ProtectedResourceRepository

class ProtectedResourceRepositoryImpl(
    private val protectedApi: ProtectedApi,
    private val logger: Logger,
) : ProtectedResourceRepository {

    override suspend fun performOperation(): AppResult<String> =
        safeApiCall {
            val resource = protectedApi.getResource()
            logger.i(TAG, "Authenticated operation succeeded for ${resource.username}: ${resource.message}")
            resource.message
        }

    companion object {
        private const val TAG = "ProtectedResourceRepositoryImpl"
    }
}

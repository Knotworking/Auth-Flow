package com.knotworking.authexample.data.network.api

import com.knotworking.authexample.data.network.dto.ProtectedResourceDto
import retrofit2.http.GET

interface ProtectedApi {
    @GET("protected/resource")
    suspend fun getResource(): ProtectedResourceDto
}

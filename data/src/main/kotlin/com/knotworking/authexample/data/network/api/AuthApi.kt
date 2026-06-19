package com.knotworking.authexample.data.network.api

import com.knotworking.authexample.data.network.dto.LoginRequestDto
import com.knotworking.authexample.data.network.dto.LoginResponseDto
import com.knotworking.authexample.data.network.dto.RefreshRequestDto
import com.knotworking.authexample.data.network.dto.RefreshResponseDto
import com.knotworking.authexample.data.network.dto.TokenDto
import com.knotworking.authexample.data.network.dto.UserDto
import okhttp3.ResponseBody
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.DELETE
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

interface AuthApi {
    @POST("auth/login")
    suspend fun login(@Body request: LoginRequestDto): LoginResponseDto

    @POST("auth/refresh")
    suspend fun refresh(@Body request: RefreshRequestDto): RefreshResponseDto

    @GET("admin/users")
    suspend fun getUsers(): List<UserDto>

    @POST("admin/users")
    suspend fun addUser(@Body user: UserDto): Response<ResponseBody>

    @DELETE("admin/users/{username}")
    suspend fun removeUser(@Path("username") username: String): Response<ResponseBody>

    @GET("admin/tokens")
    suspend fun getTokens(): List<TokenDto>

    @POST("admin/tokens/{token}/revoke")
    suspend fun revokeToken(@Path("token") token: String): Response<ResponseBody>
}

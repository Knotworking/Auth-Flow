package com.knotworking.authexample.data.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class LoginRequestDto(val username: String, val password: String)

@Serializable
data class LoginResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
)

@Serializable
data class RefreshRequestDto(val refreshToken: String)

@Serializable
data class RefreshResponseDto(
    val accessToken: String,
    val expiresIn: Long,
)

@Serializable
data class UserDto(val username: String, val password: String)

@Serializable
data class TokenDto(
    val token: String,
    val type: String,
    val username: String,
    val expiresAtEpochSeconds: Long,
    val revoked: Boolean,
)

@Serializable
data class ProtectedResourceDto(
    val message: String,
    val username: String,
)

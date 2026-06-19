package com.knotworking.authexample.domain.model

import java.time.Instant

data class AuthSession(
    val username: String,
    val accessToken: String,
    val refreshToken: String,
    val accessExpiresAt: Instant,
)

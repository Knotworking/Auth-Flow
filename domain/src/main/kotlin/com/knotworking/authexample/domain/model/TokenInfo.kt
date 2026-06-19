package com.knotworking.authexample.domain.model

import java.time.Instant

data class TokenInfo(
    val token: String,
    val type: String,
    val username: String,
    val expiresAt: Instant,
    val revoked: Boolean,
)

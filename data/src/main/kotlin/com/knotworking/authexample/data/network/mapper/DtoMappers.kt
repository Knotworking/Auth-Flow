package com.knotworking.authexample.data.network.mapper

import com.knotworking.authexample.data.network.dto.LoginResponseDto
import com.knotworking.authexample.data.network.dto.RefreshResponseDto
import com.knotworking.authexample.data.network.dto.TokenDto
import com.knotworking.authexample.data.network.dto.UserDto
import com.knotworking.authexample.domain.model.AuthSession
import com.knotworking.authexample.domain.model.Credentials
import com.knotworking.authexample.domain.model.TokenInfo
import java.time.Instant

fun LoginResponseDto.toAuthSession(username: String): AuthSession = AuthSession(
    username = username,
    accessToken = accessToken,
    refreshToken = refreshToken,
    accessExpiresAt = Instant.now().plusSeconds(expiresIn),
)

fun AuthSession.withRefreshedToken(response: RefreshResponseDto): AuthSession = copy(
    accessToken = response.accessToken,
    accessExpiresAt = Instant.now().plusSeconds(response.expiresIn),
)

fun UserDto.toDomain(): Credentials = Credentials(username, password)

fun Credentials.toDto(): UserDto = UserDto(username, password)

fun TokenDto.toDomain(): TokenInfo = TokenInfo(
    token = token,
    type = type,
    username = username,
    expiresAt = Instant.ofEpochSecond(expiresAtEpochSeconds),
    revoked = revoked,
)

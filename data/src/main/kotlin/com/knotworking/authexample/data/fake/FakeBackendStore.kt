package com.knotworking.authexample.data.fake

import com.knotworking.authexample.domain.Logger
import com.knotworking.authexample.domain.model.TokenInfo
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.io.File
import java.time.Instant
import java.util.UUID

data class IssuedTokens(
    val accessToken: String,
    val refreshToken: String,
    val accessExpiresAt: Instant,
)

data class IssuedAccessToken(
    val token: String,
    val expiresAt: Instant,
)

class FakeBackendStore(
    private val storeFile: File,
    private val logger: Logger,
    val accessTokenExpirySeconds: Long = DEFAULT_ACCESS_TOKEN_EXPIRY_SECONDS,
) {
    @Serializable
    private data class StoreData(
        val users: Map<String, String> = emptyMap(),
        val accessTokens: Map<String, AccessTokenEntry> = emptyMap(),
        val refreshTokens: Map<String, String> = emptyMap(),
        val revokedRefreshTokens: Set<String> = emptySet(),
    )

    @Serializable
    data class AccessTokenEntry(
        val username: String,
        val expiresAtEpochSeconds: Long,
        val revoked: Boolean = false,
    )

    private val json = Json { ignoreUnknownKeys = true }
    private val users = mutableMapOf<String, String>()
    private val accessTokens = mutableMapOf<String, AccessTokenEntry>()
    private val refreshTokens = mutableMapOf<String, String>()
    private val revokedRefreshTokens = mutableSetOf<String>()

    fun init() = synchronized(this) { load() }

    fun isEmpty(): Boolean = synchronized(this) { users.isEmpty() }

    fun seedIfEmpty(initialUsers: Map<String, String>) {
        synchronized(this) {
            if (users.isEmpty()) {
                users.putAll(initialUsers)
                persist()
                logger.i(TAG, "Seeded ${initialUsers.size} users")
            }
        }
    }

    fun validateCredentials(username: String, password: String): Boolean =
        synchronized(this) { users[username] == password }

    fun addUser(username: String, password: String) {
        synchronized(this) {
            users[username] = password
            persist()
            logger.i(TAG, "User added: $username")
        }
    }

    fun removeUser(username: String) {
        synchronized(this) {
            users.remove(username)
            persist()
            logger.i(TAG, "User removed: $username")
        }
    }

    fun getUsers(): Map<String, String> = synchronized(this) { users.toMap() }

    fun issueTokens(username: String): IssuedTokens {
        val accessToken = UUID.randomUUID().toString()
        val refreshToken = UUID.randomUUID().toString()
        val expiresAt = Instant.now().plusSeconds(accessTokenExpirySeconds)
        synchronized(this) {
            accessTokens[accessToken] = AccessTokenEntry(username, expiresAt.epochSecond)
            refreshTokens[refreshToken] = username
            persist()
            logger.i(TAG, "Tokens issued for $username — access expires at $expiresAt")
        }
        return IssuedTokens(accessToken, refreshToken, expiresAt)
    }

    fun issueNewAccessToken(username: String): IssuedAccessToken {
        val token = UUID.randomUUID().toString()
        val expiresAt = Instant.now().plusSeconds(accessTokenExpirySeconds)
        synchronized(this) {
            accessTokens[token] = AccessTokenEntry(username, expiresAt.epochSecond)
            persist()
            logger.i(TAG, "New access token issued for $username, expires at $expiresAt")
        }
        return IssuedAccessToken(token, expiresAt)
    }

    // Returns the owning username if the token is valid (not expired, not revoked).
    fun validateAccessToken(token: String): String? = synchronized(this) {
        val entry = accessTokens[token] ?: return@synchronized null
        val expiresAt = Instant.ofEpochSecond(entry.expiresAtEpochSeconds)
        when {
            entry.revoked -> { logger.d(TAG, "Token rejected: revoked"); null }
            Instant.now().isAfter(expiresAt) -> { logger.d(TAG, "Token rejected: expired"); null }
            else -> entry.username
        }
    }

    fun getUsernameForRefreshToken(token: String): String? = synchronized(this) {
        if (token in revokedRefreshTokens) null else refreshTokens[token]
    }

    fun revokeRefreshToken(token: String): Boolean = synchronized(this) {
        if (refreshTokens.containsKey(token)) {
            revokedRefreshTokens.add(token)
            persist()
            logger.i(TAG, "Refresh token revoked: ${token.take(8)}...")
            true
        } else {
            false
        }
    }

    fun revokeAccessToken(token: String): Boolean = synchronized(this) {
        val entry = accessTokens[token] ?: return@synchronized false
        accessTokens[token] = entry.copy(revoked = true)
        persist()
        logger.i(TAG, "Access token revoked: ${token.take(8)}...")
        true
    }

    fun getAllTokens(): List<TokenInfo> = synchronized(this) {
        val accessList = accessTokens.map { (token, entry) ->
            TokenInfo(
                token = token,
                type = "access",
                username = entry.username,
                expiresAt = Instant.ofEpochSecond(entry.expiresAtEpochSeconds),
                revoked = entry.revoked,
            )
        }
        val refreshList = refreshTokens.map { (token, username) ->
            TokenInfo(
                token = token,
                type = "refresh",
                username = username,
                expiresAt = Instant.now().plusSeconds(86400 * 30L),
                revoked = token in revokedRefreshTokens,
            )
        }
        accessList + refreshList
    }

    private fun persist() {
        val data = StoreData(
            users = users.toMap(),
            accessTokens = accessTokens.toMap(),
            refreshTokens = refreshTokens.toMap(),
            revokedRefreshTokens = revokedRefreshTokens.toSet(),
        )
        storeFile.parentFile?.mkdirs()
        storeFile.writeText(json.encodeToString(data))
    }

    private fun load() {
        if (!storeFile.exists()) return
        runCatching {
            val data = json.decodeFromString<StoreData>(storeFile.readText())
            users.putAll(data.users)
            accessTokens.putAll(data.accessTokens)
            refreshTokens.putAll(data.refreshTokens)
            revokedRefreshTokens.addAll(data.revokedRefreshTokens)
            logger.i(TAG, "Loaded store: ${users.size} users, ${accessTokens.size} access tokens")
        }.onFailure { e ->
            logger.e(TAG, "Failed to load store", e)
        }
    }

    companion object {
        private const val TAG = "FakeBackendStore"
        const val DEFAULT_ACCESS_TOKEN_EXPIRY_SECONDS = 45L
    }
}

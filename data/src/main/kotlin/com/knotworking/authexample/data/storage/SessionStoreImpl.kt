package com.knotworking.authexample.data.storage

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.knotworking.authexample.core.Logger
import com.knotworking.authexample.domain.model.AuthSession
import com.knotworking.authexample.domain.repository.SessionStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import java.time.Instant
import java.util.Base64

private val Context.authSessionDataStore: DataStore<Preferences> by preferencesDataStore("auth_session")

class SessionStoreImpl(
    context: Context,
    private val cryptoManager: CryptoManager,
    private val logger: Logger,
) : SessionStore {

    private val dataStore = context.applicationContext.authSessionDataStore
    private val sessionKey = stringPreferencesKey("encrypted_session")

    override val sessionFlow: Flow<AuthSession?> = dataStore.data
        .map { prefs ->
            prefs[sessionKey]?.let { encoded ->
                runCatching {
                    val encrypted = Base64.getDecoder().decode(encoded)
                    val json = cryptoManager.decrypt(encrypted).toString(Charsets.UTF_8)
                    Json.decodeFromString<AuthSessionDto>(json).toAuthSession()
                }.onFailure { logger.e(TAG, "Failed to decrypt session", it) }
                    .getOrNull()
            }
        }

    override suspend fun save(session: AuthSession) {
        val json = Json.encodeToString(session.toDto())
        val encrypted = cryptoManager.encrypt(json.toByteArray(Charsets.UTF_8))
        val encoded = Base64.getEncoder().encodeToString(encrypted)
        dataStore.edit { prefs -> prefs[sessionKey] = encoded }
        logger.i(TAG, "Session persisted for ${session.username}")
    }

    override suspend fun read(): AuthSession? {
        val encoded = dataStore.data.map { it[sessionKey] }.first() ?: return null
        return runCatching {
            val encrypted = Base64.getDecoder().decode(encoded)
            val json = cryptoManager.decrypt(encrypted).toString(Charsets.UTF_8)
            Json.decodeFromString<AuthSessionDto>(json).toAuthSession()
        }.onFailure { logger.e(TAG, "Failed to decrypt session on read", it) }
            .getOrNull()
    }

    override suspend fun clear() {
        dataStore.edit { it.clear() }
        logger.i(TAG, "Session cleared")
    }

    companion object {
        private const val TAG = "SessionStoreImpl"
    }
}

@Serializable
private data class AuthSessionDto(
    val username: String,
    val accessToken: String,
    val refreshToken: String,
    val accessExpiresAtEpochMilli: Long,
)

private fun AuthSession.toDto() = AuthSessionDto(
    username = username,
    accessToken = accessToken,
    refreshToken = refreshToken,
    accessExpiresAtEpochMilli = accessExpiresAt.toEpochMilli(),
)

private fun AuthSessionDto.toAuthSession() = AuthSession(
    username = username,
    accessToken = accessToken,
    refreshToken = refreshToken,
    accessExpiresAt = Instant.ofEpochMilli(accessExpiresAtEpochMilli),
)

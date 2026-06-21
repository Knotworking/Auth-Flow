package com.knotworking.authexample.data.fake

import com.knotworking.authexample.core.Logger
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.Response
import okhttp3.mockwebserver.MockWebServer
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files

class FakeBackendAdminTest {

    @Serializable
    private data class LoginCapture(val accessToken: String, val refreshToken: String)

    @Serializable
    private data class UserCapture(val username: String, val password: String)

    @Serializable
    private data class TokenCapture(
        val token: String,
        val type: String,
        val username: String,
        val revoked: Boolean,
    )

    private val noOpLogger = object : Logger {
        override fun d(tag: String, message: String) = Unit
        override fun i(tag: String, message: String) = Unit
        override fun w(tag: String, message: String) = Unit
        override fun e(tag: String, message: String, throwable: Throwable?) = Unit
    }

    private val json = Json { ignoreUnknownKeys = true }
    private val client = OkHttpClient()

    private lateinit var tempDir: File
    private lateinit var store: FakeBackendStore
    private lateinit var server: MockWebServer

    @Before
    fun setUp() {
        tempDir = Files.createTempDirectory("test_admin").toFile()
        store = FakeBackendStore(
            storeFile = File(tempDir, "store.json"),
            logger = noOpLogger,
            accessTokenExpirySeconds = 60L,
        )
        store.init()
        store.addUser("alice", "password123")
        server = MockWebServer()
        server.dispatcher = FakeBackendDispatcher(store, noOpLogger)
        server.start()
    }

    @After
    fun tearDown() {
        server.shutdown()
        tempDir.deleteRecursively()
    }

    // --- Admin: users ---

    @Test
    fun `getUsers returns seeded user`() {
        val response = get("/admin/users")
        assertEquals(200, response.code)
        val users = json.decodeFromString<List<UserCapture>>(response.body!!.string())
        assertTrue(users.any { it.username == "alice" && it.password == "password123" })
    }

    @Test
    fun `addUser creates a new user who can log in`() {
        val addResponse = postJson("/admin/users", """{"username":"bob","password":"pass456"}""")
        assertEquals(201, addResponse.code)

        val loginResponse = postJson("/auth/login", """{"username":"bob","password":"pass456"}""")
        assertEquals(200, loginResponse.code)
    }

    @Test
    fun `removeUser prevents subsequent login`() {
        val deleteResponse = delete("/admin/users/alice")
        assertEquals(204, deleteResponse.code)

        val loginResponse = postJson("/auth/login", """{"username":"alice","password":"password123"}""")
        assertEquals(401, loginResponse.code)
    }

    @Test
    fun `removeUser is reflected in getUsers`() {
        delete("/admin/users/alice")
        val response = get("/admin/users")
        val users = json.decodeFromString<List<UserCapture>>(response.body!!.string())
        assertFalse(users.any { it.username == "alice" })
    }

    // --- Admin: tokens ---

    @Test
    fun `getTokens returns access and refresh tokens after login`() {
        postJson("/auth/login", """{"username":"alice","password":"password123"}""")

        val response = get("/admin/tokens")
        assertEquals(200, response.code)
        val tokens = json.decodeFromString<List<TokenCapture>>(response.body!!.string())
        assertTrue("should have an access token", tokens.any { it.type == "access" })
        assertTrue("should have a refresh token", tokens.any { it.type == "refresh" })
    }

    @Test
    fun `revoking access token marks it revoked in getTokens`() {
        val loginBody = postJson("/auth/login", """{"username":"alice","password":"password123"}""")
        val tokens = json.decodeFromString<LoginCapture>(loginBody.body!!.string())

        postJson("/admin/tokens/${tokens.accessToken}/revoke", "")

        val tokenList = json.decodeFromString<List<TokenCapture>>(get("/admin/tokens").body!!.string())
        val accessToken = tokenList.first { it.token == tokens.accessToken }
        assertTrue(accessToken.revoked)
    }

    // --- Forced-logout path: revoke refresh token ---

    @Test
    fun `revoking refresh token causes subsequent refresh to return 401`() {
        val loginBody = postJson("/auth/login", """{"username":"alice","password":"password123"}""")
        val tokens = json.decodeFromString<LoginCapture>(loginBody.body!!.string())

        // Revoke the refresh token via admin API
        val revokeResponse = postJson("/admin/tokens/${tokens.refreshToken}/revoke", "")
        assertEquals(200, revokeResponse.code)

        // Refresh attempt now returns 401
        val refreshResponse = postJson("/auth/refresh", """{"refreshToken":"${tokens.refreshToken}"}""")
        assertEquals(401, refreshResponse.code)
    }

    @Test
    fun `revoked refresh token is listed as revoked in getTokens`() {
        val loginBody = postJson("/auth/login", """{"username":"alice","password":"password123"}""")
        val tokens = json.decodeFromString<LoginCapture>(loginBody.body!!.string())

        postJson("/admin/tokens/${tokens.refreshToken}/revoke", "")

        val tokenList = json.decodeFromString<List<TokenCapture>>(get("/admin/tokens").body!!.string())
        val refreshToken = tokenList.first { it.token == tokens.refreshToken }
        assertTrue(refreshToken.revoked)
    }

    // --- Edge cases ---

    @Test
    fun `unknown route returns 404`() {
        val response = get("/not/a/real/path")
        assertEquals(404, response.code)
    }

    @Test
    fun `refresh with unknown token returns 401`() {
        val response = postJson("/auth/refresh", """{"refreshToken":"unknown-token"}""")
        assertEquals(401, response.code)
    }

    @Test
    fun `protected resource without bearer token returns 401`() {
        val request = Request.Builder().url(server.url("/protected/resource")).get().build()
        val response = client.newCall(request).execute()
        assertEquals(401, response.code)
    }

    // --- Persistence ---

    @Test
    fun `added users persist across store reload`() {
        postJson("/admin/users", """{"username":"carol","password":"carolpass"}""")

        // Simulate app restart: new store from same file
        val store2 = FakeBackendStore(
            storeFile = File(tempDir, "store.json"),
            logger = noOpLogger,
        )
        store2.init()
        assertTrue(store2.validateCredentials("carol", "carolpass"))
    }

    // --- helpers ---

    private fun postJson(path: String, body: String): Response {
        val request = Request.Builder()
            .url(server.url(path))
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()
        return client.newCall(request).execute()
    }

    private fun get(path: String): Response {
        val request = Request.Builder().url(server.url(path)).get().build()
        return client.newCall(request).execute()
    }

    private fun delete(path: String): Response {
        val request = Request.Builder().url(server.url(path)).delete().build()
        return client.newCall(request).execute()
    }
}

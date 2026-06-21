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
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import java.io.File
import java.nio.file.Files

class FakeBackendDispatcherTest {

    @Serializable
    private data class LoginCapture(val accessToken: String, val refreshToken: String)

    @Serializable
    private data class RefreshCapture(val accessToken: String)

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
        tempDir = Files.createTempDirectory("test_backend").toFile()
        store = FakeBackendStore(
            storeFile = File(tempDir, "store.json"),
            logger = noOpLogger,
            accessTokenExpirySeconds = 1L,
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

    @Test
    fun `login with valid credentials returns 200 with tokens`() {
        val response = postJson("/auth/login", """{"username":"alice","password":"password123"}""")
        assertEquals(200, response.code)
        val body = response.body!!.string()
        assertTrue("body should contain accessToken", body.contains("accessToken"))
        assertTrue("body should contain refreshToken", body.contains("refreshToken"))
    }

    @Test
    fun `login with wrong password returns 401`() {
        val response = postJson("/auth/login", """{"username":"alice","password":"wrong"}""")
        assertEquals(401, response.code)
    }

    @Test
    fun `protected resource with valid token returns 200`() {
        val tokens = login()
        val response = getAuthorized("/protected/resource", tokens.accessToken)
        assertEquals(200, response.code)
    }

    @Test
    fun `protected resource with expired token returns 401`() {
        val tokens = login()
        Thread.sleep(1500L)
        val response = getAuthorized("/protected/resource", tokens.accessToken)
        assertEquals(401, response.code)
    }

    @Test
    fun `refresh replaces expired access token`() {
        val tokens = login()
        Thread.sleep(1500L)

        val refreshResponse = postJson("/auth/refresh", """{"refreshToken":"${tokens.refreshToken}"}""")
        assertEquals(200, refreshResponse.code)

        val newToken = json.decodeFromString<RefreshCapture>(refreshResponse.body!!.string()).accessToken
        val response = getAuthorized("/protected/resource", newToken)
        assertEquals(200, response.code)
    }

    @Test
    fun `revoked token returns 401 on protected resource`() {
        val tokens = login()
        postJson("/admin/tokens/${tokens.accessToken}/revoke", "")
        val response = getAuthorized("/protected/resource", tokens.accessToken)
        assertEquals(401, response.code)
    }

    // --- helpers ---

    private fun login(): LoginCapture {
        val response = postJson("/auth/login", """{"username":"alice","password":"password123"}""")
        return json.decodeFromString(response.body!!.string())
    }

    private fun postJson(path: String, body: String): Response {
        val request = Request.Builder()
            .url(server.url(path))
            .post(body.toRequestBody("application/json".toMediaType()))
            .build()
        return client.newCall(request).execute()
    }

    private fun getAuthorized(path: String, token: String): Response {
        val request = Request.Builder()
            .url(server.url(path))
            .addHeader("Authorization", "Bearer $token")
            .get()
            .build()
        return client.newCall(request).execute()
    }
}

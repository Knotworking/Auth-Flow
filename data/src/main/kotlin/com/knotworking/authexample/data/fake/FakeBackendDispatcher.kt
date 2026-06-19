package com.knotworking.authexample.data.fake

import com.knotworking.authexample.domain.Logger
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json
import okhttp3.mockwebserver.Dispatcher
import okhttp3.mockwebserver.MockResponse
import okhttp3.mockwebserver.RecordedRequest

// --- Request / response DTOs (internal to fake backend) ---

@Serializable
internal data class LoginRequestDto(val username: String, val password: String)

@Serializable
internal data class LoginResponseDto(
    val accessToken: String,
    val refreshToken: String,
    val expiresIn: Long,
)

@Serializable
internal data class RefreshRequestDto(val refreshToken: String)

@Serializable
internal data class RefreshResponseDto(val accessToken: String, val expiresIn: Long)

@Serializable
internal data class CredentialsDto(val username: String, val password: String)

@Serializable
internal data class TokenDto(
    val token: String,
    val type: String,
    val username: String,
    val expiresAtEpochSeconds: Long,
    val revoked: Boolean,
)

// -----------------------------------------------------------

class FakeBackendDispatcher(
    private val store: FakeBackendStore,
    private val logger: Logger,
) : Dispatcher() {

    private val json = Json { ignoreUnknownKeys = true }

    override fun dispatch(request: RecordedRequest): MockResponse {
        val path = request.path ?: return notFound()
        val method = request.method ?: return notFound()
        return try {
            route(method, path, request)
        } catch (e: Exception) {
            logger.e(TAG, "Unhandled error dispatching $method $path: ${e.message}", e)
            serverError()
        }
    }

    private fun route(method: String, path: String, request: RecordedRequest): MockResponse = when {
        method == "POST" && path == "/auth/login" -> handleLogin(request)
        method == "POST" && path == "/auth/refresh" -> handleRefresh(request)
        method == "GET" && path == "/protected/resource" -> handleProtectedResource(request)
        method == "GET" && path == "/admin/users" -> handleGetUsers()
        method == "POST" && path == "/admin/users" -> handleAddUser(request)
        method == "DELETE" && path.startsWith("/admin/users/") -> handleDeleteUser(path)
        method == "GET" && path == "/admin/tokens" -> handleGetTokens()
        method == "POST" && path.matches(Regex("/admin/tokens/[^/]+/revoke")) -> handleRevokeToken(path)
        else -> notFound()
    }

    private fun handleLogin(request: RecordedRequest): MockResponse {
        val dto = request.parseBody<LoginRequestDto>() ?: return badRequest("Invalid body")
        logger.i(TAG, "Login attempt: ${dto.username}")

        if (!store.validateCredentials(dto.username, dto.password)) {
            logger.w(TAG, "Login failed: invalid credentials for ${dto.username}")
            return unauthorized("Invalid credentials")
        }

        val issued = store.issueTokens(dto.username)
        logger.i(TAG, "Login successful for ${dto.username}")
        return ok(json.encodeToString(LoginResponseDto(issued.accessToken, issued.refreshToken, store.accessTokenExpirySeconds)))
    }

    private fun handleRefresh(request: RecordedRequest): MockResponse {
        val dto = request.parseBody<RefreshRequestDto>() ?: return badRequest("Invalid body")
        logger.i(TAG, "Refresh attempt")

        val username = store.getUsernameForRefreshToken(dto.refreshToken)
            ?: run {
                logger.w(TAG, "Refresh failed: unknown refresh token")
                return unauthorized("Invalid refresh token")
            }

        val issued = store.issueNewAccessToken(username)
        logger.i(TAG, "Refresh successful for $username")
        return ok(json.encodeToString(RefreshResponseDto(issued.token, store.accessTokenExpirySeconds)))
    }

    private fun handleProtectedResource(request: RecordedRequest): MockResponse {
        val token = request.bearerToken()
            ?: run { logger.w(TAG, "Protected: missing bearer"); return unauthorized("Missing token") }

        val username = store.validateAccessToken(token)
            ?: run { logger.w(TAG, "Protected: token invalid/expired/revoked"); return unauthorized("Token invalid") }

        logger.i(TAG, "Protected resource accessed by $username")
        return ok("""{"message":"Protected data","username":"$username"}""")
    }

    private fun handleGetUsers(): MockResponse {
        val users = store.getUsers().map { (u, p) -> CredentialsDto(u, p) }
        logger.i(TAG, "Admin: listing ${users.size} users")
        return ok(json.encodeToString(users))
    }

    private fun handleAddUser(request: RecordedRequest): MockResponse {
        val dto = request.parseBody<CredentialsDto>() ?: return badRequest("Invalid body")
        store.addUser(dto.username, dto.password)
        return created()
    }

    private fun handleDeleteUser(path: String): MockResponse {
        val username = path.removePrefix("/admin/users/")
        store.removeUser(username)
        return noContent()
    }

    private fun handleGetTokens(): MockResponse {
        val tokens = store.getAllTokens().map { info ->
            TokenDto(info.token, info.type, info.username, info.expiresAt.epochSecond, info.revoked)
        }
        logger.i(TAG, "Admin: listing ${tokens.size} tokens")
        return ok(json.encodeToString(tokens))
    }

    private fun handleRevokeToken(path: String): MockResponse {
        val token = path.removePrefix("/admin/tokens/").removeSuffix("/revoke")
        val revoked = store.revokeAccessToken(token) || store.revokeRefreshToken(token)
        if (!revoked) logger.w(TAG, "Token not found for revocation: ${token.take(8)}...")
        return ok("{}")
    }

    // --- helpers ---

    private inline fun <reified T> RecordedRequest.parseBody(): T? =
        runCatching { json.decodeFromString<T>(body.readUtf8()) }.getOrNull()

    private fun RecordedRequest.bearerToken(): String? =
        getHeader("Authorization")?.takeIf { it.startsWith("Bearer ") }?.removePrefix("Bearer ")

    private fun ok(body: String) =
        MockResponse().setResponseCode(200).setBody(body).addHeader("Content-Type", "application/json")

    private fun created() = MockResponse().setResponseCode(201)
    private fun noContent() = MockResponse().setResponseCode(204)
    private fun badRequest(msg: String) = MockResponse().setResponseCode(400).setBody("""{"error":"$msg"}""")
    private fun unauthorized(msg: String) = MockResponse().setResponseCode(401).setBody("""{"error":"$msg"}""")
    private fun notFound() = MockResponse().setResponseCode(404)
    private fun serverError() = MockResponse().setResponseCode(500)

    companion object {
        private const val TAG = "FakeBackendDispatcher"
    }
}

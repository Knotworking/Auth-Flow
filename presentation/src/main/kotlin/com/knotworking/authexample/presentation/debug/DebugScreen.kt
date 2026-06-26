package com.knotworking.authexample.presentation.debug

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.knotworking.authexample.domain.model.AuthSession
import com.knotworking.authexample.domain.model.Credentials
import com.knotworking.authexample.domain.model.TokenInfo
import com.knotworking.authexample.presentation.theme.AuthExampleTheme
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val displayFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yy").withZone(ZoneId.systemDefault())

@Composable
fun DebugScreen(viewModel: DebugViewModel = koinViewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    DebugScreenContent(
        state = state,
        onIntent = { viewModel.onIntent(it) },
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DebugScreenContent(
    state: DebugContract.State,
    onIntent: (DebugContract.Intent) -> Unit,
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Debug") },
                actions = {
                    TextButton(onClick = { onIntent(DebugContract.Intent.Refresh) }) {
                        Text("Refresh")
                    }
                },
            )
        }
    ) { padding ->
        if (state.isLoading) {
            Column(
                modifier = Modifier.fillMaxSize().padding(padding),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                CircularProgressIndicator()
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
            ) {
                state.error?.let { error ->
                    item {
                        Text(
                            text = error,
                            color = MaterialTheme.colorScheme.error,
                            style = MaterialTheme.typography.bodySmall,
                            modifier = Modifier.padding(vertical = 4.dp),
                        )
                    }
                }

                // --- Current session ---
                item { SectionHeader("Current Session") }
                item { SessionCard(state.currentSession) }

                // --- Users ---
                item { Spacer(Modifier.height(8.dp)) }
                item { SectionHeader("Users (${state.users.size})") }
                items(state.users, key = { it.username }) { user ->
                    UserRow(
                        credentials = user,
                        onRemove = { onIntent(DebugContract.Intent.RemoveUser(user.username)) },
                    )
                }
                item { AddUserRow(state = state, onIntent = onIntent) }

                // --- Tokens ---
                item { Spacer(Modifier.height(8.dp)) }
                item { SectionHeader("Tokens (${state.tokens.size})") }
                items(state.tokens, key = { it.token }) { token ->
                    TokenRow(
                        token = token,
                        onRevoke = { onIntent(DebugContract.Intent.RevokeToken(token.token)) },
                    )
                }
                item { Spacer(Modifier.height(16.dp)) }
            }
        }
    }
}

@Composable
private fun SectionHeader(title: String) {
    Text(
        text = title,
        style = MaterialTheme.typography.titleSmall,
        color = MaterialTheme.colorScheme.primary,
        modifier = Modifier.padding(vertical = 4.dp),
    )
    HorizontalDivider()
}

@Composable
private fun SessionCard(session: AuthSession?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
            if (session == null) {
                Text("No active session", style = MaterialTheme.typography.bodyMedium)
            } else {
                LabeledValue("User", session.username)
                LabeledValue("Access token", session.accessToken.take(16) + "…")
                val isExpired = Instant.now().isAfter(session.accessExpiresAt)
                LabeledValue(
                    label = "Expires",
                    value = displayFormatter.format(session.accessExpiresAt) + if (isExpired) " (EXPIRED)" else "",
                )
            }
        }
    }
}

@Composable
private fun UserRow(credentials: Credentials, onRemove: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Text(credentials.username, style = MaterialTheme.typography.bodyMedium)
            Text(credentials.password, style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant)
        }
        TextButton(
            onClick = onRemove,
            colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
        ) {
            Text("Remove")
        }
    }
}

@Composable
private fun AddUserRow(state: DebugContract.State, onIntent: (DebugContract.Intent) -> Unit) {
    Column(
        modifier = Modifier.fillMaxWidth().padding(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(8.dp),
    ) {
        Text("Add user", style = MaterialTheme.typography.labelMedium)
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            OutlinedTextField(
                value = state.newUsername,
                onValueChange = { onIntent(DebugContract.Intent.UpdateNewUsername(it)) },
                label = { Text("Username") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
            OutlinedTextField(
                value = state.newPassword,
                onValueChange = { onIntent(DebugContract.Intent.UpdateNewPassword(it)) },
                label = { Text("Password") },
                singleLine = true,
                modifier = Modifier.weight(1f),
            )
        }
        Button(
            onClick = { onIntent(DebugContract.Intent.AddUser) },
            enabled = state.newUsername.isNotBlank() && state.newPassword.isNotBlank(),
            modifier = Modifier.fillMaxWidth(),
        ) {
            Text("Add")
        }
    }
}

@Composable
private fun TokenRow(token: TokenInfo, onRevoke: () -> Unit) {
    val isExpired = Instant.now().isAfter(token.expiresAt)
    val statusLabel = when {
        token.revoked -> "REVOKED"
        isExpired -> "EXPIRED"
        else -> "VALID"
    }
    val statusColor = when {
        token.revoked || isExpired -> MaterialTheme.colorScheme.error
        else -> MaterialTheme.colorScheme.primary
    }
    Card(modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp)) {
        Row(
            modifier = Modifier.padding(12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = token.token.take(16) + "…",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                )
                Text(token.username, style = MaterialTheme.typography.bodyMedium)
                Text(
                    text = "Expires: ${displayFormatter.format(token.expiresAt)}",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                )
                Text(statusLabel, style = MaterialTheme.typography.labelSmall, color = statusColor)
            }
            if (!token.revoked && !isExpired) {
                TextButton(
                    onClick = onRevoke,
                    colors = ButtonDefaults.textButtonColors(contentColor = MaterialTheme.colorScheme.error),
                ) {
                    Text("Revoke")
                }
            }
        }
    }
}

@Composable
private fun LabeledValue(label: String, value: String) {
    Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = "$label:",
            style = MaterialTheme.typography.bodySmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall,
            fontFamily = FontFamily.Monospace,
        )
    }
}

@Preview
@Composable
private fun DebugScreenContentPreview() {
    AuthExampleTheme {
        DebugScreenContent(
            state = DebugContract.State(
                users = listOf(
                    Credentials(username = "alice", password = "password123"),
                    Credentials(username = "bob", password = "hunter2"),
                ),
                tokens = listOf(
                    TokenInfo(
                        token = "tok-valid-aaaa",
                        type = "access",
                        username = "alice",
                        expiresAt = Instant.now().plusSeconds(3600),
                        revoked = false,
                    ),
                    TokenInfo(
                        token = "tok-expired-bbbb",
                        type = "access",
                        username = "bob",
                        expiresAt = Instant.now().minusSeconds(60),
                        revoked = false,
                    ),
                    TokenInfo(
                        token = "tok-revoked-cccc",
                        type = "refresh",
                        username = "alice",
                        expiresAt = Instant.now().plusSeconds(7200),
                        revoked = true,
                    ),
                ),
                currentSession = AuthSession(
                    username = "alice",
                    accessToken = "abcdef1234567890abcdef1234567890",
                    refreshToken = "refresh-token-xyz",
                    accessExpiresAt = Instant.now().plusSeconds(3600),
                ),
            ),
            onIntent = {},
        )
    }
}

@Preview
@Composable
private fun DebugScreenContentLoadingPreview() {
    AuthExampleTheme {
        DebugScreenContent(
            state = DebugContract.State(isLoading = true),
            onIntent = {},
        )
    }
}

@Preview
@Composable
private fun DebugScreenContentEmptyPreview() {
    AuthExampleTheme {
        DebugScreenContent(
            state = DebugContract.State(),
            onIntent = {},
        )
    }
}

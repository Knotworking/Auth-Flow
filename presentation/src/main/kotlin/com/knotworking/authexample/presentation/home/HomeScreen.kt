package com.knotworking.authexample.presentation.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Menu
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DrawerValue
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalDrawerSheet
import androidx.compose.material3.ModalNavigationDrawer
import androidx.compose.material3.NavigationDrawerItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberDrawerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.knotworking.authexample.presentation.theme.AuthExampleTheme
import kotlinx.coroutines.launch
import org.koin.androidx.compose.koinViewModel
import java.time.Instant
import java.time.ZoneId
import java.time.format.DateTimeFormatter

private val displayFormatter: DateTimeFormatter =
    DateTimeFormatter.ofPattern("HH:mm:ss dd/MM/yy").withZone(ZoneId.systemDefault())

@Composable
fun HomeScreen(
    onNavigateToDebug: () -> Unit,
    viewModel: HomeViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    HomeScreenContent(
        state = state,
        onIntent = { viewModel.onIntent(it) },
        onNavigateToDebug = onNavigateToDebug,
    )
}

@Composable
private fun HomeScreenContent(
    state: HomeContract.State,
    onIntent: (HomeContract.Intent) -> Unit,
    onNavigateToDebug: () -> Unit,
) {
    val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
    val scope = rememberCoroutineScope()

    ModalNavigationDrawer(
        drawerState = drawerState,
        drawerContent = {
            HomeDrawerSheet(
                username = state.username,
                onNavigateToDebug = {
                    scope.launch { drawerState.close() }
                    onNavigateToDebug()
                },
                onLogout = {
                    scope.launch { drawerState.close() }
                    onIntent(HomeContract.Intent.Logout)
                },
            )
        },
    ) {
        Scaffold(
            topBar = { HomeTopBar(onOpenDrawer = { scope.launch { drawerState.open() } }) }
        ) { padding ->
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp),
                verticalArrangement = Arrangement.spacedBy(16.dp),
            ) {
                SessionCard(accessToken = state.accessToken, accessExpiresAt = state.accessExpiresAt)
                AuthOperationSection(
                    isLoading = state.isOperationLoading,
                    onClick = { onIntent(HomeContract.Intent.PerformAuthOperation) },
                )
                state.operationResult?.let { OperationResultCard(result = it) }
                state.error?.let { HomeErrorMessage(error = it) }
            }
        }
    }
}

@Composable
private fun HomeDrawerSheet(
    username: String,
    onNavigateToDebug: () -> Unit,
    onLogout: () -> Unit,
) {
    ModalDrawerSheet {
        Column(modifier = Modifier.padding(16.dp)) {
            Text("AuthExample", style = MaterialTheme.typography.headlineSmall)
            Text(
                text = username,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }
        HorizontalDivider()
        NavigationDrawerItem(
            label = { Text("Developer settings") },
            selected = false,
            icon = { Icon(Icons.Default.Settings, contentDescription = null) },
            onClick = onNavigateToDebug,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
        NavigationDrawerItem(
            label = { Text("Logout") },
            selected = false,
            onClick = onLogout,
            modifier = Modifier.padding(horizontal = 12.dp),
        )
    }
}

@Preview
@Composable
private fun HomeDrawerSheetPreview() {
    AuthExampleTheme {
        HomeDrawerSheet(
            username = "alice",
            onNavigateToDebug = {},
            onLogout = {},
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeTopBar(onOpenDrawer: () -> Unit) {
    TopAppBar(
        title = { Text("Home") },
        navigationIcon = {
            IconButton(onClick = onOpenDrawer) {
                Icon(Icons.Default.Menu, contentDescription = "Open menu")
            }
        },
    )
}

@Preview
@Composable
private fun HomeTopBarPreview() {
    AuthExampleTheme {
        HomeTopBar(onOpenDrawer = {})
    }
}

@Composable
private fun SessionCard(accessToken: String, accessExpiresAt: Instant?) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp),
        ) {
            Text(
                "Session",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            if (accessToken.isNotEmpty()) {
                Text(
                    text = accessToken.take(20) + "…",
                    style = MaterialTheme.typography.bodySmall,
                    fontFamily = FontFamily.Monospace,
                )
                if (accessExpiresAt != null) {
                    val isExpired = Instant.now().isAfter(accessExpiresAt)
                    Text(
                        text = "Expires: ${displayFormatter.format(accessExpiresAt)}" +
                                if (isExpired) " — EXPIRED" else "",
                        style = MaterialTheme.typography.bodySmall,
                        color = if (isExpired) MaterialTheme.colorScheme.error
                        else MaterialTheme.colorScheme.onSurfaceVariant,
                    )
                }
            } else {
                Text("No session data", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SessionCardPreview() {
    AuthExampleTheme {
        SessionCard(
            accessToken = "abcdef1234567890abcdef1234567890",
            accessExpiresAt = Instant.now().plusSeconds(3600),
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun SessionCardEmptyPreview() {
    AuthExampleTheme {
        SessionCard(accessToken = "", accessExpiresAt = null)
    }
}

@Composable
private fun AuthOperationSection(isLoading: Boolean, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        enabled = !isLoading,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("Perform authenticated operation")
    }
    if (isLoading) {
        Box(modifier = Modifier.fillMaxWidth(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthOperationSectionPreview() {
    AuthExampleTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            AuthOperationSection(isLoading = false, onClick = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AuthOperationSectionLoadingPreview() {
    AuthExampleTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            AuthOperationSection(isLoading = true, onClick = {})
        }
    }
}

@Composable
private fun OperationResultCard(result: String) {
    Card(modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(
                "Result",
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary,
            )
            Spacer(Modifier.height(4.dp))
            Text(result, style = MaterialTheme.typography.bodyMedium)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun OperationResultCardPreview() {
    AuthExampleTheme {
        OperationResultCard(result = "Operation succeeded")
    }
}

@Composable
private fun HomeErrorMessage(error: String) {
    Text(
        text = error,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
    )
}

@Preview(showBackground = true)
@Composable
private fun HomeErrorMessagePreview() {
    AuthExampleTheme {
        HomeErrorMessage(error = "Something went wrong")
    }
}

@Preview
@Composable
private fun HomeScreenContentPreview() {
    AuthExampleTheme {
        HomeScreenContent(
            state = HomeContract.State(
                username = "alice",
                accessToken = "abcdef1234567890abcdef1234567890",
                accessExpiresAt = Instant.now().plusSeconds(3600),
            ),
            onIntent = {},
            onNavigateToDebug = {},
        )
    }
}

@Preview
@Composable
private fun HomeScreenContentLoadingPreview() {
    AuthExampleTheme {
        HomeScreenContent(
            state = HomeContract.State(
                username = "alice",
                accessToken = "abcdef1234567890abcdef1234567890",
                accessExpiresAt = Instant.now().plusSeconds(3600),
                isOperationLoading = true,
            ),
            onIntent = {},
            onNavigateToDebug = {},
        )
    }
}

@Preview
@Composable
private fun HomeScreenContentResultPreview() {
    AuthExampleTheme {
        HomeScreenContent(
            state = HomeContract.State(
                username = "alice",
                accessToken = "abcdef1234567890abcdef1234567890",
                accessExpiresAt = Instant.now().plusSeconds(3600),
                operationResult = "Operation succeeded",
            ),
            onIntent = {},
            onNavigateToDebug = {},
        )
    }
}

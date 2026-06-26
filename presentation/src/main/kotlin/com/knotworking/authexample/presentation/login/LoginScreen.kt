package com.knotworking.authexample.presentation.login

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.knotworking.authexample.presentation.theme.AuthExampleTheme
import org.koin.androidx.compose.koinViewModel

@Composable
fun LoginScreen(
    onNavigateToDebug: () -> Unit,
    viewModel: LoginViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LoginScreenContent(
        state = state,
        onIntent = { viewModel.onIntent(it) },
        onNavigateToDebug = onNavigateToDebug,
    )
}

@Composable
private fun LoginScreenContent(
    state: LoginContract.State,
    onIntent: (LoginContract.Intent) -> Unit,
    onNavigateToDebug: () -> Unit,
) {
    Scaffold(
        topBar = { LoginTopBar(onNavigateToDebug = onNavigateToDebug) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentAlignment = Alignment.Center,
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 32.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                LoginFormFields(
                    username = state.usernameInput,
                    password = state.passwordInput,
                    onUsernameChange = { onIntent(LoginContract.Intent.UpdateUsername(it)) },
                    onPasswordChange = { onIntent(LoginContract.Intent.UpdatePassword(it)) },
                    onSubmit = { onIntent(LoginContract.Intent.Submit) },
                    enabled = !state.isLoading,
                )
                state.error?.let { LoginErrorMessage(error = it) }
                LoginButton(
                    onClick = { onIntent(LoginContract.Intent.Submit) },
                    enabled = !state.isLoading,
                    isLoading = state.isLoading,
                )
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun LoginTopBar(onNavigateToDebug: () -> Unit) {
    TopAppBar(
        title = { Text("Login") },
        actions = {
            IconButton(onClick = onNavigateToDebug) {
                Icon(Icons.Default.Settings, contentDescription = "Debug settings")
            }
        },
    )
}

@Preview
@Composable
private fun LoginTopBarPreview() {
    AuthExampleTheme {
        LoginTopBar(onNavigateToDebug = {})
    }
}

@Composable
private fun LoginFormFields(
    username: String,
    password: String,
    onUsernameChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    enabled: Boolean,
) {
    OutlinedTextField(
        value = username,
        onValueChange = onUsernameChange,
        label = { Text("Username") },
        singleLine = true,
        enabled = enabled,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Next),
        modifier = Modifier.fillMaxWidth(),
    )
    OutlinedTextField(
        value = password,
        onValueChange = onPasswordChange,
        label = { Text("Password") },
        singleLine = true,
        enabled = enabled,
        visualTransformation = PasswordVisualTransformation(),
        keyboardOptions = KeyboardOptions(
            keyboardType = KeyboardType.Password,
            imeAction = ImeAction.Done,
        ),
        keyboardActions = KeyboardActions(onDone = { onSubmit() }),
        modifier = Modifier.fillMaxWidth(),
    )
}

@Preview(showBackground = true)
@Composable
private fun LoginFormFieldsEmptyPreview() {
    AuthExampleTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            LoginFormFields(
                username = "",
                password = "",
                onUsernameChange = {},
                onPasswordChange = {},
                onSubmit = {},
                enabled = true,
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginFormFieldsFilledPreview() {
    AuthExampleTheme {
        Column(modifier = Modifier.padding(16.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
            LoginFormFields(
                username = "alice",
                password = "secret",
                onUsernameChange = {},
                onPasswordChange = {},
                onSubmit = {},
                enabled = true,
            )
        }
    }
}

@Composable
private fun LoginErrorMessage(error: String) {
    Text(
        text = error,
        color = MaterialTheme.colorScheme.error,
        style = MaterialTheme.typography.bodySmall,
    )
}

@Preview(showBackground = true)
@Composable
private fun LoginErrorMessagePreview() {
    AuthExampleTheme {
        LoginErrorMessage(error = "Invalid credentials")
    }
}

@Composable
private fun LoginButton(
    onClick: () -> Unit,
    enabled: Boolean,
    isLoading: Boolean,
) {
    Spacer(Modifier.height(4.dp))
    Button(
        onClick = onClick,
        enabled = enabled,
        modifier = Modifier.fillMaxWidth(),
    ) {
        Text("Login")
    }
    if (isLoading) {
        CircularProgressIndicator()
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginButtonPreview() {
    AuthExampleTheme {
        Column(modifier = Modifier.padding(horizontal = 32.dp)) {
            LoginButton(onClick = {}, enabled = true, isLoading = false)
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun LoginButtonLoadingPreview() {
    AuthExampleTheme {
        Column(modifier = Modifier.padding(horizontal = 32.dp)) {
            LoginButton(onClick = {}, enabled = false, isLoading = true)
        }
    }
}

@Preview
@Composable
private fun LoginScreenContentPreview() {
    AuthExampleTheme {
        LoginScreenContent(
            state = LoginContract.State(usernameInput = "alice", passwordInput = "secret"),
            onIntent = {},
            onNavigateToDebug = {},
        )
    }
}

@Preview
@Composable
private fun LoginScreenContentLoadingPreview() {
    AuthExampleTheme {
        LoginScreenContent(
            state = LoginContract.State(
                usernameInput = "alice",
                passwordInput = "secret",
                isLoading = true,
            ),
            onIntent = {},
            onNavigateToDebug = {},
        )
    }
}

@Preview
@Composable
private fun LoginScreenContentErrorPreview() {
    AuthExampleTheme {
        LoginScreenContent(
            state = LoginContract.State(
                usernameInput = "alice",
                passwordInput = "secret",
                error = "Invalid credentials",
            ),
            onIntent = {},
            onNavigateToDebug = {},
        )
    }
}

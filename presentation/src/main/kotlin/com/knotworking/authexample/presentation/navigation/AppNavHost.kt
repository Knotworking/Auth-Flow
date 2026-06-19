package com.knotworking.authexample.presentation.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.knotworking.authexample.domain.model.AuthState
import com.knotworking.authexample.presentation.AppViewModel
import com.knotworking.authexample.presentation.debug.DebugScreen
import com.knotworking.authexample.presentation.home.HomeScreen
import com.knotworking.authexample.presentation.login.LoginScreen
import org.koin.androidx.compose.koinViewModel

@Composable
fun AppNavHost(viewModel: AppViewModel = koinViewModel()) {
    val navController = rememberNavController()
    val authState by viewModel.authState.collectAsStateWithLifecycle()

    LaunchedEffect(authState) {
        when (authState) {
            is AuthState.Authenticated -> navController.navigate(Route.Home) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
            is AuthState.Unauthenticated -> navController.navigate(Route.Login) {
                popUpTo(0) { inclusive = true }
                launchSingleTop = true
            }
            AuthState.Unknown -> Unit
        }
    }

    NavHost(navController = navController, startDestination = Route.Login) {
        composable(Route.Login) {
            LoginScreen(onNavigateToDebug = { navController.navigate(Route.Debug) })
        }
        composable(Route.Home) {
            HomeScreen(onNavigateToDebug = { navController.navigate(Route.Debug) })
        }
        composable(Route.Debug) {
            DebugScreen()
        }
    }
}

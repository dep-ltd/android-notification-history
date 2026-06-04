package com.notificationhistory.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.notificationhistory.ui.feed.FeedScreen
import com.notificationhistory.ui.feed.FeedViewModel
import com.notificationhistory.ui.lock.LockScreen
import com.notificationhistory.security.AuthManager

@Composable
fun AppNavGraph(
    authManager: AuthManager,
    feedViewModel: FeedViewModel
) {
    val navController = rememberNavController()
    val startDestination = if (authManager.isPinSet()) "lock" else "setup"

    NavHost(navController = navController, startDestination = startDestination) {
        composable("lock") {
            LockScreen(
                remainingAttempts = authManager.getRemainingAttempts(),
                onPinEntered = { pin ->
                    if (authManager.checkPin(pin)) {
                        navController.navigate("feed") {
                            popUpTo("lock") { inclusive = true }
                        }
                    }
                }
            )
        }
        composable("setup") {
            // Simple Setup screen for now
            LockScreen(
                remainingAttempts = 10,
                onPinEntered = { pin ->
                    authManager.setPin(pin)
                    navController.navigate("feed") {
                        popUpTo("setup") { inclusive = true }
                    }
                }
            )
        }
        composable("feed") {
            FeedScreen(viewModel = feedViewModel)
        }
    }
}

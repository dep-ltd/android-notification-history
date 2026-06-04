package com.notificationhistory.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.notificationhistory.security.AuthManager
import com.notificationhistory.ui.detail.DetailScreen
import com.notificationhistory.ui.feed.FeedScreen
import com.notificationhistory.ui.feed.FeedViewModel
import com.notificationhistory.ui.lock.LockScreen
import com.notificationhistory.ui.permission.PermissionScreen
import com.notificationhistory.util.NotificationAccess

@Composable
fun AppNavGraph(
    authManager: AuthManager,
    feedViewModel: FeedViewModel
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasListenerAccess by remember { mutableStateOf(NotificationAccess.isListenerEnabled(context)) }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasListenerAccess = NotificationAccess.isListenerEnabled(context)
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val startDestination = when {
        !hasListenerAccess -> "permission"
        authManager.isPinSet() -> "lock"
        else -> "setup"
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("permission") {
            PermissionScreen(
                onAccessGranted = {
                    hasListenerAccess = true
                    val next = if (authManager.isPinSet()) "lock" else "setup"
                    navController.navigate(next) {
                        popUpTo("permission") { inclusive = true }
                    }
                }
            )
        }
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
            FeedScreen(
                viewModel = feedViewModel,
                onNotificationClick = { id ->
                    navController.navigate("detail/$id")
                }
            )
        }
        composable(
            route = "detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) {
            DetailScreen(onBack = { navController.popBackStack() })
        }
    }
}

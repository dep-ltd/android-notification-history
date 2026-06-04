package com.notificationhistory.ui.navigation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalLifecycleOwner
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.hilt.navigation.compose.hiltViewModel
import com.notificationhistory.security.AppSessionManager
import com.notificationhistory.security.AuthManager
import com.notificationhistory.security.BiometricGate
import com.notificationhistory.ui.detail.DetailScreen
import com.notificationhistory.ui.adaptive.MainAppScaffold
import com.notificationhistory.ui.feed.FeedViewModel
import com.notificationhistory.ui.lock.LockScreen
import com.notificationhistory.ui.lock.PinSetupScreen
import com.notificationhistory.ui.onboarding.ConsentScreen
import com.notificationhistory.ui.onboarding.WipeCompleteScreen
import com.notificationhistory.ui.permission.PermissionScreen
import com.notificationhistory.ui.settings.AppPickerScreen
import com.notificationhistory.ui.settings.SettingsScreen
import com.notificationhistory.util.NotificationAccess
@Composable
fun AppNavGraph(
    authManager: AuthManager,
    sessionManager: AppSessionManager
) {
    val navController = rememberNavController()
    val context = LocalContext.current
    val activity = context as FragmentActivity
    val lifecycleOwner = LocalLifecycleOwner.current
    var hasListenerAccess by remember { mutableStateOf(NotificationAccess.isListenerEnabled(context)) }
    val isUnlocked by sessionManager.isUnlocked.collectAsState()
    val requiresWipeScreen by authManager.requiresWipeScreen.collectAsState()
    var policyAccepted by remember { mutableStateOf(authManager.hasAcceptedWipePolicy()) }

    LaunchedEffect(requiresWipeScreen) {
        if (requiresWipeScreen) {
            navController.navigate("wipe_complete") {
                popUpTo(navController.graph.id) { inclusive = true }
            }
        }
    }

    DisposableEffect(lifecycleOwner) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) {
                hasListenerAccess = NotificationAccess.isListenerEnabled(context)
                sessionManager.lockIfTimedOut()
            }
        }
        lifecycleOwner.lifecycle.addObserver(observer)
        onDispose { lifecycleOwner.lifecycle.removeObserver(observer) }
    }

    val startDestination = when {
        requiresWipeScreen -> "wipe_complete"
        !hasListenerAccess -> "permission"
        !authManager.isPinSet() -> if (policyAccepted) "setup" else "consent"
        !isUnlocked -> "lock"
        else -> "feed"
    }

    NavHost(navController = navController, startDestination = startDestination) {
        composable("wipe_complete") {
            WipeCompleteScreen(
                onContinue = {
                    authManager.acknowledgeWipeScreen()
                    policyAccepted = false
                    navController.navigate("consent") {
                        popUpTo("wipe_complete") { inclusive = true }
                    }
                }
            )
        }
        composable("consent") {
            ConsentScreen(
                onContinue = {
                    authManager.acceptWipePolicy()
                    policyAccepted = true
                    navController.navigate("setup") {
                        popUpTo("consent") { inclusive = true }
                    }
                }
            )
        }
        composable("permission") {
            PermissionScreen(
                onAccessGranted = {
                    hasListenerAccess = true
                    val next = when {
                        requiresWipeScreen -> "wipe_complete"
                        !authManager.isPinSet() -> if (authManager.hasAcceptedWipePolicy()) "setup" else "consent"
                        !isUnlocked -> "lock"
                        else -> "feed"
                    }
                    navController.navigate(next) {
                        popUpTo("permission") { inclusive = true }
                    }
                }
            )
        }
        composable("setup") {
            var accepted by remember { mutableStateOf(authManager.hasAcceptedWipePolicy()) }
            PinSetupScreen(
                hasAcceptedPolicy = accepted,
                onAcceptPolicy = { checked ->
                    accepted = checked
                    if (checked) authManager.acceptWipePolicy()
                },
                onPinConfirmed = { pin ->
                    authManager.setupPin(pin)
                    navController.navigate("feed") {
                        popUpTo("setup") { inclusive = true }
                    }
                }
            )
        }
        composable("lock") {
            val remaining = authManager.getRemainingAttempts()
            LockScreen(
                remainingAttempts = remaining,
                showBiometric = authManager.isBiometricEnabled() && BiometricGate.canAuthenticate(activity),
                onPinEntered = { pin ->
                    if (authManager.checkPin(pin)) {
                        navController.navigate("feed") {
                            popUpTo("lock") { inclusive = true }
                        }
                    }
                },
                onBiometricClick = {
                    BiometricGate.authenticate(
                        activity = activity,
                        onSuccess = { authManager.registerBiometricSuccess() },
                        onFailure = { authManager.registerBiometricFailure() }
                    )
                }
            )
            LaunchedEffect(isUnlocked) {
                if (isUnlocked) {
                    navController.navigate("feed") {
                        popUpTo("lock") { inclusive = true }
                    }
                }
            }
        }
        composable("feed") {
            val feedViewModel: FeedViewModel = hiltViewModel()
            LaunchedEffect(isUnlocked, authManager.isPinSet()) {
                if (authManager.isPinSet() && !isUnlocked) {
                    navController.navigate("lock") {
                        popUpTo("feed") { inclusive = true }
                    }
                }
            }
            MainAppScaffold(
                navController = navController,
                feedViewModel = feedViewModel,
                authManager = authManager,
                activity = activity
            )
        }
        composable("app_picker") {
            AppPickerScreen(onBack = { navController.popBackStack() })
        }
        composable(
            route = "detail/{id}",
            arguments = listOf(navArgument("id") { type = NavType.LongType })
        ) {
            DetailScreen(onBack = { navController.popBackStack() })
        }
    }
}

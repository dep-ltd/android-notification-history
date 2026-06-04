package com.notificationhistory.ui.adaptive

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.material3.adaptive.navigationsuite.NavigationSuiteScaffold
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import com.notificationhistory.R
import com.notificationhistory.security.AuthManager
import com.notificationhistory.ui.feed.FeedViewModel

private enum class MainDestination {
    FEED,
    SETTINGS
}

@Composable
fun MainAppScaffold(
    navController: NavHostController,
    feedViewModel: FeedViewModel,
    authManager: AuthManager,
    activity: FragmentActivity,
    modifier: Modifier = Modifier
) {
    var destination by rememberSaveable { mutableStateOf(MainDestination.FEED) }

    NavigationSuiteScaffold(
        modifier = modifier,
        navigationSuiteItems = {
            item(
                icon = {
                    Icon(
                        Icons.Default.History,
                        contentDescription = stringResource(R.string.nav_feed)
                    )
                },
                label = { Text(stringResource(R.string.nav_feed)) },
                selected = destination == MainDestination.FEED,
                onClick = { destination = MainDestination.FEED }
            )
            item(
                icon = {
                    Icon(
                        Icons.Default.Settings,
                        contentDescription = stringResource(R.string.nav_settings)
                    )
                },
                label = { Text(stringResource(R.string.nav_settings)) },
                selected = destination == MainDestination.SETTINGS,
                onClick = { destination = MainDestination.SETTINGS }
            )
        }
    ) {
        when (destination) {
            MainDestination.FEED -> FeedDetailPane(
                feedViewModel = feedViewModel,
                navController = navController,
                onOpenSettings = { destination = MainDestination.SETTINGS }
            )
            MainDestination.SETTINGS -> SettingsAdaptive(
                navController = navController,
                authManager = authManager,
                activity = activity,
                onBackFromRoot = { destination = MainDestination.FEED }
            )
        }
    }
}

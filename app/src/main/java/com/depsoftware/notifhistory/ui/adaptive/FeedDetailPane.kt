package com.depsoftware.notifhistory.ui.adaptive

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import com.depsoftware.notifhistory.R
import com.depsoftware.notifhistory.ui.detail.DetailScreen
import com.depsoftware.notifhistory.ui.feed.FeedScreen
import com.depsoftware.notifhistory.ui.feed.FeedViewModel

@Composable
fun FeedDetailPane(
    feedViewModel: FeedViewModel,
    navController: NavHostController,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isCompact = isCompactWidth()
    var selectedId by rememberSaveable { mutableStateOf<Long?>(null) }

    if (isCompact) {
        FeedScreen(
            modifier = modifier.adaptiveHingePadding(),
            viewModel = feedViewModel,
            onNotificationClick = { id -> navController.navigate("detail/$id") },
            onOpenSettings = onOpenSettings
        )
        return
    }

    Row(
        modifier = modifier
            .fillMaxSize()
            .adaptiveHingePadding()
    ) {
        FeedScreen(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.42f),
            viewModel = feedViewModel,
            onNotificationClick = { id -> selectedId = id },
            onOpenSettings = onOpenSettings,
            showSettingsInTopBar = false
        )
        Box(
            modifier = Modifier
                .fillMaxHeight()
                .weight(0.58f)
        ) {
            val id = selectedId
            if (id != null) {
                DetailScreen(
                    notificationId = id,
                    onBack = { selectedId = null },
                    showBack = false
                )
            } else {
                DetailPlaceholder()
            }
        }
    }
}

@Composable
private fun DetailPlaceholder() {
    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.Notifications,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = stringResource(R.string.detail_select_prompt),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.padding(top = 12.dp)
        )
    }
}

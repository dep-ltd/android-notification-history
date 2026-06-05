package com.depsoftware.notifhistory.ui.feed

import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.depsoftware.notifhistory.R
import com.depsoftware.notifhistory.data.entities.NotificationEvent
import com.depsoftware.notifhistory.data.entities.NotificationEventType
import com.depsoftware.notifhistory.ui.components.AppIcon
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: FeedViewModel,
    onNotificationClick: (Long) -> Unit,
    onOpenSettings: () -> Unit,
    modifier: Modifier = Modifier,
    showSettingsInTopBar: Boolean = true
) {
    val notifications by viewModel.notifications.collectAsState()
    val packages by viewModel.availablePackages.collectAsState()
    val selectedPackage by viewModel.packageFilterState.collectAsState()
    val hasActiveFilters by viewModel.hasActiveFilters.collectAsState()
    var searchText by remember { mutableStateOf("") }

    Scaffold(
        modifier = modifier,
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.feed_title)) },
                actions = {
                    if (showSettingsInTopBar) {
                        IconButton(onClick = onOpenSettings) {
                            Icon(
                                imageVector = Icons.Default.Settings,
                                contentDescription = stringResource(R.string.settings_title)
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            OutlinedTextField(
                value = searchText,
                onValueChange = {
                    searchText = it
                    viewModel.setSearchQuery(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 8.dp),
                placeholder = { Text(stringResource(R.string.feed_search_hint)) },
                singleLine = true
            )
            if (packages.isNotEmpty()) {
                Row(
                    modifier = Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = 16.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    FilterChip(
                        selected = selectedPackage == null,
                        onClick = { viewModel.setPackageFilter(null) },
                        label = { Text(stringResource(R.string.feed_filter_all)) }
                    )
                    packages.forEach { pkg ->
                        FilterChip(
                            selected = selectedPackage == pkg,
                            onClick = { viewModel.setPackageFilter(pkg) },
                            label = { Text(pkg.substringAfterLast('.')) }
                        )
                    }
                }
            }
            if (notifications.isEmpty()) {
                FeedEmptyState(
                    filtered = hasActiveFilters,
                    modifier = Modifier.weight(1f).fillMaxWidth()
                )
            } else {
                LazyColumn(
                    modifier = Modifier.weight(1f).fillMaxWidth(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(
                        items = notifications,
                        key = { it.id }
                    ) { event ->
                        NotificationItem(
                            event = event,
                            onClick = { onNotificationClick(event.id) }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(event: NotificationEvent, onClick: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth().clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            NotificationItemContent(event)
        }
    }
}

@Composable
private fun NotificationItemContent(event: NotificationEvent) {
    val removed = event.removedAt != null
    val eventType = NotificationEventType.fromStored(event.eventType)
    Column {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                AppIcon(packageName = event.packageName, size = 20.dp)
                Text(
                    text = event.appLabel ?: event.packageName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
            Text(
                text = formatFeedTime(event.postedAt),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = event.title ?: stringResource(R.string.detail_no_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.SemiBold,
            textDecoration = if (removed) TextDecoration.LineThrough else null
        )
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = event.text ?: stringResource(R.string.detail_no_text),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 3,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textDecoration = if (removed) TextDecoration.LineThrough else null
        )
        if (eventType == NotificationEventType.UPDATED || removed) {
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                if (eventType == NotificationEventType.UPDATED) {
                    AssistChip(
                        onClick = {},
                        enabled = false,
                        label = { Text(stringResource(R.string.feed_updated_badge)) }
                    )
                }
                if (removed) {
                    AssistChip(
                        onClick = {},
                        enabled = false,
                        label = { Text(stringResource(R.string.detail_status_removed)) },
                        colors = AssistChipDefaults.assistChipColors(
                            disabledContainerColor = MaterialTheme.colorScheme.errorContainer,
                            disabledLabelColor = MaterialTheme.colorScheme.onErrorContainer
                        )
                    )
                }
            }
        }
    }
}

@Composable
private fun FeedEmptyState(filtered: Boolean, modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.padding(horizontal = 32.dp, vertical = 24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            imageVector = Icons.Outlined.NotificationsNone,
            contentDescription = null,
            modifier = Modifier.size(56.dp),
            tint = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = stringResource(if (filtered) R.string.feed_empty_filtered else R.string.feed_empty),
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center
        )
        if (!filtered) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = stringResource(R.string.feed_empty_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.8f),
                textAlign = androidx.compose.ui.text.style.TextAlign.Center
            )
        }
    }
}

private fun formatFeedTime(timestamp: Long): String {
    val now = Calendar.getInstance()
    val posted = Calendar.getInstance().apply { timeInMillis = timestamp }
    val pattern = if (
        now.get(Calendar.YEAR) == posted.get(Calendar.YEAR) &&
        now.get(Calendar.DAY_OF_YEAR) == posted.get(Calendar.DAY_OF_YEAR)
    ) {
        "HH:mm"
    } else {
        "dd MMM, HH:mm"
    }
    return SimpleDateFormat(pattern, Locale.getDefault()).format(Date(timestamp))
}

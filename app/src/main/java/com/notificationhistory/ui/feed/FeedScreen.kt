package com.notificationhistory.ui.feed

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import com.notificationhistory.R
import com.notificationhistory.data.entities.NotificationEvent
import com.notificationhistory.data.models.FeedListItem
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FeedScreen(
    viewModel: FeedViewModel,
    onNotificationClick: (Long) -> Unit,
    onOpenSettings: () -> Unit
) {
    val feedItems by viewModel.feedItems.collectAsState()
    val packages by viewModel.availablePackages.collectAsState()
    val selectedPackage by viewModel.packageFilterState.collectAsState()
    var searchText by remember { mutableStateOf("") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.feed_title)) },
                actions = {
                    IconButton(onClick = onOpenSettings) {
                        Icon(
                            imageVector = Icons.Default.Settings,
                            contentDescription = stringResource(R.string.settings_title)
                        )
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
            if (feedItems.isEmpty()) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(stringResource(R.string.feed_empty))
                }
            } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(16.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(
                    items = feedItems,
                    key = { item ->
                        when (item) {
                            is FeedListItem.Single -> "single-${item.event.id}"
                            is FeedListItem.Group -> "group-${item.groupKey}"
                        }
                    }
                ) { item ->
                    when (item) {
                        is FeedListItem.Single -> NotificationItem(
                            event = item.event,
                            onClick = { onNotificationClick(item.event.id) }
                        )
                        is FeedListItem.Group -> GroupFeedItem(
                            item = item,
                            onToggle = { viewModel.toggleGroup(item.groupKey) },
                            onSummaryClick = item.summary?.let { summary ->
                                { onNotificationClick(summary.id) }
                            },
                            onChildClick = onNotificationClick
                        )
                    }
                }
            }
            }
        }
    }
}

@Composable
private fun GroupFeedItem(
    item: FeedListItem.Group,
    onToggle: () -> Unit,
    onSummaryClick: (() -> Unit)?,
    onChildClick: (Long) -> Unit
) {
    val display = item.summary ?: item.children.firstOrNull() ?: return
    val childCount = item.children.size

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(enabled = onSummaryClick != null, onClick = { onSummaryClick?.invoke() }),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    NotificationItemContent(display)
                    if (childCount > 0) {
                        Text(
                            text = stringResource(R.string.feed_group_count, childCount),
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
                if (childCount > 0) {
                    IconButton(onClick = onToggle) {
                        Icon(
                            imageVector = if (item.isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = stringResource(
                                if (item.isExpanded) R.string.feed_collapse_group else R.string.feed_expand_group
                            )
                        )
                    }
                }
            }
            AnimatedVisibility(visible = item.isExpanded && childCount > 0) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item.children.forEach { child ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onChildClick(child.id) },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Box(modifier = Modifier.padding(12.dp)) {
                                NotificationItemContent(child)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun NotificationItem(
    event: NotificationEvent,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Box(modifier = Modifier.padding(16.dp)) {
            NotificationItemContent(event)
        }
    }
}

@Composable
private fun NotificationItemContent(event: NotificationEvent) {
    val removed = event.removedAt != null
    Column {
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
            Text(
                text = event.appLabel ?: event.packageName,
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = formatTime(event.postedAt),
                style = MaterialTheme.typography.labelSmall
            )
        }
        Spacer(modifier = Modifier.height(4.dp))
        Text(
            text = event.title ?: stringResource(R.string.detail_no_title),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = FontWeight.Bold,
            textDecoration = if (removed) TextDecoration.LineThrough else null
        )
        Text(
            text = event.text ?: stringResource(R.string.detail_no_text),
            style = MaterialTheme.typography.bodyMedium,
            maxLines = 2,
            textDecoration = if (removed) TextDecoration.LineThrough else null
        )
        if (event.eventType == "UPDATED") {
            Text(
                text = stringResource(R.string.feed_updated_badge),
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.tertiary
            )
        }
    }
}

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

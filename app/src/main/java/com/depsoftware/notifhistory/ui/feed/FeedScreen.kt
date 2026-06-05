package com.depsoftware.notifhistory.ui.feed

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.NotificationsNone
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.depsoftware.notifhistory.R
import com.depsoftware.notifhistory.data.entities.NotificationEvent
import com.depsoftware.notifhistory.data.models.FeedListItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.text.SimpleDateFormat
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
    val feedItems by viewModel.feedItems.collectAsState()
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
            if (feedItems.isEmpty()) {
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
                                onEventClick = onNotificationClick
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
    onEventClick: (Long) -> Unit
) {
    val newest = item.events.first()
    val appLabel = newest.appLabel ?: newest.packageName

    Card(
        modifier = Modifier.fillMaxWidth(),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onEventClick(newest.id) },
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(
                    modifier = Modifier.weight(1f),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    AppIcon(packageName = newest.packageName, size = 22.dp)
                    Column {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(6.dp)
                        ) {
                            Text(
                                text = appLabel,
                                style = MaterialTheme.typography.labelMedium,
                                color = MaterialTheme.colorScheme.primary
                            )
                            Surface(
                                shape = MaterialTheme.shapes.small,
                                color = MaterialTheme.colorScheme.primaryContainer
                            ) {
                                Text(
                                    text = "${item.events.size}",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.padding(horizontal = 5.dp, vertical = 1.dp)
                                )
                            }
                        }
                        Text(
                            text = newest.title ?: stringResource(R.string.detail_no_title),
                            style = MaterialTheme.typography.titleSmall,
                            fontWeight = FontWeight.SemiBold
                        )
                        if (!newest.text.isNullOrBlank()) {
                            Text(
                                text = newest.text,
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1
                            )
                        }
                    }
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = formatTime(newest.postedAt),
                        style = MaterialTheme.typography.labelSmall
                    )
                    IconButton(onClick = onToggle, modifier = Modifier.size(36.dp)) {
                        Icon(
                            imageVector = if (item.isExpanded) Icons.Default.ExpandLess else Icons.Default.ExpandMore,
                            contentDescription = stringResource(
                                if (item.isExpanded) R.string.feed_collapse_group else R.string.feed_expand_group
                            )
                        )
                    }
                }
            }
            AnimatedVisibility(visible = item.isExpanded) {
                Column(
                    modifier = Modifier.padding(top = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    item.events.forEach { event ->
                        Card(
                            modifier = Modifier
                                .fillMaxWidth()
                                .clickable { onEventClick(event.id) },
                            colors = CardDefaults.cardColors(
                                containerColor = MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Box(modifier = Modifier.padding(12.dp)) {
                                NotificationItemContent(event)
                            }
                        }
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
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                AppIcon(packageName = event.packageName, size = 18.dp)
                Text(
                    text = event.appLabel ?: event.packageName,
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.primary
                )
            }
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

@Composable
private fun AppIcon(packageName: String, size: Dp = 18.dp) {
    val context = LocalContext.current
    val icon by produceState<ImageBitmap?>(initialValue = null, key1 = packageName) {
        value = withContext(Dispatchers.Default) {
            try {
                val d = context.packageManager.getApplicationIcon(packageName)
                val w = d.intrinsicWidth.coerceIn(1, 192)
                val h = d.intrinsicHeight.coerceIn(1, 192)
                val bmp = Bitmap.createBitmap(w, h, Bitmap.Config.ARGB_8888)
                Canvas(bmp).also { c -> d.setBounds(0, 0, w, h); d.draw(c) }
                bmp.asImageBitmap()
            } catch (_: Exception) {
                null
            }
        }
    }
    val img = icon
    if (img != null) {
        Image(
            bitmap = img,
            contentDescription = null,
            modifier = Modifier.size(size)
        )
    } else {
        Spacer(Modifier.size(size))
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

private fun formatTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("HH:mm", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

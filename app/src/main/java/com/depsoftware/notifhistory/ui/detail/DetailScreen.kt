package com.depsoftware.notifhistory.ui.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.OpenInNew
import androidx.compose.material3.*
import com.depsoftware.notifhistory.ui.adaptive.isExpandedWidth
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.depsoftware.notifhistory.data.entities.NotificationEvent
import com.depsoftware.notifhistory.data.entities.NotificationEventType
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextDecoration
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.depsoftware.notifhistory.R
import com.depsoftware.notifhistory.ui.components.AppIcon
import java.io.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DetailScreen(
    onBack: () -> Unit,
    notificationId: Long? = null,
    showBack: Boolean = true,
    viewModel: DetailViewModel = hiltViewModel(
        key = notificationId?.let { "detail_pane_$it" } ?: "detail_nav"
    )
) {
    val event by viewModel.notification.collectAsState()
    val context = LocalContext.current
    val isExpanded = isExpandedWidth()

    LaunchedEffect(notificationId) {
        notificationId?.let { viewModel.bindNotificationId(it) }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.detail_title)) },
                navigationIcon = {
                    if (showBack) {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.detail_back)
                            )
                        }
                    }
                }
            )
        }
    ) { padding ->
        val item = event
        if (item == null) {
            Box(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
            return@Scaffold
        }

        if (isExpanded) {
            DetailExpandedLayout(
                item = item,
                context = context,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(horizontal = 24.dp, vertical = 16.dp)
            )
        } else {
            DetailCompactLayout(
                item = item,
                context = context,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
            )
        }
    }
}

@Composable
private fun DetailCompactLayout(
    item: NotificationEvent,
    context: android.content.Context,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .verticalScroll(rememberScrollState())
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DetailHeaderCard(item)
        DetailMediaSection(item, context)
        DetailContentCard(item)
        DetailMetaCard(item)
        item.clickUri?.let { DetailLinkButton(it, context) }
    }
}

@Composable
private fun DetailExpandedLayout(
    item: NotificationEvent,
    context: android.content.Context,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        Column(
            modifier = Modifier
                .weight(0.38f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailHeaderCard(item)
            DetailMetaCard(item)
            item.clickUri?.let { DetailLinkButton(it, context) }
        }
        Column(
            modifier = Modifier
                .weight(0.62f)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailMediaSection(item, context, imageHeight = 220.dp)
            DetailContentCard(item)
        }
    }
}

@Composable
private fun DetailHeaderCard(item: NotificationEvent) {
    val eventType = NotificationEventType.fromStored(item.eventType)
    val removed = item.removedAt != null

    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.spacedBy(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppIcon(packageName = item.packageName, size = 48.dp)
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.appLabel ?: item.packageName,
                    style = MaterialTheme.typography.titleMedium,
                    fontWeight = FontWeight.SemiBold
                )
                Text(
                    text = item.packageName,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = stringResource(R.string.detail_received_at, formatDateTime(item.postedAt)),
                    style = MaterialTheme.typography.labelMedium
                )
                item.updatedAt?.let { updated ->
                    Text(
                        text = stringResource(R.string.detail_updated_at, formatDateTime(updated)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
                item.removedAt?.let { removedAt ->
                    Text(
                        text = stringResource(R.string.detail_removed_at, formatDateTime(removedAt)),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
        if (eventType == NotificationEventType.UPDATED || removed) {
            Row(
                modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 12.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
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
private fun DetailContentCard(item: NotificationEvent) {
    val removed = item.removedAt != null
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Text(
                text = stringResource(R.string.detail_message),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            Text(
                text = item.title ?: stringResource(R.string.detail_no_title),
                style = MaterialTheme.typography.headlineSmall,
                fontWeight = FontWeight.Bold,
                textDecoration = if (removed) TextDecoration.LineThrough else null
            )
            Text(
                text = item.text ?: stringResource(R.string.detail_no_text),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textDecoration = if (removed) TextDecoration.LineThrough else null
            )
        }
    }
}

@Composable
private fun DetailMetaCard(item: NotificationEvent) {
    Card(
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
        ) {
            Text(
                text = stringResource(R.string.detail_info),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
            item.channelId?.let { channel ->
                DetailMetaRow(
                    label = stringResource(R.string.detail_channel_label),
                    value = channel
                )
            }
            DetailMetaRow(
                label = stringResource(R.string.detail_package_label),
                value = item.packageName
            )
            item.groupKey?.let { groupKey ->
                DetailMetaRow(
                    label = stringResource(R.string.detail_group_label),
                    value = groupKey
                )
            }
        }
    }
}

@Composable
private fun DetailMetaRow(label: String, value: String) {
    Column {
        Text(
            text = label,
            style = MaterialTheme.typography.labelSmall,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
        Text(
            text = value,
            style = MaterialTheme.typography.bodySmall
        )
    }
}

@Composable
private fun DetailMediaSection(
    item: NotificationEvent,
    context: android.content.Context,
    imageHeight: androidx.compose.ui.unit.Dp = 180.dp
) {
    if (item.mediaPaths.isEmpty()) return
    Card {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            Text(
                text = stringResource(R.string.detail_attachments),
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary,
                modifier = Modifier.padding(horizontal = 4.dp)
            )
            LazyRow(horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                items(item.mediaPaths, key = { it }) { path ->
                    AsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(Uri.fromFile(File(path)))
                            .crossfade(true)
                            .build(),
                        contentDescription = stringResource(R.string.detail_image),
                        modifier = Modifier
                            .height(imageHeight)
                            .width(imageHeight * 1.2f)
                            .clip(RoundedCornerShape(12.dp)),
                        contentScale = ContentScale.Crop
                    )
                }
            }
        }
    }
}

@Composable
private fun DetailLinkButton(uri: String, context: android.content.Context) {
    FilledTonalButton(
        onClick = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            ContextCompat.startActivity(context, intent, null)
        },
        modifier = Modifier.fillMaxWidth()
    ) {
        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(stringResource(R.string.detail_open_link))
    }
}

private fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

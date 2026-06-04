package com.depsoftware.notifhistory.ui.detail

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import com.depsoftware.notifhistory.data.entities.NotificationEvent
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.core.content.ContextCompat
import androidx.hilt.navigation.compose.hiltViewModel
import coil.compose.AsyncImage
import coil.request.ImageRequest
import com.depsoftware.notifhistory.R
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
                    .padding(16.dp)
            )
        } else {
            DetailCompactLayout(
                item = item,
                context = context,
                modifier = Modifier
                    .fillMaxSize()
                    .padding(padding)
                    .padding(16.dp)
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
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        DetailMediaRow(item, context)
        DetailMetaBlock(item, context)
        HorizontalDivider()
        DetailTextBlock(item)
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
        horizontalArrangement = Arrangement.spacedBy(24.dp)
    ) {
        Column(
            modifier = Modifier.weight(0.4f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailMetaBlock(item, context, includeLink = false)
            item.clickUri?.let { DetailLinkButton(it, context) }
        }
        Column(
            modifier = Modifier.weight(0.6f),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            DetailMediaRow(item, context, fillFraction = 0.45f)
            HorizontalDivider()
            DetailTextBlock(item)
        }
    }
}

@Composable
private fun DetailMediaRow(
    item: NotificationEvent,
    context: android.content.Context,
    fillFraction: Float = 0.6f
) {
    if (item.mediaPaths.isEmpty()) return
    LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        items(item.mediaPaths, key = { it }) { path ->
            AsyncImage(
                model = ImageRequest.Builder(context)
                    .data(Uri.fromFile(File(path)))
                    .crossfade(true)
                    .build(),
                contentDescription = stringResource(R.string.detail_image),
                modifier = Modifier
                    .height(160.dp)
                    .fillMaxWidth(fillFraction),
                contentScale = ContentScale.Crop
            )
        }
    }
}

@Composable
private fun DetailMetaBlock(
    item: NotificationEvent,
    context: android.content.Context,
    includeLink: Boolean = true
) {
    Text(
        text = item.appLabel ?: item.packageName,
        style = MaterialTheme.typography.titleLarge
    )
    Text(
        text = item.packageName,
        style = MaterialTheme.typography.bodySmall,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
    Text(
        text = formatDateTime(item.postedAt),
        style = MaterialTheme.typography.labelMedium
    )
    item.updatedAt?.let { updated ->
        Text(
            text = stringResource(R.string.detail_updated_at, formatDateTime(updated)),
            style = MaterialTheme.typography.labelSmall
        )
    }
    item.channelId?.let { channel ->
        Text(
            text = stringResource(R.string.detail_channel, channel),
            style = MaterialTheme.typography.bodySmall
        )
    }
    if (includeLink) {
        item.clickUri?.let { DetailLinkButton(it, context) }
    }
}

@Composable
private fun DetailLinkButton(uri: String, context: android.content.Context) {
    TextButton(
        onClick = {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(uri))
            ContextCompat.startActivity(context, intent, null)
        }
    ) {
        Icon(Icons.AutoMirrored.Filled.OpenInNew, contentDescription = null)
        Spacer(modifier = Modifier.width(8.dp))
        Text(stringResource(R.string.detail_open_link))
    }
}

@Composable
private fun DetailTextBlock(item: NotificationEvent) {
    Text(
        text = item.title ?: stringResource(R.string.detail_no_title),
        style = MaterialTheme.typography.headlineSmall
    )
    Text(
        text = item.text ?: stringResource(R.string.detail_no_text),
        style = MaterialTheme.typography.bodyLarge
    )
}

private fun formatDateTime(timestamp: Long): String {
    val sdf = SimpleDateFormat("dd MMM yyyy, HH:mm:ss", Locale.getDefault())
    return sdf.format(Date(timestamp))
}

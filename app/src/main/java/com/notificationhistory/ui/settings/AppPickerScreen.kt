package com.notificationhistory.ui.settings

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import com.notificationhistory.R

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AppPickerScreen(
    onBack: () -> Unit,
    showTopBar: Boolean = true,
    embedded: Boolean = false,
    viewModel: AppPickerViewModel = hiltViewModel()
) {
    val apps by viewModel.apps.collectAsState()
    var search by remember { mutableStateOf("") }

    val content: @Composable (Modifier) -> Unit = { contentModifier ->
        Column(modifier = contentModifier.fillMaxSize()) {
            OutlinedTextField(
                value = search,
                onValueChange = {
                    search = it
                    viewModel.setSearchQuery(it)
                },
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                placeholder = { Text(stringResource(R.string.app_picker_search)) },
                singleLine = true
            )
            LazyColumn(
                contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                verticalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                items(apps, key = { it.packageName }) { app ->
                    ListItem(
                        headlineContent = { Text(app.label) },
                        supportingContent = { Text(app.packageName) },
                        trailingContent = {
                            Switch(
                                checked = app.isBlacklisted,
                                onCheckedChange = { checked ->
                                    viewModel.toggleBlacklist(app.packageName, checked)
                                }
                            )
                        }
                    )
                    HorizontalDivider()
                }
            }
        }
    }

    if (showTopBar) {
        Scaffold(
            topBar = {
                TopAppBar(
                    title = { Text(stringResource(R.string.app_picker_title)) },
                    navigationIcon = {
                        IconButton(onClick = onBack) {
                            Icon(
                                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                                contentDescription = stringResource(R.string.detail_back)
                            )
                        }
                    }
                )
            }
        ) { padding -> content(Modifier.padding(padding)) }
    } else {
        Column(modifier = Modifier.fillMaxSize().padding(if (embedded) 0.dp else 16.dp)) {
            if (embedded) {
                Text(
                    text = stringResource(R.string.app_picker_title),
                    style = MaterialTheme.typography.titleLarge,
                    modifier = Modifier.padding(bottom = 8.dp)
                )
            }
            content(Modifier)
        }
    }
}

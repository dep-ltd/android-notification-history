package com.depsoftware.notifhistory.ui.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.FilterChip
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.hilt.navigation.compose.hiltViewModel
import com.depsoftware.notifhistory.R
import com.depsoftware.notifhistory.security.AuthManager
import com.depsoftware.notifhistory.security.BiometricGate
import com.depsoftware.notifhistory.ui.adaptive.SettingsSection

@Composable
fun SettingsPanel(
    section: SettingsSection? = null,
    authManager: AuthManager,
    activity: FragmentActivity,
    onOpenAppPicker: (() -> Unit)? = null,
    modifier: Modifier = Modifier,
    viewModel: SettingsViewModel = hiltViewModel()
) {
    var biometricEnabled by remember { mutableStateOf(authManager.isBiometricEnabled()) }
    val canUseBiometric = BiometricGate.canAuthenticate(activity)
    val retentionDays by viewModel.retentionDays.collectAsState()
    val lockTimeout by viewModel.lockTimeoutMinutes.collectAsState()
    var showClearDialog by remember { mutableStateOf(false) }

    val showSecurity = section == null || section == SettingsSection.SECURITY
    val showData = section == null || section == SettingsSection.DATA
    val showApps = section == null || section == SettingsSection.APPS

    if (showClearDialog) {
        AlertDialog(
            onDismissRequest = { showClearDialog = false },
            title = { Text(stringResource(R.string.settings_clear_history_title)) },
            text = { Text(stringResource(R.string.settings_clear_history_message)) },
            confirmButton = {
                TextButton(
                    onClick = {
                        viewModel.clearHistory { showClearDialog = false }
                    }
                ) {
                    Text(stringResource(R.string.settings_clear_history_confirm))
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text(stringResource(R.string.settings_clear_history_cancel))
                }
            }
        )
    }

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        if (showSecurity) {
            Text(
                text = stringResource(R.string.settings_section_security),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Card(modifier = Modifier.fillMaxWidth()) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(16.dp),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Column(modifier = Modifier.weight(1f)) {
                        Text(
                            text = stringResource(R.string.settings_biometric),
                            style = MaterialTheme.typography.titleMedium
                        )
                        Text(
                            text = stringResource(
                                if (canUseBiometric) {
                                    R.string.settings_biometric_hint
                                } else {
                                    R.string.settings_biometric_unavailable
                                }
                            ),
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    Switch(
                        checked = biometricEnabled,
                        onCheckedChange = {
                            biometricEnabled = it
                            authManager.setBiometricEnabled(it)
                        },
                        enabled = canUseBiometric
                    )
                }
            }
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.settings_lock_timeout),
                        style = MaterialTheme.typography.titleMedium
                    )
                    val options = listOf(0, 1, 5)
                    Row(
                        modifier = Modifier.padding(top = 8.dp),
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        options.forEach { minutes ->
                            FilterChip(
                                selected = lockTimeout == minutes,
                                onClick = { viewModel.setLockTimeoutMinutes(minutes) },
                                label = {
                                    Text(
                                        when (minutes) {
                                            0 -> stringResource(R.string.settings_lock_immediate)
                                            1 -> stringResource(R.string.settings_lock_1min)
                                            else -> stringResource(R.string.settings_lock_5min)
                                        }
                                    )
                                }
                            )
                        }
                    }
                }
            }
        }

        if (showData) {
            Text(
                text = stringResource(R.string.settings_section_data),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Card(modifier = Modifier.fillMaxWidth()) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.settings_retention, retentionDays),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Slider(
                        value = retentionDays.toFloat(),
                        onValueChange = { viewModel.setRetentionDays(it.toInt()) },
                        valueRange = 7f..365f,
                        steps = 10
                    )
                    Text(
                        text = stringResource(R.string.settings_retention_hint),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
            OutlinedButton(
                onClick = { showClearDialog = true },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(stringResource(R.string.settings_clear_history))
            }
        }

        if (showApps && onOpenAppPicker != null) {
            Text(
                text = stringResource(R.string.settings_section_apps),
                style = MaterialTheme.typography.titleSmall,
                color = MaterialTheme.colorScheme.primary
            )
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable(onClick = onOpenAppPicker)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = stringResource(R.string.settings_ignore_apps),
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = stringResource(R.string.settings_ignore_apps_hint),
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }
        }
    }
}

package com.notificationhistory.ui.adaptive

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Card
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ListItem
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.fragment.app.FragmentActivity
import androidx.navigation.NavHostController
import com.notificationhistory.R
import com.notificationhistory.security.AuthManager
import com.notificationhistory.ui.settings.AppPickerScreen
import com.notificationhistory.ui.settings.SettingsPanel
import com.notificationhistory.ui.settings.SettingsScreen

enum class SettingsSection {
    SECURITY,
    DATA,
    APPS
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsAdaptive(
    navController: NavHostController,
    authManager: AuthManager,
    activity: FragmentActivity,
    onBackFromRoot: () -> Unit
) {
    val isCompact = isCompactWidth()

    if (isCompact) {
        SettingsScreen(
            onBack = onBackFromRoot,
            onOpenAppPicker = { navController.navigate("app_picker") },
            authManager = authManager,
            activity = activity
        )
        return
    }

    var selectedSection by rememberSaveable { mutableStateOf(SettingsSection.SECURITY) }

    Row(
        modifier = Modifier
            .fillMaxSize()
            .adaptiveHingePadding()
    ) {
        SettingsSectionList(
            modifier = Modifier
                .widthIn(min = 200.dp, max = 320.dp)
                .fillMaxHeight(),
            selected = selectedSection,
            onSelect = { selectedSection = it }
        )
        Column(
            modifier = Modifier
                .weight(1f)
                .fillMaxHeight()
                .padding(16.dp)
        ) {
            when (selectedSection) {
                SettingsSection.SECURITY, SettingsSection.DATA -> SettingsPanel(
                    section = selectedSection,
                    authManager = authManager,
                    activity = activity
                )
                SettingsSection.APPS -> Box(modifier = Modifier.fillMaxSize()) {
                    AppPickerScreen(
                        onBack = {},
                        showTopBar = false,
                        embedded = true
                    )
                }
            }
        }
    }
}

@Composable
private fun SettingsSectionList(
    selected: SettingsSection,
    onSelect: (SettingsSection) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(modifier = modifier.padding(8.dp)) {
        Text(
            text = stringResource(R.string.settings_title),
            style = MaterialTheme.typography.titleLarge,
            modifier = Modifier.padding(16.dp)
        )
        SettingsSection.entries.forEach { section ->
            val label = when (section) {
                SettingsSection.SECURITY -> stringResource(R.string.settings_section_security)
                SettingsSection.DATA -> stringResource(R.string.settings_section_data)
                SettingsSection.APPS -> stringResource(R.string.settings_section_apps)
            }
            Card(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 8.dp, vertical = 4.dp)
                    .clickable { onSelect(section) }
            ) {
                ListItem(
                    headlineContent = {
                        Text(
                            text = label,
                            color = if (section == selected) {
                                MaterialTheme.colorScheme.primary
                            } else {
                                MaterialTheme.colorScheme.onSurface
                            }
                        )
                    }
                )
            }
        }
    }
}

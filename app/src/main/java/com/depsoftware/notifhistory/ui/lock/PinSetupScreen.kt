package com.depsoftware.notifhistory.ui.lock

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.depsoftware.notifhistory.R

@Composable
fun PinSetupScreen(
    onPinConfirmed: (String) -> Unit,
    hasAcceptedPolicy: Boolean,
    onAcceptPolicy: (Boolean) -> Unit
) {
    var step by remember { mutableIntStateOf(1) }
    var firstPin by remember { mutableStateOf("") }
    var error by remember { mutableStateOf<String?>(null) }
    var numpadKey by remember { mutableIntStateOf(0) }
    val mismatchMessage = stringResource(R.string.pin_setup_mismatch)

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.Lock,
            contentDescription = null,
            modifier = Modifier.size(64.dp),
            tint = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(16.dp))
        Text(
            text = if (step == 1) {
                stringResource(R.string.pin_setup_create)
            } else {
                stringResource(R.string.pin_setup_confirm)
            },
            style = MaterialTheme.typography.headlineMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.height(12.dp))
        Card(
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.errorContainer
            ),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(
                text = stringResource(R.string.pin_setup_device_pin_warning),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onErrorContainer,
                modifier = Modifier.padding(12.dp)
            )
        }
        Spacer(modifier = Modifier.height(16.dp))
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(checked = hasAcceptedPolicy, onCheckedChange = onAcceptPolicy)
            Text(
                text = stringResource(R.string.pin_setup_wipe_consent),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                modifier = Modifier.weight(1f)
            )
        }
        error?.let {
            Spacer(modifier = Modifier.height(8.dp))
            Text(it, color = MaterialTheme.colorScheme.error, textAlign = TextAlign.Center)
        }
        Spacer(modifier = Modifier.height(16.dp))
        key(numpadKey) {
            Numpad(
                enabled = hasAcceptedPolicy,
                onPinComplete = { entered ->
                    when (step) {
                        1 -> {
                            firstPin = entered
                            step = 2
                            error = null
                            numpadKey++
                        }
                        2 -> {
                            if (entered == firstPin) {
                                onPinConfirmed(entered)
                            } else {
                                error = mismatchMessage
                                step = 1
                                firstPin = ""
                                numpadKey++
                            }
                        }
                    }
                }
            )
        }
    }
}

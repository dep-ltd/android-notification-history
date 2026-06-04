package com.depsoftware.notifhistory.ui.lock

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Fingerprint
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import com.depsoftware.notifhistory.R

@Composable
fun LockScreen(
    remainingAttempts: Int,
    showBiometric: Boolean,
    onPinEntered: (String) -> Unit,
    onBiometricClick: () -> Unit
) {
    LaunchedEffect(showBiometric) {
        if (showBiometric) onBiometricClick()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text(stringResource(R.string.lock_enter_pin), style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(8.dp))
        Text(
            text = stringResource(R.string.lock_attempts_left, remainingAttempts),
            color = MaterialTheme.colorScheme.error,
            style = MaterialTheme.typography.bodyMedium
        )
        Spacer(modifier = Modifier.height(24.dp))
        Numpad(onPinComplete = onPinEntered)
        if (showBiometric) {
            Spacer(modifier = Modifier.height(24.dp))
            OutlinedButton(onClick = onBiometricClick) {
                Icon(Icons.Default.Fingerprint, contentDescription = null)
                Spacer(modifier = Modifier.width(8.dp))
                Text(stringResource(R.string.lock_use_biometric))
            }
        }
    }
}

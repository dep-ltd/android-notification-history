package com.depsoftware.notifhistory.ui.lock

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun PinDots(pinLength: Int, modifier: Modifier = Modifier) {
    Row(modifier = modifier, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        repeat(6) { index ->
            Text(if (index < pinLength) "●" else "○", fontSize = 24.sp)
        }
    }
}

@Composable
fun Numpad(
    enabled: Boolean = true,
    onPinComplete: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var pin by remember { mutableStateOf("") }
    val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "C", "0", "OK")

    Column(modifier = modifier, horizontalAlignment = androidx.compose.ui.Alignment.CenterHorizontally) {
        PinDots(pin.length)
        Spacer(modifier = Modifier.height(24.dp))
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.width(280.dp)
        ) {
            items(keys) { key ->
                when (key) {
                    "C" -> Button(
                        onClick = { if (enabled && pin.isNotEmpty()) pin = pin.dropLast(1) },
                        enabled = enabled,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.errorContainer,
                            contentColor = MaterialTheme.colorScheme.onErrorContainer
                        ),
                        modifier = Modifier.aspectRatio(1f)
                    ) {
                        Text(key, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    "OK" -> Button(
                        onClick = { if (enabled && pin.length == 6) onPinComplete(pin) },
                        enabled = enabled && pin.length == 6,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary,
                            contentColor = MaterialTheme.colorScheme.onPrimary
                        ),
                        modifier = Modifier.aspectRatio(1f)
                    ) {
                        Text(key, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                    else -> OutlinedButton(
                        onClick = { if (enabled && pin.length < 6) pin += key },
                        enabled = enabled,
                        modifier = Modifier.aspectRatio(1f)
                    ) {
                        Text(key, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

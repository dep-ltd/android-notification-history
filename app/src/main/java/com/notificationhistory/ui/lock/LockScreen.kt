package com.notificationhistory.ui.lock

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

@Composable
fun LockScreen(
    remainingAttempts: Int,
    onPinEntered: (String) -> Unit
) {
    var pin by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(Icons.Default.Lock, contentDescription = null, modifier = Modifier.size(64.dp))
        Spacer(modifier = Modifier.height(16.dp))
        Text("Enter PIN", style = MaterialTheme.typography.headlineMedium)
        Text("Attempts left: $remainingAttempts", color = MaterialTheme.colorScheme.error)
        
        Spacer(modifier = Modifier.height(32.dp))
        
        // PIN Display
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            repeat(6) { index ->
                val char = if (index < pin.length) "●" else "○"
                Text(char, fontSize = 24.sp)
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Custom Numpad
        val keys = listOf("1", "2", "3", "4", "5", "6", "7", "8", "9", "C", "0", "OK")
        LazyVerticalGrid(
            columns = GridCells.Fixed(3),
            horizontalArrangement = Arrangement.spacedBy(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp),
            modifier = Modifier.width(280.dp)
        ) {
            items(keys) { key ->
                Button(
                    onClick = {
                        when (key) {
                            "C" -> if (pin.isNotEmpty()) pin = pin.dropLast(1)
                            "OK" -> if (pin.length == 6) onPinEntered(pin)
                            else -> if (pin.length < 6) pin += key
                        }
                    },
                    modifier = Modifier.aspectRatio(1f)
                ) {
                    Text(key, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

package com.notificationhistory.ui.lock

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.Button
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
                Button(
                    onClick = {
                        if (!enabled) return@Button
                        when (key) {
                            "C" -> if (pin.isNotEmpty()) pin = pin.dropLast(1)
                            "OK" -> if (pin.length == 6) onPinComplete(pin)
                            else -> if (pin.length < 6) pin += key
                        }
                    },
                    enabled = enabled,
                    modifier = Modifier.aspectRatio(1f)
                ) {
                    Text(key, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                }
            }
        }
    }
}

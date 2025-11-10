package com.blitzapp.remote.ui

import android.graphics.BitmapFactory
import android.util.Base64
import android.util.Log
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.blitzapp.remote.BluetoothDevice
import com.blitzapp.remote.MediaInfo
import com.blitzapp.remote.WiFiInfo
import com.blitzapp.remote.ui.theme.*

// Helper function to format doubles
private fun Double.format(decimals: Int) = "%.${decimals}f".format(this)

// Helper function to format network speed with auto-scaling
private fun formatSpeed(mbps: Double?): String {
    Log.d("wifi speed" , mbps.toString())
    if (mbps == null) return "0 Kbps"
    return when {
        mbps >= 1000 -> "${(mbps / 1000).format(1)} Gbps"
        mbps >= 1 -> "${mbps.format(1)} Mbps"
        else -> "${(mbps * 1024).toInt()} Kbps"
    }
}

@Composable
fun WifiSpeedIndicator(wifiInfo: WiFiInfo?) {
    // Show the indicator if wifi is connected
    if (wifiInfo != null && wifiInfo.connected == true) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(16.dp),
            contentAlignment = Alignment.TopEnd
        ) {
            val downloadSpeed = formatSpeed(wifiInfo.downloadSpeed)
            val uploadSpeed = formatSpeed(wifiInfo.uploadSpeed)
            Text(
                text = "↓$downloadSpeed / ↑$uploadSpeed",
                color = TextSecondary,
                fontSize = 12.sp,
                modifier = Modifier
                    .background(CardBackground.copy(alpha = 0.8f), RoundedCornerShape(8.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            )
        }
    }
}

@Composable
fun ErrorCard(error: String?) {
    if (!error.isNullOrEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Error)
        ) {
            Row(
                modifier = Modifier.padding(16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "❌", fontSize = 24.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = error,
                    color = TextPrimary,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun Header() {
    Row(modifier = Modifier.padding(16.dp)) {
        Text(
            text = "⚡ BLITZ REMOTE",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = Color.White,
        )
    }
}

@Composable
fun ConnectionCard(
    isConnected: Boolean,
    onConnect: (String, String, String) -> Unit
) {
    var ipAddress by remember { mutableStateOf("192.168.1.109") }
    var port by remember { mutableStateOf("8765") }
    var path by remember { mutableStateOf("/ws") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Connection", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            TextField(
                value = ipAddress,
                onValueChange = { ipAddress = it },
                label = { Text("IP Address") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = port,
                onValueChange = { port = it },
                label = { Text("Port") }
            )
            Spacer(modifier = Modifier.height(8.dp))
            TextField(
                value = path,
                onValueChange = { path = it },
                label = { Text("Path") }
            )
            Spacer(modifier = Modifier.height(16.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(onClick = { onConnect(ipAddress, port, path) }) {
                    Text(text = "CONNECT")
                }
                Spacer(modifier = Modifier.weight(1f))
                Text(
                    text = if (isConnected) "● CONNECTED" else "○ DISCONNECTED",
                    color = if (isConnected) Success else Error,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun NowPlayingCard(
    mediaInfo: MediaInfo?,
    onCommand: (String) -> Unit,
    artWork: String?
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = "🎵 NOW PLAYING",
                style = MaterialTheme.typography.titleMedium,
                color = PrimaryAccent
            )

            Spacer(modifier = Modifier.height(12.dp))

            // Robust artwork handling
            val artworkData = mediaInfo?.albumArt ?: null

            // Log the artwork data to see its value in Logcat
          //  Log.d("ArtworkDebug", "Artwork data received: $artworkData")

            Box(
                modifier = Modifier
                    .size(160.dp)
                    .clip(RoundedCornerShape(16.dp))
                    .border(2.dp, PrimaryAccent, RoundedCornerShape(16.dp))
                    .background(Color.Black) // Fallback background
            ) {
                if (artworkData.isNullOrBlank()) {
                    // Placeholder when no artwork is available
                    Text("🖼no artwork", fontSize = 80.sp, textAlign = TextAlign.Center, modifier = Modifier.align(Alignment.Center))
                } else if (artworkData.startsWith("data:")) {
                    // Handle Base64 encoded image
                    val imageBitmap = remember(artworkData) {
                        try {

                            val pureBase64 = artworkData.substringAfter(',')
                            val decodedBytes = Base64.decode(pureBase64, Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)?.asImageBitmap()
                        } catch (e: Exception) {
                            null
                        }
                    }
                    if (imageBitmap != null) {
                        Image(
                            bitmap = imageBitmap,
                            contentDescription = "Album Art",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize()
                        )
                    } else {
                        // Placeholder if Base64 decoding fails
                        Text("⚠️", fontSize = 80.sp, textAlign = TextAlign.Center, modifier = Modifier.align(Alignment.Center))
                    }
                } else {
                    // Handle URL or local file path using Coil
                    AsyncImage(
                        model = artworkData,
                        contentDescription = "Album Art",
                        contentScale = ContentScale.Crop,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = mediaInfo?.title ?: "No Track",
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
                textAlign = TextAlign.Center
            )

            if (mediaInfo?.artist.isNullOrBlank()) "—" else mediaInfo?.artist?.let {
                Text(
                    text = it,
                    style = MaterialTheme.typography.bodyMedium,
                    color = TextSecondary,
                    textAlign = TextAlign.Center
                )
            }

            Spacer(modifier = Modifier.height(16.dp))

            val progress = (mediaInfo?.position?.toFloat() ?: 0f) / (mediaInfo?.duration?.toFloat() ?: 1f)
            LinearProgressIndicator(
                progress = progress,
                modifier = Modifier.fillMaxWidth(),
                color = PrimaryAccent
            )
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(text = formatTime(mediaInfo?.position), color = TextSecondary)
                Text(text = formatTime(mediaInfo?.duration), color = TextSecondary)
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = { onCommand("player_prev") }) {
                    Text("⏮", fontSize = 28.sp, color = Color.White)
                }

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(
                    onClick = { onCommand("player_toggle") },
                    modifier = Modifier
                        .size(56.dp)
                        .background(PrimaryAccent, CircleShape)
                ) {
                    Text(
                        if (mediaInfo?.status == "Playing") "⏸" else "▶",
                        fontSize = 28.sp,
                        color = Color.White
                    )
                }

                Spacer(modifier = Modifier.width(16.dp))

                IconButton(onClick = { onCommand("player_next") }) {
                    Text("⏭", fontSize = 28.sp, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun BluetoothDevicesCard(devices: List<BluetoothDevice>) {
    if (devices.isNotEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = CardBackground)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Bluetooth Devices", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                devices.forEach { device ->
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(text = device.name ?: "Unnamed Device", fontWeight = FontWeight.Bold, color = Color.White)
                            Text(text = device.macAddress ?: "No Address", fontSize = 12.sp, color = TextSecondary)
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        device.battery?.let {
                            Text(text = "$it%", color = Success, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionsCard(onCommand: (String) -> Unit) {
    val actions = listOf(
        "player_toggle" to "Play/Pause",
        "player_next" to "Next",
        "player_prev" to "Previous",
        "system_update" to "Update",
        "list_home" to "List Home",
        "git_status" to "Git Status",
        "open_firefox" to "Firefox",
        "open_vscode" to "VSCode",
        "open_edge" to "Edge"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = CardBackground)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Quick Actions", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(200.dp) // Added fixed height
            ) {
                items(actions) { (command, label) ->
                    Button(onClick = { onCommand(command) }) {
                        Text(text = label)
                    }
                }
            }
        }
    }
}

@Composable
fun SystemOutputCard(output: String?) {
    if (!output.isNullOrEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = InputBackground)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "System Output", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = output,
                    color = Success,
                    fontFamily = FontFamily.Monospace,
                    modifier = Modifier
                        .background(Color.Black)
                        .padding(8.dp)
                        .fillMaxWidth()
                )
            }
        }
    }
}

fun formatTime(microseconds: Double?): String {
    if (microseconds == null || microseconds <= 0) return "0:00"
    val seconds = (microseconds / 1_000_000).toInt()
    val mins = seconds / 60
    val secs = seconds % 60
    return "$mins:${secs.toString().padStart(2, '0')}"
}

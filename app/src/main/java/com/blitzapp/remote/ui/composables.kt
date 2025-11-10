package com.blitzapp.remote.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.blur
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

// Dynamic color data class
data class DynamicColors(
    val primary: Color = PrimaryAccent,
    val secondary: Color = Color(0xFF1F1F1F),
    val background: Color = Color(0xFF121212),
    val surface: Color = Color(0xFF1E1E1E),
    val text: Color = Color.White
)

// Extract dominant color from artwork bitmap
fun extractColorsFromBitmap(bitmap: Bitmap?): DynamicColors {
    if (bitmap == null) return DynamicColors()

    return try {
        // Get dominant color from bitmap - using standard API
        val resized = Bitmap.createScaledBitmap(bitmap, 150, 150, true)

        var dominantColor = PrimaryAccent
        var r = 0L
        var g = 0L
        var b = 0L
        var pixelCount = 0

        for (i in 0 until resized.width) {
            for (j in 0 until resized.height) {
                val pixel = resized.getPixel(i, j)
                r += android.graphics.Color.red(pixel)
                g += android.graphics.Color.green(pixel)
                b += android.graphics.Color.blue(pixel)
                pixelCount++
            }
        }

        if (pixelCount > 0) {
            val avgR = (r / pixelCount).toInt()
            val avgG = (g / pixelCount).toInt()
            val avgB = (b / pixelCount).toInt()
            dominantColor = Color(red = avgR, green = avgG, blue = avgB)
        }

        DynamicColors(
            primary = dominantColor,
            secondary = dominantColor.copy(alpha = 0.6f),
            background = Color(0xFF0D0D0D),
            surface = dominantColor.copy(alpha = 0.1f),
            text = Color.White
        )
    } catch (_: Exception) {
        DynamicColors()
    }
}

// Calculate contrasting/opposite color for better visibility
fun getContrastingColor(color: Color): Color {
    // Calculate luminance
    val luminance = (0.299 * color.red + 0.587 * color.green + 0.114 * color.blue)

    // Return white for dark colors, black for light colors
    return if (luminance > 0.5f) Color.Black else Color.White
}

// Get vibrant opposite color for accents
fun getOppositeColor(color: Color): Color {
    return Color(
        red = 1f - color.red,
        green = 1f - color.green,
        blue = 1f - color.blue,
        alpha = 1f
    )
}

// Helper function to format doubles
private fun Double.format(decimals: Int) = "%.${decimals}f".format(this)

// Helper function to format network speed with auto-scaling
private fun formatSpeed(mbps: Double?): String {
    // Log.d("wifi speed" , mbps.toString())
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
fun Header(dynamicColors: DynamicColors = DynamicColors()) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(dynamicColors.background)
            .padding(16.dp)
    ) {
        Text(
            text = "⚡ BLITZ REMOTE",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = dynamicColors.primary,
        )
    }
}

@Composable
fun ConnectionCard(
    isConnected: Boolean,
    onConnect: (String, String, String) -> Unit,
    dynamicColors: DynamicColors = DynamicColors()
) {
    var ipAddress by remember { mutableStateOf("192.168.1.109") }
    var port by remember { mutableStateOf("8765") }
    var path by remember { mutableStateOf("/ws") }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = dynamicColors.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Connection", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = dynamicColors.primary)
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
                Button(
                    onClick = { onConnect(ipAddress, port, path) },
                    colors = ButtonDefaults.buttonColors(containerColor = dynamicColors.primary)
                ) {
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
    onColorsUpdate: (DynamicColors) -> Unit = {}
) {
    var dynamicColors by remember { mutableStateOf(DynamicColors()) }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        colors = CardDefaults.cardColors(containerColor = dynamicColors.background)
    ) {
        BoxWithConstraints(modifier = Modifier.fillMaxWidth()) {
            // Calculate dynamic image size based on screen width
            val screenWidth = maxWidth
            val imageSize = when {
                screenWidth < 360.dp -> screenWidth * 0.5f  // Small phones: 50% width
                screenWidth < 600.dp -> screenWidth * 0.45f // Medium phones: 45% width
                screenWidth < 840.dp -> 280.dp              // Tablets portrait: 280dp
                else -> 320.dp                               // Large screens/landscape: 320dp
            }

            // Blurred background artwork
            val artworkData = mediaInfo?.albumArt

            if (!artworkData.isNullOrBlank()) {
                if (artworkData.startsWith("data:")) {
                    // Base64 blurred background
                    val imageBitmap = remember(artworkData) {
                        try {
                            val pureBase64 = artworkData.substringAfter(',')
                            val decodedBytes = Base64.decode(pureBase64, Base64.DEFAULT)
                            BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)?.asImageBitmap()
                        } catch (_: Exception) {
                            null
                        }
                    }

                    imageBitmap?.let {
                        Image(
                            bitmap = it,
                            contentDescription = null,
                            contentScale = ContentScale.Crop,
                            modifier = Modifier
                                .matchParentSize()
                                .blur(50.dp)
                                .background(Color.Black.copy(alpha = 0.3f))
                        )
                    }
                } else {
                    // CDN URL blurred background
                    AsyncImage(
                        model = artworkData,
                        contentDescription = null,
                        contentScale = ContentScale.Crop,
                        modifier = Modifier
                            .matchParentSize()
                            .blur(50.dp)
                            .background(Color.Black.copy(alpha = 0.3f))
                    )
                }

                // Dark overlay for better text readability
                Box(
                    modifier = Modifier
                        .matchParentSize()
                        .background(Color.Black.copy(alpha = 0.5f))
                )
            }

            // Content on top of blurred background
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                // Calculate contrasting color for text
                val textColor = Color.White
                val accentColor = getOppositeColor(dynamicColors.primary)

                Text(
                    text = "🎵 NOW PLAYING",
                    style = MaterialTheme.typography.titleMedium,
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(12.dp))

                // Album artwork (sharp, not blurred) - Dynamic size
                val placeholderSize = (imageSize.value * 0.5f).sp
                Box(
                    modifier = Modifier
                        .size(imageSize)
                        .clip(RoundedCornerShape(16.dp))
                        .border(3.dp, accentColor, RoundedCornerShape(16.dp))
                        .background(Color.Black) // Fallback background
                ) {
                    if (artworkData.isNullOrBlank()) {
                        // Placeholder when no artwork is available
                        Text("🖼", fontSize = placeholderSize, textAlign = TextAlign.Center, modifier = Modifier.align(Alignment.Center))
                    } else if (artworkData.startsWith("data:")) {
                        // Handle Base64 encoded image
                        val imageBitmap = remember(artworkData) {
                            try {
                                val pureBase64 = artworkData.substringAfter(',')
                                val decodedBytes = Base64.decode(pureBase64, Base64.DEFAULT)
                                BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                            } catch (_: Exception) {
                                null
                            }
                        }

                        LaunchedEffect(imageBitmap) {
                            if (imageBitmap != null) {
                                val colors = extractColorsFromBitmap(imageBitmap)
                                dynamicColors = colors
                                onColorsUpdate(colors)
                            }
                        }

                        if (imageBitmap != null) {
                            Image(
                                bitmap = imageBitmap.asImageBitmap(),
                                contentDescription = "Album Art",
                                contentScale = ContentScale.Crop,
                                modifier = Modifier.fillMaxSize()
                            )
                        } else {
                            // Placeholder if Base64 decoding fails
                            Text("⚠️", fontSize = placeholderSize, textAlign = TextAlign.Center, modifier = Modifier.align(Alignment.Center))
                        }
                        }
                    } else {
                        // Handle URL (HTTPS CDN) or local file path using Coil
                        var imageLoaded by remember(artworkData) { mutableStateOf(false) }

                        AsyncImage(
                            model = artworkData,
                            contentDescription = "Album Art",
                            contentScale = ContentScale.Crop,
                            modifier = Modifier.fillMaxSize(),
                            onSuccess = { state ->
                                if (!imageLoaded) {
                                    imageLoaded = true
                                    val drawable = state.result.drawable

                                    // Try to get bitmap from drawable
                                    val bitmap = when (drawable) {
                                        is android.graphics.drawable.BitmapDrawable -> {
                                            drawable.bitmap
                                        }
                                        else -> {
                                            // Convert other drawable types to bitmap
                                            try {
                                                val width = drawable.intrinsicWidth.takeIf { it > 0 } ?: 100
                                                val height = drawable.intrinsicHeight.takeIf { it > 0 } ?: 100
                                                val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                                                val canvas = android.graphics.Canvas(bitmap)
                                                drawable.setBounds(0, 0, canvas.width, canvas.height)
                                                drawable.draw(canvas)
                                                bitmap
                                            } catch (e: Exception) {
                                                null
                                            }
                                        }
                                    }

                                    bitmap?.let {
                                        val colors = extractColorsFromBitmap(it)
                                        dynamicColors = colors
                                        onColorsUpdate(colors)
                                    }
                                }
                            },
                            onError = {
                                // Log error or show placeholder
                            }
                        )
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Text(
                    text = mediaInfo?.title ?: "No Track",
                    style = MaterialTheme.typography.titleLarge,
                    color = textColor,
                    fontWeight = FontWeight.Bold,
                    textAlign = TextAlign.Center
                )

                if (mediaInfo != null && !mediaInfo.artist.isNullOrBlank()) {
                    Text(
                        text = mediaInfo.artist,
                        style = MaterialTheme.typography.bodyMedium,
                        color = textColor.copy(alpha = 0.9f),
                        textAlign = TextAlign.Center
                    )
                }

                Spacer(modifier = Modifier.height(16.dp))

                val progress = (mediaInfo?.position?.toFloat() ?: 0f) / (mediaInfo?.duration?.toFloat() ?: 1f)
                LinearProgressIndicator(
                    { progress },
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(4.dp)),
                    color = accentColor,
                    trackColor = textColor.copy(alpha = 0.3f)
                )
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Text(text = formatTime(mediaInfo?.position), color = textColor.copy(alpha = 0.8f), fontSize = 12.sp)
                    Text(text = formatTime(mediaInfo?.duration), color = textColor.copy(alpha = 0.8f), fontSize = 12.sp)
                }

                Spacer(modifier = Modifier.height(16.dp))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.Center,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    IconButton(onClick = { onCommand("player_prev") }) {
                        Text("⏮", fontSize = 28.sp, color = textColor)
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    IconButton(
                        onClick = { onCommand("player_toggle") },
                        modifier = Modifier
                            .size(56.dp)
                            .background(accentColor, CircleShape)
                    ) {
                        Text(
                            if (mediaInfo?.status == "Playing") "⏸" else "▶",
                            fontSize = 28.sp,
                            color = getContrastingColor(accentColor)
                        )
                    }

                    Spacer(modifier = Modifier.width(16.dp))

                    IconButton(onClick = { onCommand("player_next") }) {
                        Text("⏭", fontSize = 28.sp, color = textColor)
                    }
                }
            }
        }
    }
}

@Composable
fun BluetoothDevicesCard(
    devices: List<BluetoothDevice>,
    dynamicColors: DynamicColors = DynamicColors()
) {
    if (devices.isNotEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = dynamicColors.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "Bluetooth Devices", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = dynamicColors.primary)
                Spacer(modifier = Modifier.height(16.dp))
                devices.forEach { device ->
                    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
                        Column {
                            Text(text = device.name ?: "Unnamed Device", fontWeight = FontWeight.Bold, color = dynamicColors.text)
                            Text(text = device.macAddress ?: "No Address", fontSize = 12.sp, color = dynamicColors.primary.copy(alpha = 0.6f))
                        }
                        Spacer(modifier = Modifier.weight(1f))
                        device.battery?.let {
                            Text(text = "$it%", color = dynamicColors.primary, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionsCard(
    onCommand: (String) -> Unit,
    dynamicColors: DynamicColors = DynamicColors()
) {
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
        colors = CardDefaults.cardColors(containerColor = dynamicColors.surface)
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            Text(text = "Quick Actions", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = dynamicColors.primary)
            Spacer(modifier = Modifier.height(16.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(2),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(200.dp)
            ) {
                items(actions) { (command, label) ->
                    Button(
                        onClick = { onCommand(command) },
                        colors = ButtonDefaults.buttonColors(containerColor = dynamicColors.primary)
                    ) {
                        Text(text = label, color = dynamicColors.background)
                    }
                }
            }
        }
    }
}

@Composable
fun SystemOutputCard(
    output: String?,
    dynamicColors: DynamicColors = DynamicColors()
) {
    if (!output.isNullOrEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            colors = CardDefaults.cardColors(containerColor = dynamicColors.surface)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Text(text = "System Output", fontSize = 20.sp, fontWeight = FontWeight.Bold, color = dynamicColors.primary)
                Spacer(modifier = Modifier.height(16.dp))
                Text(
                    text = output,
                    color = dynamicColors.primary,
                    fontFamily = FontFamily.Monospace,
                    fontSize = 12.sp,
                    modifier = Modifier
                        .background(Color.Black, RoundedCornerShape(8.dp))
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

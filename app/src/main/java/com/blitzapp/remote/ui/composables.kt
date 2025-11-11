package com.blitzapp.remote.ui

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.animation.*
import androidx.compose.animation.core.*
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.blur
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.DpSize
import androidx.compose.ui.unit.sp
import coil.compose.AsyncImage
import com.blitzapp.remote.BluetoothDevice
import com.blitzapp.remote.MediaInfo
import com.blitzapp.remote.MainViewModel
import com.blitzapp.remote.WiFiInfo
import com.blitzapp.remote.ui.theme.*
import androidx.compose.ui.draw.clip

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
                .padding(horizontal = 8.dp, vertical = 8.dp),
            colors = CardDefaults.cardColors(containerColor = Error),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Row(
                modifier = Modifier.padding(20.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "⚠️", fontSize = 28.sp)
                Spacer(modifier = Modifier.width(16.dp))
                Text(
                    text = error,
                    color = Color.White,
                    fontWeight = FontWeight.Bold,
                    fontSize = 16.sp
                )
            }
        }
    }
}

@Composable
fun ConnectingCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier.padding(20.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.Center
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(24.dp),
                color = PrimaryAccent,
                strokeWidth = 3.dp
            )
            Spacer(modifier = Modifier.width(16.dp))
            Text(
                text = "Connecting to server...",
                color = Color.White,
                fontWeight = FontWeight.Bold,
                fontSize = 16.sp
            )
        }
    }
}

@Composable
fun Header(
    dynamicColors: DynamicColors = DynamicColors(),
    onSettingsClick: (() -> Unit)? = null,
    isConnected: Boolean = false
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.background)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "⚡ BLITZ REMOTE",
            fontSize = 32.sp,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.weight(1f)
        )

        if (onSettingsClick != null) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Connection status dot
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .background(
                            color = if (isConnected) Success else Error,
                            shape = CircleShape
                        )
                )
                Spacer(modifier = Modifier.width(8.dp))
                IconButton(onClick = onSettingsClick) {
                    Text(
                        text = "⚙️",
                        fontSize = 32.sp,
                        color = MaterialTheme.colorScheme.primary
                    )
                }
            }
        }
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
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1E1E1E)),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(text = "🔌 Connection", fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.White)
            Spacer(modifier = Modifier.height(16.dp))

            OutlinedTextField(
                value = ipAddress,
                onValueChange = { ipAddress = it },
                label = { Text("IP Address") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = port,
                onValueChange = { port = it },
                label = { Text("Port") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(12.dp))

            OutlinedTextField(
                value = path,
                onValueChange = { path = it },
                label = { Text("Path") },
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(12.dp)
            )
            Spacer(modifier = Modifier.height(20.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Button(
                    onClick = { onConnect(ipAddress, port, path) },
                    colors = ButtonDefaults.buttonColors(containerColor = PrimaryAccent),
                    shape = RoundedCornerShape(12.dp),
                    modifier = Modifier.height(48.dp)
                ) {
                    Text(text = "CONNECT", fontWeight = FontWeight.Bold)
                }
                Spacer(modifier = Modifier.weight(1f))

                Surface(
                    color = if (isConnected) Success else Error,
                    shape = RoundedCornerShape(20.dp)
                ) {
                    Text(
                        text = if (isConnected) "● CONNECTED" else "○ DISCONNECTED",
                        color = Color.White,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp),
                        fontSize = 12.sp
                    )
                }
            }
        }
    }
}


@Composable
fun NowPlayingCard(
    mediaInfo: MediaInfo?,
    onCommand: (String) -> Unit,
    dynamicColors: DynamicColors,
    onColorsUpdate: (DynamicColors) -> Unit
) {
    val scale by animateFloatAsState(
        targetValue = if (mediaInfo != null) 1f else 0.95f,
        animationSpec = spring(
            dampingRatio = Spring.DampingRatioMediumBouncy,
            stiffness = Spring.StiffnessLow
        ),
        label = "scale"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp)
            .scale(scale),
        colors = CardDefaults.cardColors(containerColor = Color(0xFF1A1A1A)),
        shape = RoundedCornerShape(16.dp)
    ) {
        Box(modifier = Modifier.fillMaxWidth()) {
            // Blurred background artwork with crossfade
            val artworkData = mediaInfo?.albumArt
            val isPlaying = mediaInfo?.status == "Playing"

            // Animated blur value based on playing state
            val blurAmount by animateDpAsState(
                targetValue = if (isPlaying) 30.dp else 50.dp,
                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                label = "blur_animation"
            )

            // Animated overlay alpha based on playing state
            val overlayAlpha by animateFloatAsState(
                targetValue = if (isPlaying) 0.5f else 0.7f,
                animationSpec = tween(durationMillis = 800, easing = FastOutSlowInEasing),
                label = "overlay_animation"
            )

            // Background layer with blur
            Box(modifier = Modifier.matchParentSize()) {
                AnimatedContent(
                    targetState = artworkData,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(600)) togetherWith
                                fadeOut(animationSpec = tween(600))
                    },
                    label = "background_transition"
                ) { currentArtwork ->
                    Box(modifier = Modifier.fillMaxSize()) {
                        if (!currentArtwork.isNullOrBlank()) {
                            if (currentArtwork.startsWith("data:")) {
                                val imageBitmap = remember(currentArtwork) {
                                    try {
                                        val pureBase64 = currentArtwork.substringAfter(',')
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
                                            .fillMaxSize()
                                            .blur(blurAmount)
                                    )
                                }
                            } else {
                                AsyncImage(
                                    model = currentArtwork,
                                    contentDescription = null,
                                    contentScale = ContentScale.Crop,
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .blur(blurAmount)
                                )
                            }
                        }

                        // Dark overlay always on top of blur with animated alpha
                        Box(
                            modifier = Modifier
                                .fillMaxSize()
                                .background(Color.Black.copy(alpha = overlayAlpha))
                        )
                    }
                }
            }

            // Content on top
            Column(
                modifier = Modifier
                    .padding(20.dp)
                    .fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                val textColor = dynamicColors.text
                val accentColor = getOppositeColor(dynamicColors.primary)

                Text(
                    text = "🎵 NOW PLAYING",
                    style = MaterialTheme.typography.titleMedium,
                    color = accentColor,
                    fontWeight = FontWeight.Bold
                )

                Spacer(modifier = Modifier.height(16.dp))

                // Album artwork with animated crossfade and scale
                AnimatedContent(
                    targetState = artworkData,
                    transitionSpec = {
                        (fadeIn(animationSpec = tween(500)) +
                                scaleIn(
                                    initialScale = 0.85f,
                                    animationSpec = tween(500, easing = FastOutSlowInEasing)
                                )).togetherWith(
                            fadeOut(animationSpec = tween(300)) +
                                    scaleOut(
                                        targetScale = 0.95f,
                                        animationSpec = tween(300)
                                    )
                        )
                    },
                    label = "artwork_transition"
                ) { currentArtwork ->
                    var imageSize by remember(currentArtwork) { mutableStateOf(DpSize(200.dp, 200.dp)) }
                    Card(
                        modifier = Modifier
                            .size(imageSize.width + 8.dp, imageSize.height + 8.dp)
                            .padding(4.dp),
                        shape = RoundedCornerShape(16.dp),
                        elevation = CardDefaults.cardElevation(
                            defaultElevation = 12.dp,
                            pressedElevation = 16.dp
                        ),
                        colors = CardDefaults.cardColors(containerColor = Color.Black),
                        border = androidx.compose.foundation.BorderStroke(3.dp, accentColor)
                    ) {
                        Box(modifier = Modifier.fillMaxSize()) {
                        if (currentArtwork.isNullOrBlank()) {
                            Text("🖼", fontSize = 80.sp, textAlign = TextAlign.Center, modifier = Modifier.align(Alignment.Center))
                        } else if (currentArtwork.startsWith("data:")) {
                            val imageBitmap = remember(currentArtwork) {
                                try {
                                    val pureBase64 = currentArtwork.substringAfter(',')
                                    val decodedBytes = Base64.decode(pureBase64, Base64.DEFAULT)
                                    BitmapFactory.decodeByteArray(decodedBytes, 0, decodedBytes.size)
                                } catch (_: Exception) {
                                    null
                                }
                            }

                            LaunchedEffect(imageBitmap) {
                                if (imageBitmap != null) {
                                    val colors = extractColorsFromBitmap(imageBitmap)
                                    onColorsUpdate(colors)
                                    val ratio = imageBitmap.width.toFloat() / imageBitmap.height
                                    val maxSize = 200.dp
                                    imageSize = if (ratio > 1) {
                                        DpSize(maxSize, maxSize / ratio)
                                    } else {
                                        DpSize(maxSize * ratio, maxSize)
                                    }
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
                                Text("⚠️", fontSize = 80.sp, textAlign = TextAlign.Center, modifier = Modifier.align(Alignment.Center))
                            }
                        } else {
                            var painter by remember(currentArtwork) { mutableStateOf<Any?>(null) }

                            AsyncImage(
                                model = currentArtwork,
                                contentDescription = "Album Art",
                                contentScale = ContentScale.Fit,
                                modifier = Modifier.fillMaxSize(),
                                onSuccess = { state ->
                                    painter = state.result
                                    val drawable = state.result.drawable

                                    val width = drawable.intrinsicWidth
                                    val height = drawable.intrinsicHeight
                                    if (width > 0 && height > 0) {
                                        val ratio = width.toFloat() / height
                                        val maxSize = 200.dp
                                        imageSize = if (ratio > 1) {
                                            DpSize(maxSize, maxSize / ratio)
                                        } else {
                                            DpSize(maxSize * ratio, maxSize)
                                        }
                                    }
                                }
                            )

                            LaunchedEffect(painter) {
                                painter?.let { result ->
                                    val drawable = (result as? coil.compose.AsyncImagePainter.State.Success)?.result?.drawable
                                    drawable?.let { d ->
                                        val bitmap = when (d) {
                                            is android.graphics.drawable.BitmapDrawable -> d.bitmap
                                            else -> {
                                                try {
                                                    val width = d.intrinsicWidth.takeIf { it > 0 } ?: 100
                                                    val height = d.intrinsicHeight.takeIf { it > 0 } ?: 100
                                                    val bmp = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
                                                    val canvas = android.graphics.Canvas(bmp)
                                                    d.setBounds(0, 0, canvas.width, canvas.height)
                                                    d.draw(canvas)
                                                    bmp
                                                } catch (_: Exception) {
                                                    null
                                                }
                                            }
                                        }

                                        bitmap?.let {
                                            val colors = extractColorsFromBitmap(it)
                                            onColorsUpdate(colors)
                                        }
                                    }
                                }
                            }
                        }
                        }
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                // Animated text transitions
                AnimatedContent(
                    targetState = mediaInfo?.title,
                    transitionSpec = {
                        fadeIn(animationSpec = tween(400)) togetherWith
                                fadeOut(animationSpec = tween(200))
                    },
                    label = "title_transition"
                ) { title ->
                    Text(
                        text = title ?: "No Track",
                        style = MaterialTheme.typography.titleLarge,
                        color = textColor,
                        fontWeight = FontWeight.Bold,
                        textAlign = TextAlign.Center
                    )
                }

                if (mediaInfo != null && !mediaInfo.artist.isNullOrBlank()) {
                    AnimatedContent(
                        targetState = mediaInfo.artist,
                        transitionSpec = {
                            fadeIn(animationSpec = tween(400, delayMillis = 100)) togetherWith
                                    fadeOut(animationSpec = tween(200))
                        },
                        label = "artist_transition"
                    ) { artist ->
                        Text(
                            text = artist,
                            style = MaterialTheme.typography.bodyMedium,
                            color = textColor.copy(alpha = 0.9f),
                            textAlign = TextAlign.Center
                        )
                    }
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
                        AnimatedContent(
                            targetState = mediaInfo?.status == "Playing",
                            transitionSpec = {
                                fadeIn(tween(200)) + scaleIn(tween(200)) togetherWith
                                        fadeOut(tween(200)) + scaleOut(tween(200))
                            },
                            label = "play_pause_icon"
                        ) { isPlaying ->
                            Text(
                                if (isPlaying) "⏸" else "▶",
                                fontSize = 28.sp,
                                color = getContrastingColor(accentColor)
                            )
                        }
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
    dynamicColors: DynamicColors
) {
    if (devices.isNotEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = dynamicColors.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "📱 Bluetooth Devices",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = dynamicColors.text
                )
                Spacer(modifier = Modifier.height(16.dp))

                devices.forEachIndexed { index, device ->
                    Surface(
                        color = dynamicColors.surface.copy(alpha = 0.5f),
                        shape = RoundedCornerShape(12.dp)
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(16.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column(modifier = Modifier.weight(1f)) {
                                Text(
                                    text = device.name ?: "Unnamed Device",
                                    fontWeight = FontWeight.Bold,
                                    color = dynamicColors.text,
                                    fontSize = 16.sp
                                )
                                Spacer(modifier = Modifier.height(4.dp))
                                Text(
                                    text = device.macAddress ?: "No Address",
                                    fontSize = 12.sp,
                                    color = dynamicColors.text.copy(alpha = 0.6f)
                                )
                            }

                            device.battery?.let {
                                Surface(
                                    color = dynamicColors.primary,
                                    shape = RoundedCornerShape(8.dp)
                                ) {
                                    Text(
                                        text = "$it%",
                                        color = getContrastingColor(dynamicColors.primary),
                                        fontWeight = FontWeight.Bold,
                                        modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp)
                                    )
                                }
                            }
                        }
                    }

                    if (index < devices.size - 1) {
                        Spacer(modifier = Modifier.height(8.dp))
                    }
                }
            }
        }
    }
}

@Composable
fun QuickActionsCard(
    onCommand: (String) -> Unit,
    dynamicColors: DynamicColors
) {
    val actions = listOf(
        "player_toggle" to "▶️ Play/Pause",
        "player_next" to "⏭️ Next",
        "player_prev" to "⏮️ Previous",
        "system_update" to "🔄 Update",
        "list_home" to "📁 Home",
        "git_status" to "💻 Git",
        "open_firefox" to "🦊 Firefox",
        "open_vscode" to "📝 VSCode",
        "open_edge" to "🌐 Edge"
    )

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = dynamicColors.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Text(
                text = "⚡ Quick Actions",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = dynamicColors.text
            )
            Spacer(modifier = Modifier.height(16.dp))

            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.height(240.dp)
            ) {
                items(actions) { (command, label) ->
                    Button(
                        onClick = { onCommand(command) },
                        colors = ButtonDefaults.buttonColors(
                            containerColor = dynamicColors.surface.copy(alpha = 0.5f)
                        ),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(60.dp)
                    ) {
                        Text(
                            text = label,
                            color = dynamicColors.text,
                            fontSize = 12.sp,
                            textAlign = TextAlign.Center,
                            fontWeight = FontWeight.Medium
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun SystemOutputCard(
    output: String?,
    dynamicColors: DynamicColors
) {
    if (!output.isNullOrEmpty()) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(8.dp),
            colors = CardDefaults.cardColors(containerColor = dynamicColors.surface),
            shape = RoundedCornerShape(16.dp),
            elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
        ) {
            Column(modifier = Modifier.padding(20.dp)) {
                Text(
                    text = "💻 System Output",
                    fontSize = 22.sp,
                    fontWeight = FontWeight.Bold,
                    color = dynamicColors.text
                )
                Spacer(modifier = Modifier.height(16.dp))

                Surface(
                    color = Color.Black,
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        text = output,
                        color = Color(0xFF00FF00),
                        fontFamily = FontFamily.Monospace,
                        fontSize = 12.sp,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(16.dp)
                    )
                }
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

@Composable
fun SettingsButton(onClick: () -> Unit, dynamicColors: DynamicColors = DynamicColors()) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(8.dp),
        colors = CardDefaults.cardColors(containerColor = dynamicColors.surface),
        shape = RoundedCornerShape(16.dp),
        elevation = CardDefaults.cardElevation(defaultElevation = 4.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onClick() }
                .padding(20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "⚙️ Settings",
                fontSize = 22.sp,
                fontWeight = FontWeight.Bold,
                color = dynamicColors.text,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = "→",
                fontSize = 24.sp,
                color = dynamicColors.primary
            )
        }
    }
}

@Composable
fun SettingsScreen(
    viewModel: MainViewModel,
    onConnect: (String, String, String) -> Unit,
    onBackClick: () -> Unit
) {
    val connectionStatus by viewModel.connectionStatus
    val dynamicColors = remember { mutableStateOf(DynamicColors()) }

    Column(modifier = Modifier.fillMaxSize()) {
        // Header with back button
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .background(dynamicColors.value.background)
                .padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onBackClick) {
                Text(
                    text = "←",
                    fontSize = 32.sp,
                    color = dynamicColors.value.primary
                )
            }
            Text(
                text = "⚙️ Settings",
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                color = dynamicColors.value.primary,
                modifier = Modifier.padding(start = 8.dp)
            )
        }

        // Connection Card
        ConnectionCard(isConnected = connectionStatus, onConnect = onConnect, dynamicColors = dynamicColors.value)
    }
}

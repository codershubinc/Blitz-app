package com.blitzapp.remote

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.staggeredgrid.LazyVerticalStaggeredGrid
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridCells
import androidx.compose.foundation.lazy.staggeredgrid.StaggeredGridItemSpan
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.blitzapp.remote.ui.*
import com.blitzapp.remote.ui.theme.BlitzAppTheme

enum class Screen {
    MAIN, SETTINGS
}

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var webSocketManager: WebSocketManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        webSocketManager = WebSocketManager(viewModel)

        // Auto-connect on app load with default settings
        val defaultUrl = "ws://192.168.1.109:8765/ws"
        webSocketManager.connect(defaultUrl)

        setContent {
            BlitzAppTheme {
                val windowSizeClass = calculateWindowSizeClass(this)
                val currentScreen = remember { mutableStateOf(Screen.MAIN) }
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    when (currentScreen.value) {
                        Screen.MAIN -> MainScreen(
                            viewModel = viewModel,
                            widthSizeClass = windowSizeClass.widthSizeClass,
                            onConnect = { ip, port, path ->
                                val url = "ws://$ip:$port$path"
                                webSocketManager.connect(url)
                            },
                            onCommand = { command ->
                                webSocketManager.sendCommand(command)
                            },
                            onSettingsClick = { currentScreen.value = Screen.SETTINGS }
                        )
                        Screen.SETTINGS -> SettingsScreen(
                            viewModel = viewModel,
                            onConnect = { ip, port, path ->
                                val url = "ws://$ip:$port$path"
                                webSocketManager.connect(url)
                            },
                            onBackClick = { currentScreen.value = Screen.MAIN }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun MainScreen(
    viewModel: MainViewModel,
    widthSizeClass: WindowWidthSizeClass,
    onConnect: (String, String, String) -> Unit,
    onCommand: (String) -> Unit,
    onSettingsClick: () -> Unit
) {
    val connectionStatus by viewModel.connectionStatus
    val isConnecting by viewModel.isConnecting
    val mediaInfo by viewModel.mediaInfo
    val bluetoothDevices by viewModel.bluetoothDevices
    val wifiInfo by viewModel.wifiInfo
    val commandOutput by viewModel.commandOutput
    val error by viewModel.error
    val artWork by viewModel.artWork

    // State to hold dynamic colors
    val dynamicColors = remember { mutableStateOf(DynamicColors()) }

    Box(modifier = Modifier.fillMaxSize()) {
        when (widthSizeClass) {
            WindowWidthSizeClass.Compact -> {
                PortraitLayout(mediaInfo, bluetoothDevices, commandOutput, error, connectionStatus, isConnecting, onConnect, onCommand, artWork, dynamicColors.value, onSettingsClick) { newColors ->
                    dynamicColors.value = newColors
                }
            }
            else -> {
                LandscapeLayout(mediaInfo, bluetoothDevices, commandOutput, error, connectionStatus, isConnecting, onConnect, onCommand, artWork, dynamicColors.value, onSettingsClick) { newColors ->
                    dynamicColors.value = newColors
                }
            }
        }
        WifiSpeedIndicator(wifiInfo = wifiInfo)
    }
}

@Composable
fun PortraitLayout(
    mediaInfo: MediaInfo?,
    bluetoothDevices: List<BluetoothDevice>,
    commandOutput: String?,
    error: String?,
    connectionStatus: Boolean,
    isConnecting: Boolean,
    onConnect: (String, String, String) -> Unit,
    onCommand: (String) -> Unit,
    artWork: ArtWork?,
    dynamicColors: DynamicColors,
    onSettingsClick: () -> Unit,
    onColorsUpdate: (DynamicColors) -> Unit
) {
    LazyVerticalStaggeredGrid(
        columns = StaggeredGridCells.Fixed(1),
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(8.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp),
        verticalItemSpacing = 8.dp
    ) {
        // Header - Full width
        item(span = StaggeredGridItemSpan.FullLine) {
            Header(dynamicColors = dynamicColors, onSettingsClick = onSettingsClick, isConnected = connectionStatus)
        }

        // Connecting Card - Full width (shown when connecting, hidden when connected)
        if (isConnecting && !connectionStatus) {
            item(span = StaggeredGridItemSpan.FullLine) {
                ConnectingCard()
            }
        }

        // Error Card - Full width
        if (!error.isNullOrEmpty()) {
            item(span = StaggeredGridItemSpan.FullLine) {
                ErrorCard(error = error)
            }
        }

        // Now Playing - Full width (main tile)
        item(span = StaggeredGridItemSpan.FullLine) {
            NowPlayingCard(mediaInfo = mediaInfo, onCommand = onCommand, onColorsUpdate = onColorsUpdate)
        }

        // Bluetooth Devices - Full width
        if (bluetoothDevices.isNotEmpty()) {
            item(span = StaggeredGridItemSpan.FullLine) {
                BluetoothDevicesCard(devices = bluetoothDevices, dynamicColors = dynamicColors)
            }
        }

        // Quick Actions - Full width
        item(span = StaggeredGridItemSpan.FullLine) {
            QuickActionsCard(onCommand = onCommand, dynamicColors = dynamicColors)
        }

        // System Output - Full width
        if (!commandOutput.isNullOrEmpty()) {
            item(span = StaggeredGridItemSpan.FullLine) {
                SystemOutputCard(output = commandOutput, dynamicColors = dynamicColors)
            }
        }
    }
}

@Composable
fun LandscapeLayout(
    mediaInfo: MediaInfo?,
    bluetoothDevices: List<BluetoothDevice>,
    commandOutput: String?,
    error: String?,
    connectionStatus: Boolean,
    isConnecting: Boolean,
    onConnect: (String, String, String) -> Unit,
    onCommand: (String) -> Unit,
    artWork: ArtWork?,
    dynamicColors: DynamicColors,
    onSettingsClick: () -> Unit,
    onColorsUpdate: (DynamicColors) -> Unit
) {
    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Header(dynamicColors = dynamicColors, onSettingsClick = onSettingsClick, isConnected = connectionStatus)

            // Connecting Card (shown when connecting, hidden when connected)
            if (isConnecting && !connectionStatus) {
                ConnectingCard()
            }

            NowPlayingCard(mediaInfo = mediaInfo, onCommand = onCommand, onColorsUpdate = onColorsUpdate)
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(top = 80.dp)
        ) {
            if (!error.isNullOrEmpty()) {
                ErrorCard(error = error)
            }
            if (bluetoothDevices.isNotEmpty()) {
                BluetoothDevicesCard(devices = bluetoothDevices, dynamicColors = dynamicColors)
            }
            QuickActionsCard(onCommand = onCommand, dynamicColors = dynamicColors)
            if (!commandOutput.isNullOrEmpty()) {
                SystemOutputCard(output = commandOutput, dynamicColors = dynamicColors)
            }
        }
    }
}
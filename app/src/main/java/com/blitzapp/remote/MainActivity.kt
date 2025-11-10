package com.blitzapp.remote

import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.windowsizeclass.ExperimentalMaterial3WindowSizeClassApi
import androidx.compose.material3.windowsizeclass.WindowWidthSizeClass
import androidx.compose.material3.windowsizeclass.calculateWindowSizeClass
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.blitzapp.remote.ui.*
import com.blitzapp.remote.ui.theme.BlitzAppTheme

@OptIn(ExperimentalMaterial3WindowSizeClassApi::class)
class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModels()
    private lateinit var webSocketManager: WebSocketManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON)

        webSocketManager = WebSocketManager(viewModel)

        setContent {
            BlitzAppTheme {
                val windowSizeClass = calculateWindowSizeClass(this)
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    MainScreen(
                        viewModel = viewModel,
                        widthSizeClass = windowSizeClass.widthSizeClass,
                        onConnect = { ip, port, path ->
                            val url = "ws://$ip:$port$path"
                            webSocketManager.connect(url)
                        },
                        onCommand = { command ->
                            webSocketManager.sendCommand(command)
                        }
                    )
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
    onCommand: (String) -> Unit
) {
    val connectionStatus by viewModel.connectionStatus
    val mediaInfo by viewModel.mediaInfo
    val bluetoothDevices by viewModel.bluetoothDevices
    val wifiInfo by viewModel.wifiInfo
    val commandOutput by viewModel.commandOutput
    val error by viewModel.error
    val artWork by viewModel.artWork

    Box(modifier = Modifier.fillMaxSize()) {
        when (widthSizeClass) {
            WindowWidthSizeClass.Compact -> {
                PortraitLayout(mediaInfo, bluetoothDevices, commandOutput, error, connectionStatus, onConnect, onCommand, artWork)
            }
            else -> {
                LandscapeLayout(mediaInfo, bluetoothDevices, commandOutput, error, connectionStatus, onConnect, onCommand, artWork)
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
    onConnect: (String, String, String) -> Unit,
    onCommand: (String) -> Unit,
    artWork: ArtWork?
) {
    Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
        Header()
        ErrorCard(error = error)
        ConnectionCard(isConnected = connectionStatus, onConnect = onConnect)
        NowPlayingCard(mediaInfo = mediaInfo, onCommand = onCommand, artWork = artWork?.url)
        BluetoothDevicesCard(devices = bluetoothDevices)
        QuickActionsCard(onCommand = onCommand)
        SystemOutputCard(output = commandOutput)
    }
}

@Composable
fun LandscapeLayout(
    mediaInfo: MediaInfo?,
    bluetoothDevices: List<BluetoothDevice>,
    commandOutput: String?,
    error: String?,
    connectionStatus: Boolean,
    onConnect: (String, String, String) -> Unit,
    onCommand: (String) -> Unit,
    artWork: ArtWork?
) {
    Row(modifier = Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
        ) {
            Header()
            ConnectionCard(isConnected = connectionStatus, onConnect = onConnect)
            NowPlayingCard(mediaInfo = mediaInfo, onCommand = onCommand, artWork = artWork?.url)
        }
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(top = 80.dp)
        ) {
            ErrorCard(error = error)
            BluetoothDevicesCard(devices = bluetoothDevices)
            QuickActionsCard(onCommand = onCommand)
            SystemOutputCard(output = commandOutput)
        }
    }
}
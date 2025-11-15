package com.quazaar.remote

import androidx.compose.runtime.mutableStateOf
import androidx.lifecycle.ViewModel

class MainViewModel : ViewModel() {
    val connectionStatus = mutableStateOf(false)
    val isConnecting = mutableStateOf(false)
    val mediaInfo = mutableStateOf<MediaInfo?>(null)
    val artWork = mutableStateOf<ArtWork?>(null) // Added for separate artwork
    val bluetoothDevices = mutableStateOf<List<BluetoothDevice>>(emptyList())
    val wifiInfo = mutableStateOf<WiFiInfo?>(null)
    val commandOutput = mutableStateOf<String?>(null)
    val error = mutableStateOf<String?>(null)

    fun updateConnectionStatus(isConnected: Boolean) {
        connectionStatus.value = isConnected
    }

    fun updateConnectingStatus(connecting: Boolean) {
        isConnecting.value = connecting
    }

    fun updateMediaInfo(info: MediaInfo?) {
        mediaInfo.value = info
    }

    fun updateArtWork(art: ArtWork?) {
        artWork.value = art
    }

    fun updateBluetoothDevices(devices: List<BluetoothDevice>) {
        bluetoothDevices.value = devices
    }

    fun updateWifiInfo(info: WiFiInfo?) {
        wifiInfo.value = info
    }

    fun updateCommandOutput(output: String?) {
        commandOutput.value = output
    }

    fun updateError(errorMessage: String?) {
        error.value = errorMessage
    }
}
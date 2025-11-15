package com.quazaar.remote

import android.os.Handler
import android.os.Looper
import android.util.Log
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import okhttp3.WebSocket
import okhttp3.WebSocketListener
import java.util.concurrent.TimeUnit

class WebSocketManager(private val viewModel: MainViewModel) {
    private var webSocket: WebSocket? = null
    private val client = OkHttpClient.Builder()
        .connectTimeout(5, TimeUnit.SECONDS)
        .build()
    private val gson = Gson()
    private val handler = Handler(Looper.getMainLooper())

    private var lastUrl: String? = null
    private var retryCount = 0
    private val maxRetries = 5
    private val retryDelayMs = 5000L // 5 seconds

    private val reconnectRunnable = object : Runnable {
        override fun run() {
            if (retryCount < maxRetries && lastUrl != null) {
                retryCount++
                Log.d("WebSocketManager", "Retrying connection attempt $retryCount/$maxRetries")
                connect(lastUrl!!)
            } else {
                Log.d("WebSocketManager", "Max retries reached or no URL set")
            }
        }
    }

    fun connect(url: String) {
        // Cancel any pending reconnect
        handler.removeCallbacks(reconnectRunnable)

        webSocket?.close(1000, "Starting new connection")

        lastUrl = url
        // Don't reset retry count if this is a retry attempt
        if (retryCount == 0) {
            // First attempt from user action
        }

        // Set connecting state
        viewModel.updateConnectingStatus(true)

        val request = Request.Builder().url(url).build()
        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                viewModel.updateConnectionStatus(true)
                viewModel.updateConnectingStatus(false)
                viewModel.updateError(null)
                retryCount = 0 // Reset on successful connection
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                parseMessage(text)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                viewModel.updateConnectionStatus(false)
                viewModel.updateConnectingStatus(false)
                // Start reconnect timer
                if (lastUrl != null && retryCount < maxRetries) {
                    handler.postDelayed(reconnectRunnable, retryDelayMs)
                }
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                viewModel.updateConnectionStatus(false)
                viewModel.updateConnectingStatus(false)
                if (t is java.net.SocketTimeoutException) {
                    viewModel.updateError("Connection timed out. Retry $retryCount/$maxRetries")
                } else {
                    viewModel.updateError("Connection failed: ${t.message}. Retry $retryCount/$maxRetries")
                }
                // Start reconnect timer
                if (lastUrl != null && retryCount < maxRetries) {
                    handler.postDelayed(reconnectRunnable, retryDelayMs)
                }
            }
        }

        webSocket = client.newWebSocket(request, listener)
    }

    fun sendCommand(command: String) {
        val json = """{"command": "$command"}"""
        webSocket?.send(json)
    }

    private fun parseMessage(json: String) {
        try {
            // Parse as RawServerResponse wrapper
            val serverResponse = gson.fromJson(json, RawServerResponse::class.java)

            when (serverResponse.message) {
                "media_info" -> {
                    // Parse the data field as MediaInfo
                    if (serverResponse.data != null) {
                        val mediaInfo = gson.fromJson(serverResponse.data, MediaInfo::class.java)
                        viewModel.updateMediaInfo(mediaInfo)
                        mediaInfo.albumArt?.let { artwork ->
                            viewModel.updateArtWork(ArtWork(artwork))
                        }
                    }
                }
                "bluetooth" -> {
                    if (serverResponse.data != null) {
                        val deviceListType = object : TypeToken<List<BluetoothDevice>>() {}.type
                        val devices = gson.fromJson<List<BluetoothDevice>>(serverResponse.data, deviceListType)
                        viewModel.updateBluetoothDevices(devices.filter { it.connected })
                    }
                }
                "wifi" -> {
                    if (serverResponse.data != null) {
                        val wifiInfo = gson.fromJson(serverResponse.data, WiFiInfo::class.java)
                        viewModel.updateWifiInfo(wifiInfo)
                    }
                }
                "command_output" -> {
                    serverResponse.data?.let { dataElement ->
                        val output = dataElement.asString
                        viewModel.updateCommandOutput(output)
                    }
                }
            }
        } catch (e: Exception) {
            Log.e("WebSocketManager", "Error parsing message: $json", e)
            viewModel.updateError("Parse error: ${e.message}")
        }
    }

    fun close() {
        handler.removeCallbacks(reconnectRunnable)
        webSocket?.close(1000, "Client closing connection")
        lastUrl = null
        retryCount = 0
    }
}
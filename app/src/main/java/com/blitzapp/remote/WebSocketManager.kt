package com.blitzapp.remote

import android.util.Log
import com.google.gson.Gson
import com.google.gson.JsonObject
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

    fun connect(url: String) {
        webSocket?.close(1000, "Starting new connection")

        val request = Request.Builder().url(url).build()
        val listener = object : WebSocketListener() {
            override fun onOpen(webSocket: WebSocket, response: Response) {
                viewModel.updateConnectionStatus(true)
                viewModel.updateError(null)
            }

            override fun onMessage(webSocket: WebSocket, text: String) {
                parseMessage(text)
            }

            override fun onClosed(webSocket: WebSocket, code: Int, reason: String) {
                viewModel.updateConnectionStatus(false)
            }

            override fun onFailure(webSocket: WebSocket, t: Throwable, response: Response?) {
                viewModel.updateConnectionStatus(false)
                if (t is java.net.SocketTimeoutException) {
                    viewModel.updateError("Connection timed out. Check server IP.")
                } else {
                    viewModel.updateError("Connection failed: ${t.message}")
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
            val data = gson.fromJson(json, JsonObject::class.java)
            when (data.get("status")?.asString) {
                "player" -> {
                    val playerOutput = data.getAsJsonObject("output")
                    val artwork = data.get("artwork")?.asString
                    val mediaInfo = gson.fromJson(playerOutput, MediaInfo::class.java)
                    viewModel.updateMediaInfo(mediaInfo)
                    viewModel.updateArtWork(ArtWork(artwork))
                }
                "bluetooth" -> {
                    val deviceListType = object : TypeToken<List<BluetoothDevice>>() {}.type
                    val devices = gson.fromJson<List<BluetoothDevice>>(data.get("bluetooth"), deviceListType)
                    viewModel.updateBluetoothDevices(devices.filter { it.connected })
                }
                "wifi" -> {
                    val wifiInfo = gson.fromJson(data.getAsJsonObject("wifi"), WiFiInfo::class.java)
                    viewModel.updateWifiInfo(wifiInfo)
                }
                "command_output" -> {
                    val output = data.get("output").asString
                    viewModel.updateCommandOutput(output)
                }
            }
        } catch (e: Exception) {
            Log.e("WebSocketManager", "Error parsing message: $json", e)
            viewModel.updateError("Parse error: ${e.message}")
        }
    }

    fun close() {
        webSocket?.close(1000, "Client closing connection")
    }
}
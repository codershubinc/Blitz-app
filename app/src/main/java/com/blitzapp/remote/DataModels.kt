package com.blitzapp.remote

import com.google.gson.annotations.SerializedName

data class ArtWork(
    val url: String?
)

data class MediaInfo(
    @SerializedName("Title") val title: String?,
    @SerializedName("Artist") val artist: String?,
    @SerializedName("Album") val album: String?,
    @SerializedName("Artwork") val albumArt: String?,
    @SerializedName("Length") val duration: Double?,
    @SerializedName("Position") val position: Double?,
    @SerializedName("Status") val status: String?
)

data class BluetoothDevice(
    @SerializedName("name") val name: String?,
    @SerializedName("macAddress") val macAddress: String?,
    @SerializedName("connected") val connected: Boolean,
    @SerializedName("battery") val battery: Int?,
    @SerializedName("icon") val icon: String?
)

data class WiFiInfo(
    @SerializedName("ssid") val ssid: String?,
    @SerializedName("signalStrength") val signalStrength: Int?,
    @SerializedName("linkSpeed") val linkSpeed: Int?,
    @SerializedName("frequency") val frequency: String?,
    @SerializedName("security") val security: String?,
    @SerializedName("ipAddress") val ipAddress: String?,
    @SerializedName("connected") val connected: Boolean?,
    @SerializedName("downloadSpeed") val downloadSpeed: Double?,
    @SerializedName("uploadSpeed") val uploadSpeed: Double?,
    @SerializedName("interface") val interfaceName: String?,
    @SerializedName("unitOfSpeed") val unitOfSpeed: String?
)
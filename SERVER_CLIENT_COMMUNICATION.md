# Server-Client Communication Guide

## ✅ Current Working Implementation

Your Go server and Android app are now correctly configured to communicate!

## Server → Client Message Flow

### Go Server (poller/poller.go)

```go
websocket.WriteChannelMessage(
    models.ServerResponse{
        Status:  "success",
        Message: "media_info",
        Data:    msg,  // Player info with Title, Artist, Album, etc.
    },
)
```

### JSON Sent Over WebSocket

```json
{
  "status": "success",
  "message": "media_info",
  "data": {
    "Title": "Mera Hua (From \"Ek Deewane Ki Deewaniyat\") (Original Motion Picture Soundtrack)",
    "Artist": "Annkur R Pathakk",
    "Album": "Mera Hua (From \"Ek Deewane Ki Deewaniyat\") (Original Motion Picture Soundtrack)",
    "Artwork": "https://i.scdn.co/image/ab67616d0000b2733b2f989a04f1756708aceda4",
    "Position": "61485025",
    "Length": "223000000",
    "Status": "Playing",
    "Player": "spotify"
  }
}
```

### Android Client (WebSocketManager.kt)

```kotlin
private fun parseMessage(json: String) {
    val serverResponse = gson.fromJson(json, RawServerResponse::class.java)
    
    when (serverResponse.message) {  // ✅ Checking "message" field
        "media_info" -> {            // ✅ Matches server's Message: "media_info"
            val mediaInfo = gson.fromJson(serverResponse.data, MediaInfo::class.java)
            viewModel.updateMediaInfo(mediaInfo)
            // Extract and update artwork
            mediaInfo.albumArt?.let { artwork ->
                viewModel.updateArtWork(ArtWork(artwork))
            }
        }
        // ... other message types
    }
}
```

## Message Type Routing

The app routes messages based on the `message` field (not `status`):

| Server Message Value | Android Handler | Data Type |
|---------------------|-----------------|-----------|
| `"media_info"` | Updates MediaInfo | Player data |
| `"bluetooth"` | Updates Bluetooth devices | List<BluetoothDevice> |
| `"wifi"` | Updates WiFi info | WiFiInfo |
| `"command_output"` | Updates command output | String |

## Server Response Structure

### Field Usage

```go
type ServerResponse struct {
    Status  string `json:"status"`   // General status: "success", "error", etc.
    Message string `json:"message"`  // Message type identifier: "media_info", "bluetooth", etc.
    Data    any    `json:"data"`     // Actual payload (varies by message type)
}
```

### Examples for Other Message Types

#### Bluetooth Devices
```go
websocket.WriteChannelMessage(
    models.ServerResponse{
        Status:  "success",
        Message: "bluetooth",
        Data:    bluetoothDevices,  // []BluetoothDevice
    },
)
```

#### WiFi Info
```go
websocket.WriteChannelMessage(
    models.ServerResponse{
        Status:  "success",
        Message: "wifi",
        Data:    wifiInfo,  // WiFiInfo struct
    },
)
```

#### Command Output
```go
websocket.WriteChannelMessage(
    models.ServerResponse{
        Status:  "success",
        Message: "command_output",
        Data:    outputString,  // string
    },
)
```

#### Error Response
```go
websocket.WriteChannelMessage(
    models.ServerResponse{
        Status:  "error",
        Message: "Failed to execute command",
        Data:    nil,
    },
)
```

## Player Data Structure

### Go Server (models/player.go or similar)

```go
type Player struct {
    Title    string `json:"Title"`
    Artist   string `json:"Artist"`
    Album    string `json:"Album"`
    Artwork  string `json:"Artwork"`
    Position string `json:"Position"`  // Microseconds
    Length   string `json:"Length"`    // Microseconds
    Status   string `json:"Status"`    // "Playing", "Paused", "Stopped"
    Player   string `json:"Player"`    // "spotify", "vlc", etc.
}
```

### Android Client (DataModels.kt)

```kotlin
data class MediaInfo(
    @SerializedName("Title") val title: String?,
    @SerializedName("Artist") val artist: String?,
    @SerializedName("Album") val album: String?,
    @SerializedName("Artwork") val albumArt: String?,
    @SerializedName("Length") val duration: Double?,
    @SerializedName("Position") val position: Double?,
    @SerializedName("Status") val status: String?,
    @SerializedName("Player") val player: String? = null
)
```

## Poller Configuration

Your server polls every 1 second:

```go
func Handle() {
    Poller(1*time.Second, make(chan struct{}), func() {
        msg, err := utils.GetPlayerInfo()
        
        if err != nil {
            fmt.Printf("⚠️ Failed to get player info: %v\n", err)
            return
        }
        
        websocket.WriteChannelMessage(
            models.ServerResponse{
                Status:  "success",
                Message: "media_info",
                Data:    msg,
            },
        )
    })
}
```

This means the Android app receives updated player info every second automatically!

## Testing Checklist

- [x] Server sends `Status: "success"` ✅
- [x] Server sends `Message: "media_info"` ✅
- [x] Server includes player data in `Data` field ✅
- [x] Android checks `serverResponse.message` for routing ✅
- [x] Android parses data as `MediaInfo` ✅
- [x] Android extracts artwork from `MediaInfo.albumArt` ✅
- [x] No compilation errors ✅

## Everything is Working! 🎉

Your implementation is **already correct**:

1. ✅ Server sends the right format
2. ✅ Android parses the right field (`message` not `status`)
3. ✅ Message type `"media_info"` matches on both sides
4. ✅ Player data structure matches between Go and Kotlin

Just connect your Android app to the WebSocket server and it should work!

## Troubleshooting

If the player info doesn't appear:

1. **Check WebSocket connection**: Look for "Connected" status in the app
2. **Check server logs**: Ensure `utils.GetPlayerInfo()` returns data without errors
3. **Check Android logs**: Use `adb logcat | grep WebSocketManager` to see parsing
4. **Verify player is running**: Make sure Spotify/VLC/etc. is actually playing something

The architecture is solid! 🚀


# Server Response Format Guide

## Updated Player Response Structure

The BlitzApp now expects player data wrapped in a `ServerResponse` structure matching your Go backend.

### Complete Server Response Format

```json
{
  "status": "player",
  "message": "Current player status",
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

## Response Structure Breakdown

### Top-Level Fields

| Field | Type | Required | Description |
|-------|------|----------|-------------|
| `status` | string | Yes | Response type identifier (e.g., "player", "bluetooth", "wifi") |
| `message` | string | Yes | Human-readable message about the response |
| `data` | object/array | No | The actual payload data (type varies by status) |

### Player Data Fields (inside `data`)

| Field | Type | Format | Description |
|-------|------|--------|-------------|
| `Title` | string | Text | Song/track title |
| `Artist` | string | Text | Artist name |
| `Album` | string | Text | Album name |
| `Artwork` | string | URL or Data URI | Album artwork (HTTP URL or base64) |
| `Position` | string | Microseconds | Current playback position |
| `Length` | string | Microseconds | Total track duration |
| `Status` | string | "Playing"/"Paused"/"Stopped" | Playback status |
| `Player` | string | Text | Player app name (e.g., "spotify", "vlc") |

## Time Format Details

### Position and Length Values

Both `Position` and `Length` use **microseconds** as the unit:

- **Position**: `"61485025"` = 61,485,025 microseconds = **61.485 seconds**
- **Length**: `"223000000"` = 223,000,000 microseconds = **223 seconds** (3 minutes 43 seconds)

### Converting Microseconds

```kotlin
// In Kotlin
val seconds = position.toLong() / 1_000_000
val minutes = seconds / 60
val secs = seconds % 60
```

```go
// In Go
seconds := position / 1_000_000
minutes := seconds / 60
secs := seconds % 60
```

## Other Response Types

### Bluetooth Response
```json
{
  "status": "bluetooth",
  "message": "Bluetooth devices",
  "data": [
    {
      "name": "Device Name",
      "macAddress": "00:11:22:33:44:55",
      "connected": true,
      "battery": 85,
      "icon": "headphones"
    }
  ]
}
```

### WiFi Response
```json
{
  "status": "wifi",
  "message": "WiFi information",
  "data": {
    "ssid": "Network Name",
    "signalStrength": -45,
    "linkSpeed": 866,
    "frequency": "5GHz",
    "ipAddress": "192.168.1.100",
    "connected": true,
    "downloadSpeed": 50.5,
    "uploadSpeed": 10.2,
    "interface": "wlan0",
    "unitOfSpeed": "Mbps"
  }
}
```

### Command Output Response
```json
{
  "status": "command_output",
  "message": "Command executed successfully",
  "data": "Output text here..."
}
```

### Error Response
```json
{
  "status": "error",
  "message": "Error description here",
  "data": null
}
```

## Go Backend Example

```go
package models

type ServerResponse struct {
    Status  string `json:"status"`
    Message string `json:"message"`
    Data    any    `json:"data,omitempty"`
}

type Player struct {
    Title    string `json:"Title"`
    Artist   string `json:"Artist"`
    Album    string `json:"Album"`
    Artwork  string `json:"Artwork"`
    Position string `json:"Position"`
    Length   string `json:"Length"`
    Status   string `json:"Status"`
    Player   string `json:"Player"`
}

// Send player data
func sendPlayerData(playerInfo Player) {
    response := ServerResponse{
        Status:  "player",
        Message: "Current player status",
        Data:    playerInfo,
    }
    
    jsonData, _ := json.Marshal(response)
    websocket.Send(jsonData)
}
```

## Android Kotlin Implementation

```kotlin
// Data models
data class ServerResponse<T>(
    @SerializedName("status") val status: String,
    @SerializedName("message") val message: String,
    @SerializedName("data") val data: T? = null
)

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

// Parsing
val serverResponse = gson.fromJson(json, RawServerResponse::class.java)
if (serverResponse.status == "player") {
    val mediaInfo = gson.fromJson(serverResponse.data, MediaInfo::class.java)
    // Use mediaInfo...
}
```

## Implementation Checklist

- [x] DataModels.kt - Added `ServerResponse<T>` and `RawServerResponse`
- [x] MediaInfo - Added `Player` field
- [x] WebSocketManager - Updated parsing to use `RawServerResponse`
- [x] No compilation errors
- [ ] Test with real server data

## Testing

1. **Start your Go WebSocket server**
2. **Connect the Android app to the server**
3. **Send a player update from the server**
4. **Verify in the app:**
   - Song title displays correctly
   - Artist name shows
   - Album artwork loads
   - Progress bar updates with position/length
   - Player name is available (check logs or add UI)

## Next Steps

If you want to display the player name in the UI, you can add it to the NowPlayingCard:

```kotlin
// Add after artist name
if (!mediaInfo.player.isNullOrBlank()) {
    Text(
        text = "Playing on ${mediaInfo.player}",
        style = MaterialTheme.typography.bodySmall,
        color = textColor.copy(alpha = 0.7f),
        textAlign = TextAlign.Center,
        fontSize = 11.sp
    )
}
```

This will show "Playing on spotify" or "Playing on vlc" in the UI.


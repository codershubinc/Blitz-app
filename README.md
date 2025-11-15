# Quazaar Remote

Quazaar is an Android application that acts as a remote control for your computer. It connects to a WebSocket server, allowing you to control media playback, view system information, and execute quick actions from your Android device.

## ✨ Features

*   **Media Control:** Play, pause, and skip tracks with ease.
*   **Dynamic Theming:** The UI color scheme dynamically adapts based on the album art of the currently playing media.
*   **System Information:** View details about connected Bluetooth devices and real-time WiFi speed.
*   **Quick Actions:** Execute predefined commands on your computer with a single tap.
*   **Responsive UI:** The layout adapts to both portrait and landscape orientations.

## 🚀 Tech Stack

*   **UI:** 100% [Jetpack Compose](https://developer.android.com/jetpack/compose) for a modern, declarative UI.
*   **Architecture:** Follows the MVVM (Model-View-ViewModel) pattern.
*   **State Management:** Uses `mutableStateOf` to manage UI state.
*   **Networking:** [OkHttp](https://square.github.io/okhttp/) for WebSocket communication.
*   **Asynchronous Operations:** Coroutines for managing background threads.
*   **Image Loading:** [Coil](https://coil-kt.github.io/coil/) for loading and displaying images.

## 🛠️ Setup

To build and run this project, you will need:

1.  Android Studio Iguana | 2023.2.1 or later.
2.  A connected Android device or emulator.
3.  A running instance of the corresponding Quazaar server on your computer.

**Steps:**

1.  Clone the repository.
2.  Open the project in Android Studio.
3.  In `app/src/main/java/com/quazaar/remote/MainActivity.kt`, update the `defaultUrl` with the IP address and port of your server.
    ```kotlin
    val defaultUrl = "ws://YOUR_COMPUTER_IP:8765/ws"
    ```
4.  You can also change the connection details at runtime from the settings screen in the app.
5.  Run the app on your device or emulator.

## 📁 Project Structure

The project is organized into the following key files:

*   `app/src/main/java/com/quazaar/remote/MainActivity.kt`: The main entry point of the app.
*   `app/src/main/java/com/quazaar/remote/MainViewModel.kt`: Manages the application's state.
*   `app/src/main/java/com/quazaar/remote/WebSocketManager.kt`: Handles WebSocket communication.
*   `app/src/main/java/com/quazaar/remote/DataModels.kt`: Defines the data structures for the app.
*   `app/src/main/java/com/quazaar/remote/ui/composables.kt`: Contains all the Jetpack Compose UI components.

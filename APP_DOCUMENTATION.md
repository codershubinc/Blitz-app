# BlitzApp Documentation

Welcome to the Quazaar documentation! This document provides a comprehensive overview of the app's structure, components, and functionality. It's designed to help you understand how the app is built and how to extend it.

## Project Overview

Quazaar is a remote control application for your computer. It connects to a WebSocket server, allowing you to control media playback, view system information, and execute quick actions from your Android device.

## Project Structure

The project is organized into the following key files:

- **`MainActivity.kt`**: The entry point of the app. It sets up the UI and initializes the `WebSocketManager`.
- **`MainViewModel.kt`**: The central state holder for the application. It manages the app's data and exposes it to the UI.
- **`DataModels.kt`**: Defines the data structures for `ArtWork`, `MediaInfo`, `BluetoothDevice`, and `WiFiInfo`.
- **`composables.kt`**: Contains all the Jetpack Compose UI components for the app.
- **`WebSocketManager.kt`**: Manages the WebSocket connection to the server.

## Core Components

### MainActivity.kt

The `MainActivity` is the app's entry point. It sets up the UI using Jetpack Compose and initializes a `WebSocketManager` to connect to a server. The UI is split into `MainScreen` and `SettingsScreen`, and it adapts to different screen sizes.

Key concepts:
- Uses `ComponentActivity` from AndroidX.
- Sets up Compose with `setContent`.
- Uses `viewModels()` to get the ViewModel instance.
- Handles screen navigation with a mutable state for `Screen` enum.
- Adapts UI based on window size class for portrait/landscape.

### MainViewModel.kt

The `MainViewModel` acts as the central state holder for the application. It uses `mutableStateOf` to manage various pieces of data, such as connection status, media information, and more. This data is then observed by the UI to automatically update when the state changes.

Key concepts:
- Extends `ViewModel` from AndroidX.
- Uses `MutableState` for reactive UI updates.
- Receives updates from `WebSocketManager` and exposes them to the UI.

### DataModels.kt

The `DataModels.kt` file defines the data structures that are used throughout the app. These models represent the data received from the WebSocket server and are used to populate the UI.

Key models:
- `MediaInfo`: Contains track title, artist, album art, position, duration, status.
- `BluetoothDevice`: Name, MAC address, battery level.
- `WiFiInfo`: Download/upload speeds, connected status.
- `ArtWork`: Currently not used, but intended for artwork data.

## UI Components

The UI is built entirely with Jetpack Compose. The `composables.kt` file contains a variety of reusable components, including:

- **`ErrorCard`**: Displays an error message to the user.
- **`ConnectingCard`**: Shows a loading indicator while the app is connecting to the server.
- **`Header`**: The main header of the app, which displays the app's title and connection status.
- **`ConnectionCard`**: Allows the user to configure the WebSocket connection settings.
- **`NowPlayingCard`**: Displays the currently playing media, including album art, track information, and playback controls.
- **`BluetoothDevicesCard`**: Shows a list of connected Bluetooth devices.
- **`QuickActionsCard`**: Provides a grid of buttons for executing common commands.
- **`SystemOutputCard`**: Displays output from system commands.

### Dynamic Colors Feature

One of the standout features of Quazaar is its dynamic color theming based on album artwork. When media is playing, the app extracts the dominant color from the album art and uses it to theme the UI elements.

How it works:
- For base64 encoded images (data: URLs), the bitmap is decoded directly and colors are extracted in a `LaunchedEffect`.
- For URL images, the `AsyncImage` component loads the image, and in the `onSuccess` callback, the bitmap is extracted from the drawable and colors are calculated.
- The `extractColorsFromBitmap` function resizes the image, samples pixels to find the average color, and creates a `DynamicColors` object.
- This color is then used throughout the UI for accents, surfaces, etc.

## App Functionality

When the app starts, it automatically attempts to connect to a WebSocket server at a default URL. The user can change the connection settings from the `SettingsScreen`. Once connected, the app can:

- **Control Media**: The `NowPlayingCard` allows the user to play, pause, and skip tracks.
- **View System Info**: The app displays information about connected Bluetooth devices and Wi-Fi speed.
- **Execute Quick Actions**: The `QuickActionsCard` provides buttons for running predefined commands on the server.

## Key Android/Kotlin Concepts Used

- **Jetpack Compose**: Declarative UI framework for building native Android interfaces.
- **ViewModel**: Manages UI-related data in a lifecycle-conscious way.
- **LiveData/State**: Reactive data holders that notify the UI of changes.
- **WebSocket**: Real-time communication protocol for bidirectional data exchange.
- **Coil**: Image loading library for Android, used for loading album art.
- **Material 3**: Modern design system for consistent UI components.
- **Window Size Classes**: Adapts UI layout based on screen size (compact/expanded).
- **Animations**: Uses Compose animations for smooth transitions and effects.

## Extending the App

To add new features:
- Add new data models in `DataModels.kt`.
- Update `MainViewModel.kt` to handle new data.
- Create new composables in `composables.kt`.
- Modify `WebSocketManager.kt` to send/receive new messages.

This documentation should give you a solid starting point for understanding and modifying the Quazaar. If you have any more questions, feel free to ask!

# Silver Umbrella

![Kotlin](https://img.shields.io/badge/kotlin-100%25-blueviolet)
![Android](https://img.shields.io/badge/Platform-Android-green)
![Compose](https://img.shields.io/badge/Jetpack-Compose-4285F4)

A simple, modern Weather App for Android built with Jetpack Compose.

https://github.com/user-attachments/assets/44b3a30e-caaf-4d20-a6ce-146eb44c0bf9

## Features

- **City Search**: Search for cities with auto-debounce (500ms) and minimum character limit.
- **Real-time Weather**: Displays current temperature and weather conditions.
- **Error Handling**: User-friendly error messages for network or API issues.
- **Clean Architecture**: Separation of concerns using MVVM.

## Tech Stack

- **Language**: Kotlin
- **UI Framework**: Jetpack Compose
- **Architecture**: MVVM (Model-View-ViewModel)
- **Async**: Coroutines & Flow
- **Networking**: Retrofit & OkHttp
- **Image Loading**: Coil

## Architecture

The project follows a clean separation of concerns:

- `data`: Repository implementations, API services and data mappers.
- `domain`: Domain models and repository interfaces.
- `ui`: Jetpack Compose screens and ViewModels.

### Key Decisions

- **API Key Injection**: Handled via an OkHttp Interceptor.
- **State Management**: ViewModels use sealed interfaces for state (`Idle`, `Loading`, `Success`, `Error`).
- **UX**: Search is debounced to reduce API calls. Temperatures are rounded for better readability.
- **City Repository**: Separated from weather logic to allow potential reuse of the geocoding logic.

## Setup

### Prerequisites

- **Minimum SDK**: 29 (Android 10)
- **Target SDK**: 36

### Configuration

To run the app, you need an API Key.

1.  Create a `local.properties` file in the root directory if it doesn't exist.
2.  Add your API key:
    ```properties
    API_KEY="YOUR_API_KEY_HERE"
    ```
    > **Note**: The app will intentionally crash in Debug builds if the API Key is missing or blank. This "functionality" can be removed in `OpenWeatherMapClient`

## Building and Running

### Android Studio

1.  Gradle Sync.
2.  Select the `app` configuration.
3.  Select a connected device or emulator.
4.  Click the green **Run** button.

### Command Line

Make sure you have a device or emulator connected to ADB.

```bash
./gradlew installDebug && adb shell am start -n "digital.greenman.silverumbrella/digital.greenman.silverumbrella.MainActivity"
```

## Testing

### Unit Tests

Run unit tests using Gradle:

```bash
./gradlew test
```

The report can be found at `app/build/reports/tests/testDebugUnitTest/index.html`.

![Unit Test Report](Screen%20Shot%202025-12-30%20at%2019.58.20.png)

### UI Tests

Run instrumented tests on a connected device/emulator:

```bash
./gradlew connectedDebugAndroidTest
```

The report can be found at `app/build/reports/androidTests/connected/index.html`.

![UI Test Report](Screen%20Shot%202025-12-30%20at%2020.05.34.png)

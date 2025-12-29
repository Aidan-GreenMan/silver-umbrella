# silver-umbrella
A Simple Weather App

The `temperature` Double is rounded to Integer to display in Celsius

## Run:

- Add `API_KEY="…"` to your `local.properties` file. Please note Debug app will crash if API Key is
 null or blank
- Min Android 10

## Requirements:

Kotlin & JetPack Compose

MVVM

Kotlin Coroutines & Flow

Retrofit and Okhttp

## TODO:

- [x] Weather API
- [x] Get Weather Data from API - https://openweathermap.org/current
- [x] Domain with Repository
- [x] Loading state
- [x] Display Weather Data - Name, Temperature (Celsius), Condition and brief Description
- [x] Get City name from user
- [x] Icons
- [x] Error handling
  - [x] 401
  - [x] 404
  - [x] offline
  - [x] empty results
- [x] App Icon & Theming
- [ ] Unit Tests
- [ ] UI Tests
- [ ] Video
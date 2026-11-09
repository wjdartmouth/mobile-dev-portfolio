# Android Portfolio Apps

Five standalone Kotlin + Jetpack Compose apps mirroring the iOS set. Each folder is an independent Android Studio project (own Gradle wrapper).

| App | Package | Highlights |
|-----|---------|------------|
| **FocusTimer** | `com.jianwang.portfolio.focustimer` | Pomodoro, Room history, notifications, Glance widget |
| **SpotJournal** | `com.jianwang.portfolio.spotjournal` | Camera/picker, location, Room, OSM Leaflet map (no Maps key) |
| **PulseWeather** | `com.jianwang.portfolio.pulseweather` | Open-Meteo, offline cache, °C/°F, hourly chart |
| **LedgerLite** | `com.jianwang.portfolio.ledgerlite` | Room expenses, filters, donut chart, CSV share |
| **SyncNotes** | `com.jianwang.portfolio.syncnotes` | Room always; optional Firestore sync (off by default) |

## Requirements
- JDK 17+
- Android SDK 34 / build-tools 34
- Android Studio Hedgehog+ recommended

## Build one app
```bash
cd Android/FocusTimer   # or SpotJournal / PulseWeather / LedgerLite / SyncNotes
./gradlew :app:assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

Do **not** commit `local.properties`, `google-services.json`, or APKs.

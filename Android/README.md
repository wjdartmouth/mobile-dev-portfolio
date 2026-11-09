# Android Portfolio Apps

Five standalone **Kotlin + Jetpack Compose** apps mirroring the iOS set in [wjdartmouth/mobile-dev-portfolio](https://github.com/wjdartmouth/mobile-dev-portfolio). Each folder is an independent Android Studio / Gradle project (own wrapper).

Part of a mid-level Japan hiring portfolio — readiness target **March 2027**. See the root [README.md](../README.md) and [ROADMAP.md](../ROADMAP.md).

| App | Package | Highlights | Open |
|-----|---------|------------|------|
| **Focus Timer** | `com.jianwang.portfolio.focustimer` | Pomodoro, Room history, notifications, **Glance** widget | [`FocusTimer/`](FocusTimer/) |
| **Spot Journal** | `com.jianwang.portfolio.spotjournal` | Camera/picker, location, Room, **OSM/Leaflet** map (no Maps API key) | [`SpotJournal/`](SpotJournal/) |
| **Pulse Weather** | `com.jianwang.portfolio.pulseweather` | Open-Meteo, offline cache, °C/°F, hourly chart | [`PulseWeather/`](PulseWeather/) |
| **Ledger Lite** | `com.jianwang.portfolio.ledgerlite` | Room expenses, filters, donut chart, CSV share | [`LedgerLite/`](LedgerLite/) |
| **Sync Notes** | `com.jianwang.portfolio.syncnotes` | Room always; **optional Firestore** sync (off by default) | [`SyncNotes/`](SyncNotes/) |

## Tech matrix (Android)

| Capability | Focus Timer | Spot Journal | Pulse Weather | Ledger Lite | Sync Notes |
|------------|:-----------:|:------------:|:-------------:|:-----------:|:----------:|
| Jetpack Compose + Material 3 | ✅ | ✅ | ✅ | ✅ | ✅ |
| MVVM (`ViewModel`) | ✅ | ✅ | ✅ | ✅ | ✅ |
| Room | ✅ | ✅ | | ✅ | ✅ |
| DataStore / settings | ✅ | | | | |
| Networking (Open-Meteo) | | | ✅ | | |
| Offline cache | | | ✅ | | |
| Location + map | | ✅ | | | |
| Camera / photo picker | | ✅ | | | |
| Glance widget | ✅ | | | | |
| Notifications | ✅ | | | | |
| Charts | ✅ | | ✅ | ✅ | |
| CSV share | | | | ✅ | |
| Optional Firestore | | | | | ✅ |

Planned hardening (WorkManager, UI tests, CI matrix, JA strings): [ROADMAP.md](../ROADMAP.md).

## Requirements

- JDK **17+**
- Android SDK **34** / build-tools 34
- Android Studio **Hedgehog** or newer recommended
- minSdk **26** (Android 8.0)

## Build one app

```bash
cd Android/FocusTimer   # or SpotJournal / PulseWeather / LedgerLite / SyncNotes
./gradlew :app:assembleDebug
```

APK output: `app/build/outputs/apk/debug/app-debug.apk`

Or: Android Studio → **File → Open** → select the app directory → Run.

### Sync Notes + Firebase (optional)

1. Create a Firebase project and enable Firestore.
2. Download `google-services.json` into `SyncNotes/app/`.
3. Follow the enable steps in [`SyncNotes/README.md`](SyncNotes/README.md).
4. **Never commit** `google-services.json` or API keys.

## Hygiene

Do **not** commit:

- `local.properties`
- `google-services.json`
- Built APKs / `.cxx` / IDE user files

## iOS counterparts

SwiftUI twins live under [`../Apps/`](../Apps/). Same product intent, platform-idiomatic stacks.

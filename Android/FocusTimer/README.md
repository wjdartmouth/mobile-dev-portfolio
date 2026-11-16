# Focus Timer (Android)

Pomodoro timer with session history charts, notifications, DataStore settings, Room persistence, and a Glance home-screen widget.

## Features
- Focus / short break / long break phases with configurable durations
- Room-backed session history + simple daily bar chart
- Notification + haptic when a session ends
- Glance app widget showing phase and remaining time
- Material 3, dark-theme friendly, contentDescriptions on key controls

## Open
Android Studio → **Open** → `Android/FocusTimer`

```bash
cd Android/FocusTimer
./gradlew :app:assembleDebug :app:testDebugUnitTest
```

JVM tests cover `FocusCyclePolicy` and `TimerMath` (cadence, zero minutes, `65` → `1:05`, `3660` → `1h 01m`). The unused WorkManager dependency is unchanged.

Requires JDK 17 and Android SDK 34.

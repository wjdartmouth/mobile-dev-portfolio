# Running tests

Domain policies are pure (no SwiftData, Room, or UI). View models and repositories call them. Coverage today is Focus Timer, Pulse Weather, Ledger Lite, and Sync Notes. Spot Journal has no unit-test sources. Gradle marks its test task `NO-SOURCE`, and the build still succeeds, so CI can assemble that app too.

## Android (required CI path)

From the app directory, JDK 17:

```bash
cd Android/FocusTimer && ./gradlew :app:assembleDebug :app:testDebugUnitTest
cd Android/PulseWeather && ./gradlew :app:assembleDebug :app:testDebugUnitTest
cd Android/LedgerLite && ./gradlew :app:assembleDebug :app:testDebugUnitTest
cd Android/SyncNotes && ./gradlew :app:assembleDebug :app:testDebugUnitTest
cd Android/SpotJournal && ./gradlew :app:assembleDebug :app:testDebugUnitTest
```

Reports land in `app/build/reports/tests/testDebugUnitTest/`. GitHub Actions (`.github/workflows/android.yml`) runs the same two tasks for every app on Ubuntu with Temurin 17. SDK setup uses `android-actions/setup-android@v4` and installs `platform-tools`, `platforms;android-34`, and `build-tools;34.0.0`. It does not request the removed `tools` package.

What the tests lock in:

| App | Cases |
|-----|--------|
| Focus Timer | Long-break cadence, zero completed count, break → focus, `0` / `65` / `3660` duration formatting, minute → seconds |
| Pulse Weather | Open-Meteo forecast + geocoding fixtures, missing `results` / `precipitation_probability` / current weather, codes 0 Clear, 61 Rain, 95 Thunderstorm, unknown → Unknown |
| Ledger Lite | Blank search, category + case-insensitive search, totals, category buckets sorted by total, CSV comma handling |
| Sync Notes | Higher revision wins, equal revision uses `updatedAt`, equal timestamp keeps local, missing local loses, deleted remote wins only when revision or time wins |

Android CSV still replaces commas with spaces (the exporter that shipped). iOS CSV quotes commas, quotes, and newlines. Tests follow each platform’s exporter.

## iOS (Mac only)

No `.xcodeproj` is committed. Generate, then test. These are logic tests: `CODE_SIGNING_ALLOWED=NO`, no host app, and Focus Timer does not need the widget extension running.

```bash
brew install xcodegen
cd Apps/FocusTimer && xcodegen generate
xcodebuild test -scheme FocusTimer -destination 'platform=iOS Simulator,name=iPhone 16' CODE_SIGNING_ALLOWED=NO
```

Repeat for `PulseWeather`, `LedgerLite`, and `SyncNotes`. If that simulator name is missing, pick one:

```bash
xcrun simctl list devices available
xcodebuild test -scheme PulseWeather -destination 'platform=iOS Simulator,id=<UDID>' CODE_SIGNING_ALLOWED=NO
```

iOS CI is not on GitHub yet. A macOS workflow was skipped so the default branch is not red while destination names and signing are still fragile. Revisit it once a simulator selector is proven on `macos-latest`.

Spot Journal has no XCTest target. Gradle reports `:app:testDebugUnitTest` as `NO-SOURCE` there; `assembleDebug` still runs and the build succeeds.

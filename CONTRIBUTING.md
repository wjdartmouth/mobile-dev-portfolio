# How I work in this repo

This is a monorepo of five product ideas, each built twice: SwiftUI under `Apps/` and Jetpack Compose under `Android/`. Weekly sprints prefer depth — tests, CI, accessibility, offline behavior — over a new demo app.

## Layout

| Path | What it is |
|------|------------|
| `Apps/<App>/` | iOS sources plus an XcodeGen `project.yml`. No `.xcodeproj` is checked in. |
| `Android/<App>/` | Standalone Gradle project (`./gradlew` lives in that folder). |
| `docs/` | Architecture, testing commands, screenshot folders. |
| `.github/workflows/android.yml` | Android `assembleDebug` + unit tests for all five apps. |

Package names stay `com.jianwang.portfolio.*`.

## iOS

Generate a project, then open it in Xcode 15+ and set your signing team:

```bash
brew install xcodegen
cd Apps/FocusTimer   # or SpotJournal / PulseWeather / LedgerLite / SyncNotes
xcodegen generate
open *.xcodeproj
```

Domain tests for Focus Timer, Pulse Weather, Ledger Lite, and Sync Notes are logic-test bundles. They do not host the app, and Focus Timer’s widget extension does not need to run. From the generated project:

```bash
xcodebuild test \
  -scheme FocusTimer \
  -destination 'platform=iOS Simulator,name=iPhone 16' \
  CODE_SIGNING_ALLOWED=NO
```

There is no macOS GitHub Actions workflow yet. Run these on a Mac. See [docs/testing.md](docs/testing.md).

## Android

JDK 17 and Android SDK 34:

```bash
cd Android/FocusTimer   # or SpotJournal / PulseWeather / LedgerLite / SyncNotes
./gradlew :app:assembleDebug :app:testDebugUnitTest
```

Spot Journal has no domain unit tests yet. `testDebugUnitTest` still succeeds there (zero tests) so the CI matrix can build every app.

## Do not commit

- `local.properties`
- `google-services.json` or any other API keys
- keystores (`.jks`, `.keystore`)
- APKs, AABs, and `build/` outputs
- `.idea/` and generated `.xcodeproj` bundles

Firebase for Sync Notes stays off unless you are enabling it locally with your own project.

## Weekly cadence

One meaningful change per week through March 2027. The rotation is in [ROADMAP.md](ROADMAP.md): quality, product polish, sophistication, then the narrative that matches what actually shipped. Do not add a sixth app just to have a commit.

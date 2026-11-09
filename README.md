# iOS Development Portfolio

A curated set of **five fully functional SwiftUI apps** that demonstrate the core pillars of modern iOS development. Built for interview conversations: each app is shippable demo quality, opens cleanly in Xcode, and maps to a clear technical narrative.

**Author:** Jian Wang  
**Platform:** iOS 17+ · Swift 5.9+ · SwiftUI  
**Repo:** [wjdartmouth/ios-dev-portfolio](https://github.com/wjdartmouth/ios-dev-portfolio)

---

## Apps at a glance

| App | One-liner | Open in Xcode |
|-----|-----------|---------------|
| **Focus Timer** | Pomodoro timer with history charts, notifications, and WidgetKit | `Apps/FocusTimer` |
| **Spot Journal** | Photo + note + map-pin journal with location | `Apps/SpotJournal` |
| **Pulse Weather** | Current + forecast weather via Open-Meteo (no API key) | `Apps/PulseWeather` |
| **Ledger Lite** | Expense tracker with SwiftData, charts, and CSV export | `Apps/LedgerLite` |
| **Sync Notes** | Notes with CloudKit-ready sync + local fallback | `Apps/SyncNotes` |

---

## Tech matrix

| Capability | Focus Timer | Spot Journal | Pulse Weather | Ledger Lite | Sync Notes |
|------------|:-----------:|:------------:|:-------------:|:-----------:|:----------:|
| SwiftUI + MVVM | ✅ | ✅ | ✅ | ✅ | ✅ |
| SwiftData / persistence | ✅ | ✅ SwiftData | ✅ cache | ✅ SwiftData | ✅ + CloudKit |
| async/await networking | | | ✅ Open-Meteo | | |
| MapKit + CoreLocation | | ✅ | ✅ | | |
| Camera / PhotosPicker | | ✅ | | | |
| WidgetKit (Home/Lock) | ✅ | | | | |
| ActivityKit Live Activity | ✅ | | | | |
| Local notifications | ✅ | | | | |
| Charts (Swift Charts) | ✅ | | ✅ | ✅ | |
| CSV / file export | | | | ✅ | |
| Accessibility | ✅ | ✅ | ✅ | ✅ | ✅ |
| CloudKit sync layer | | | | | ✅ |
| Dark Mode | ✅ | ✅ | ✅ | ✅ | ✅ |

---

## How to open a project

### Option A — XcodeGen (recommended)

Each app ships a `project.yml`. On a Mac with [XcodeGen](https://github.com/yonaskolb/XcodeGen) installed:

```bash
brew install xcodegen
cd Apps/FocusTimer   # or SpotJournal / PulseWeather / LedgerLite / SyncNotes
xcodegen generate
open *.xcodeproj
```

### Option B — Create an Xcode project manually

1. Open Xcode → **File → New → Project → App** (SwiftUI, Swift, iOS 17).
2. Replace the generated `ContentView` / `*App.swift` with the sources under `Sources/`.
3. Add any `Widgets/` target as a Widget Extension if present.
4. Copy `Info.plist` privacy keys and entitlements from the app folder.
5. Add `Assets.xcassets`.

Deployment target for every app: **iOS 17.0**.

---

## Per-app deep dives

Each app folder has its own `README.md` covering features, architecture, run steps, and known limitations (signing, CloudKit, widgets).

```
Apps/
  FocusTimer/     # timer · charts · notifications · widgets · Live Activity
  SpotJournal/    # camera · map · SwiftData · location
  PulseWeather/   # Open-Meteo · offline cache · search
  LedgerLite/     # SwiftData · charts · CSV · a11y
  SyncNotes/      # CloudKit-ready · local fallback · conflict notes
docs/
  architecture.md
```

---

## Design principles

- **MVVM** with observable view models (`@Observable` / `ObservableObject` as appropriate).
- Real persistence and real networking — no stub “TODO” screens.
- Explicit loading, empty, and error states.
- Privacy usage strings in every `Info.plist` that needs them.
- No API secrets. Weather uses [Open-Meteo](https://open-meteo.com/) (free, no key).

---

## Limitations (honest)

| Topic | Note |
|-------|------|
| Widgets / Live Activities | Require Xcode 15+ on macOS; build the Widget extension target alongside the app. |
| CloudKit (Sync Notes) | Needs an Apple Developer team + iCloud capability. Local fallback works without it. |
| Signing | Set your Team ID in Xcode Signing & Capabilities before running on device. |
| Xcode project files | Generated via XcodeGen (`project.yml`). Run `xcodegen generate` on Mac. |
| Screenshots | Placeholder sections in each README — drop PNGs into `docs/screenshots/` when ready. |

---

## License

MIT — see [LICENSE](LICENSE). Use freely in portfolios and interviews.

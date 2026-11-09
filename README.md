# Mobile Development Portfolio

**Author:** Jian Wang  
**Goal:** Mid-level mobile developer roles in Japan (applications from ~May 2027)  
**Readiness target:** Interview-ready by **March 2027**  
**Repo:** [wjdartmouth/mobile-dev-portfolio](https://github.com/wjdartmouth/mobile-dev-portfolio)  
**License:** MIT

Five production-shaped apps, each implemented on **iOS (SwiftUI)** and **Android (Jetpack Compose)**. This is not a collection of weekend toys — it is a deliberate portfolio for hiring conversations: real persistence, real networking, explicit error/empty/loading states, widgets where they earn their place, and architecture you can defend in an interview.

---

## 採用担当向けサマリー（日本語）

| 項目 | 内容 |
|------|------|
| **目的** | 日本のミドルレベル向けモバイルエンジニア職（目安: 2027年5月頃から応募） |
| **準備完了目標** | **2027年3月**までに面接で語れる完成度 |
| **構成** | 同一コンセプトのアプリを **iOS 5本 + Android 5本**（計10プロジェクト） |
| **技術** | iOS: Swift / SwiftUI / SwiftData / WidgetKit など · Android: Kotlin / Compose / Room / Glance など |
| **方針** | デモ止まりではなく、永続化・同期・オフライン・アクセシビリティ・テスト／CI まで段階的に厚くする |
| **ロードマップ** | 詳細は [ROADMAP.md](ROADMAP.md)（2026年11月 → 2027年3月） |

コードをそのままクローンして Xcode / Android Studio で動かせます。CloudKit・Firebase・実機署名など環境依存の部分は各 README に正直に書いてあります。

---

## Who this is for / career goal

| | |
|--|--|
| **Audience** | Recruiters and hiring engineers evaluating mid-level mobile candidates in Japan |
| **Career target** | Mid-level iOS / Android / cross-platform mobile roles (May 2027 applications) |
| **Portfolio deadline** | Feature freeze + screenshots + interview notes by **March 2027** |
| **Languages** | Product UIs trending bilingual (EN + JA strings); docs English-primary with JP hiring summary |

This repo exists so a hiring manager can open it, run an app in under 15 minutes, and have a concrete technical conversation — not a slide deck of screenshots alone.

---

## Portfolio philosophy

1. **Depth over quantity** — Five paired apps beat fifteen half-finished demos. Each app owns a clear pillar (widgets, maps/media, networking, structured data, sync).
2. **Production patterns** — MVVM, protocol-backed services, offline-first where it matters, loading / empty / error UI, privacy strings, no hardcoded API secrets.
3. **Parity with intent** — iOS and Android share product intent; implementations use platform-idiomatic APIs (SwiftData vs Room, WidgetKit vs Glance, CloudKit vs optional Firestore).
4. **Honest limitations** — Signing, CloudKit containers, Firebase `google-services.json`, and missing app icons are documented instead of papered over.
5. **Continuous sophistication** — From Nov 2026 → Mar 2027 the weekly cadence hardens tests, CI, a11y, localization, and one additional sophisticated app (see [ROADMAP.md](ROADMAP.md)).

---

## Apps at a glance

| App | Product pitch | iOS (SwiftUI) | Android (Compose) |
|-----|---------------|---------------|-------------------|
| **Focus Timer** | Pomodoro timer with history charts, notifications, home/lock widgets | [`Apps/FocusTimer`](Apps/FocusTimer) | [`Android/FocusTimer`](Android/FocusTimer) |
| **Spot Journal** | Photo + short note + map pin journal | [`Apps/SpotJournal`](Apps/SpotJournal) | [`Android/SpotJournal`](Android/SpotJournal) |
| **Pulse Weather** | Current + forecast weather (Open-Meteo, no API key) | [`Apps/PulseWeather`](Apps/PulseWeather) | [`Android/PulseWeather`](Android/PulseWeather) |
| **Ledger Lite** | Personal expense tracker with filters, charts, CSV export | [`Apps/LedgerLite`](Apps/LedgerLite) | [`Android/LedgerLite`](Android/LedgerLite) |
| **Sync Notes** | Notes with cloud sync path + local-first fallback | [`Apps/SyncNotes`](Apps/SyncNotes) | [`Android/SyncNotes`](Android/SyncNotes) |

Per-app feature READMEs live next to the source (both platforms).

---

## Full tech matrix

### iOS (Swift 5.9+ · SwiftUI · iOS 17+)

| Capability | Focus Timer | Spot Journal | Pulse Weather | Ledger Lite | Sync Notes |
|------------|:-----------:|:------------:|:-------------:|:-----------:|:----------:|
| SwiftUI + MVVM | ✅ | ✅ | ✅ | ✅ | ✅ |
| SwiftData / local persistence | ✅ | ✅ | cache files | ✅ | ✅ |
| async/await networking | | | ✅ Open-Meteo | | |
| MapKit + CoreLocation | | ✅ | ✅ geocode | | |
| Camera / PhotosPicker | | ✅ | | | |
| WidgetKit (Home / Lock) | ✅ | | | | |
| ActivityKit Live Activity | ✅ | | | | |
| Local notifications | ✅ | | | | |
| Swift Charts | ✅ | | ✅ | ✅ | |
| CSV / share sheet export | | | | ✅ | |
| CloudKit sync layer | | | | | ✅ |
| Accessibility + Dark Mode | ✅ | ✅ | ✅ | ✅ | ✅ |

### Android (Kotlin · Jetpack Compose · minSdk 26 · targetSdk 34)

| Capability | Focus Timer | Spot Journal | Pulse Weather | Ledger Lite | Sync Notes |
|------------|:-----------:|:------------:|:-------------:|:-----------:|:----------:|
| Compose + MVVM (ViewModel) | ✅ | ✅ | ✅ | ✅ | ✅ |
| Room + DataStore | ✅ | ✅ | cache | ✅ | ✅ |
| OkHttp / kotlinx serialization networking | | | ✅ Open-Meteo | | |
| Location + OSM / Leaflet map (no Maps API key) | | ✅ | | | |
| Camera / photo picker | | ✅ | | | |
| Glance app widget | ✅ | | | | |
| Notifications | ✅ | | | | |
| Charts (Compose canvas / custom) | ✅ | | ✅ | ✅ | |
| CSV share via Intent | | | | ✅ | |
| Optional Firestore sync | | | | | ✅ (off by default) |
| Material 3 + contentDescriptions | ✅ | ✅ | ✅ | ✅ | ✅ |

Cross-cutting details: [docs/architecture.md](docs/architecture.md).

---

## Architecture notes

Shared shape on both platforms:

```
App entry → ViewModels → Services (protocols) → Persistence / Network
                ↓
              Views (SwiftUI / Compose)
```

| Concern | iOS approach | Android approach |
|---------|--------------|------------------|
| UI state | `@Observable` / `ObservableObject` | `ViewModel` + `StateFlow` / Compose state |
| Persistence | SwiftData, JSON file cache, UserDefaults / App Group | Room, DataStore, file cache |
| Networking | `URLSession` + async/await | OkHttp + coroutines |
| Sync | CloudKit-ready protocol + local fallback (Sync Notes) | Room always; Firestore optional flag |
| Widgets | WidgetKit + ActivityKit (Focus Timer) | Glance (Focus Timer) |
| DI / testability | Protocol-backed services for fakes & previews | Interfaces + ViewModel injection |

Interview angle: explain *why* Pulse Weather uses a file cache instead of SwiftData/Room, and *why* Sync Notes stays usable with cloud disabled.

---

## How to open & run

### iOS (Mac + Xcode 15+)

Each iOS app ships a XcodeGen `project.yml` (no checked-in `.xcodeproj`).

```bash
brew install xcodegen
cd Apps/FocusTimer   # or SpotJournal / PulseWeather / LedgerLite / SyncNotes
xcodegen generate
open *.xcodeproj
```

1. Select the app scheme.
2. Set your **Signing Team**.
3. Run on simulator or device (iOS 17.0+).
4. Focus Timer widgets: add the Widget extension target to the Home Screen; Live Activities need a capable device/simulator.
5. Sync Notes CloudKit: enable iCloud + the container in Signing & Capabilities (local mode works without it).

Manual Xcode project creation is documented in each app README if you prefer not to use XcodeGen.

### Android (Android Studio Hedgehog+ · JDK 17 · SDK 34)

```bash
cd Android/FocusTimer   # or SpotJournal / PulseWeather / LedgerLite / SyncNotes
./gradlew :app:assembleDebug
# APK: app/build/outputs/apk/debug/app-debug.apk
```

Or **File → Open** the app folder in Android Studio and press Run.  
Do **not** commit `local.properties`, `google-services.json`, or APKs.  
All five Android apps have verified `assembleDebug` success on this portfolio’s CI workstation snapshot (Nov 2026).

See [Android/README.md](Android/README.md) for the Android matrix and Firebase enable steps.

---

## What each app proves in an interview

Use these as talking-point anchors (expand with code walkthroughs):

| App | Prove you can… |
|-----|----------------|
| **Focus Timer** | Ship background-adjacent UX: timers, notifications, App Group / widget timelines, Live Activity or Glance updates without blocking the UI thread. |
| **Spot Journal** | Combine media + location + persistence; explain permission flows, map choice (MapKit vs OSM), and photo storage strategy. |
| **Pulse Weather** | Own a networking stack: decode, caching, offline UX, units, and graceful API failure — with zero secret keys. |
| **Ledger Lite** | Model structured domain data (categories, filters, aggregates), charts, and CSV export / share. |
| **Sync Notes** | Design sync as a *layer*: local-first, conflict policy, cloud optional — defend CloudKit vs Firestore tradeoffs for JP product contexts. |

Deeper bullets live in each app’s README (`Interview talking points` sections on iOS).

---

## Honest limitations

| Topic | Reality |
|-------|---------|
| **iOS build verification** | Sources + `project.yml` are complete; full Xcode compile/sign happens on a Mac with your Team ID. |
| **Android build verification** | `assembleDebug` succeeded for all five (Nov 2026). Emulator/device UX still needs human QA screenshots. |
| **App icons** | Asset catalogs / mipmaps exist; replace placeholders with final PNGs before March 2027 freeze. |
| **Widgets / Live Activities** | Need correct signing, App Groups, and (for Live Activities) capable hardware. |
| **CloudKit (Sync Notes iOS)** | Requires Apple Developer Program + iCloud container. Local fallback is the default demo path. |
| **Firebase (Sync Notes Android)** | Off by default. Enabling needs your own Firebase project + `google-services.json` (never commit secrets). |
| **Screenshots** | Folders under [`docs/screenshots/`](docs/screenshots/) are ready; drop device PNGs before applications. |
| **Tests / CI** | Roadmap items for Nov–Dec 2026 — not yet the primary signal of this repo. |

---

## Roadmap & weekly improvement

**Path:** November 2026 → March 2027 readiness → May 2027 applications.

| Phase | Theme |
|-------|--------|
| Nov–Dec 2026 | Harden the existing five (tests, CI, a11y, error states, icons) |
| Jan 2027 | Networking / offline / sync depth; optional 6th sophisticated app |
| Feb 2027 | Polish, instrumentation, performance, bilingual docs, sample interview Q&A |
| Mar 2027 | Feature freeze, screenshots, release notes, readiness checklist |

Full plan with concrete work items: **[ROADMAP.md](ROADMAP.md)**.

A recurring routine drives weekly commits (tests, polish, or a sophistication slice) so the repo keeps looking like active, experienced ownership — not a one-shot dump.

---

## Repository layout

```
Apps/                 # iOS SwiftUI — XcodeGen project.yml per app
  FocusTimer/
  SpotJournal/
  PulseWeather/
  LedgerLite/
  SyncNotes/
Android/              # Android Compose — standalone Gradle projects
  FocusTimer/
  SpotJournal/
  PulseWeather/
  LedgerLite/
  SyncNotes/
docs/
  architecture.md
  screenshots/        # per-app folders for PNGs
ROADMAP.md            # Nov 2026 → Mar 2027 plan
README.md             # this file
LICENSE
```

---

## License & contact

MIT — see [LICENSE](LICENSE). Code may be referenced in portfolios and interviews.

**Author:** Jian Wang  
**GitHub:** [wjdartmouth](https://github.com/wjdartmouth)  
**Contact:** _(add preferred email / LinkedIn before applications)_

---

*Last documentation pass: November 2026 · Target readiness: March 2027*

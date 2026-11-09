# Architecture notes

Shared patterns for [mobile-dev-portfolio](https://github.com/wjdartmouth/mobile-dev-portfolio).  
Hiring context and timeline: [../ROADMAP.md](../ROADMAP.md).

## Shared patterns (iOS)

All five iOS apps follow the same shape:

```
Sources/
  App/           # @main entry, root scene, dependency wiring
  Models/        # Codable / @Model types
  Services/      # persistence, networking, notifications, location
  ViewModels/    # @Observable / ObservableObject
  Views/         # SwiftUI screens + reusable components
  Utilities/     # formatters, constants, helpers
```

Widget extensions (Focus Timer) live under `Widgets/` with a shared App Group–friendly model layer.

## Shared patterns (Android)

```
app/src/main/java/.../
  ui/            # Compose screens, theme
  viewmodel/     # ViewModel + UI state
  data/          # Room, DataStore, repositories
  network/       # API clients (where needed)
  widget/        # Glance (Focus Timer)
```

Each Android app is a **standalone Gradle project** (not a single multi-module monorepo yet). Modularization (`:core:ui`, `:core:data`) is a Phase 2 roadmap item if a 6th app lands.

## State & concurrency

| Platform | Approach |
|----------|----------|
| iOS | Prefer `@Observable` (Observation) on iOS 17+; `async/await`; avoid blocking the main actor on network/disk |
| Android | `ViewModel` + coroutines / `StateFlow`; Room and network off the main thread |

## Persistence choices

| App | iOS store | Android store | Why |
|-----|-----------|---------------|-----|
| Focus Timer | SwiftData + settings | Room + DataStore | Sessions queryable; settings are simple keys |
| Spot Journal | SwiftData | Room | Photos metadata + coordinates + notes |
| Pulse Weather | File cache (JSON) + memory | File / memory cache | Ephemeral forecast; no need for full DB |
| Ledger Lite | SwiftData | Room | Relational expenses + categories |
| Sync Notes | SwiftData + CloudKit protocol | Room + optional Firestore | Local-first; cloud when configured |

## Sync strategies

- **Local-first:** Sync Notes always works offline; cloud is an optional mirror.
- **Conflict policy:** Document and implement explicitly (default narrative: last-write-wins with updatedAt; field-merge as stretch goal — see ROADMAP Phase 2).
- **Interview stance:** Defend CloudKit (Apple ecosystem, silent push) vs Firestore (cross-platform, security rules) for a Japan product context.

## Testing mindset (interview talking point)

Services should stay protocol- / interface-backed (`WeatherServing`, `NoteSyncing`, `LocationProviding`, repositories) so fakes inject cleanly in previews and unit tests. Roadmap Phase 1 turns this into real XCTest / JUnit coverage.

## Related

- Root [README.md](../README.md)
- [Android/README.md](../Android/README.md)
- Per-app READMEs under `Apps/*` and `Android/*`

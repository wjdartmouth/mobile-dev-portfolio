# Architecture notes

## Shared patterns

All five apps follow the same shape:

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

## State & concurrency

- Prefer `@Observable` (Observation framework) on iOS 17+.
- Networking and disk I/O use `async/await` and actor isolation where useful.
- UI never blocks on the main actor for network or heavy file work.

## Persistence choices

| App | Store | Why |
|-----|-------|-----|
| Focus Timer | SwiftData + UserDefaults settings | Sessions are queryable; settings are simple keys |
| Spot Journal | SwiftData | Photos metadata + coordinates + notes |
| Pulse Weather | File cache (JSON) + memory | Ephemeral forecast; no need for SwiftData |
| Ledger Lite | SwiftData | Relational expenses + categories |
| Sync Notes | SwiftData local + CloudKit mirror protocol | Works offline; CloudKit when signed in |

## Testing mindset (interview talking point)

Each service is protocol-backed (`WeatherServing`, `NoteSyncing`, `LocationProviding`) so you can inject fakes in previews and unit tests. Previews ship with sample data builders.

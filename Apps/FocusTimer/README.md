# Focus Timer

A polished Pomodoro-style focus timer built with SwiftUI. Demonstrates **WidgetKit**, **ActivityKit Live Activities**, **local notifications**, **Swift Charts**, **SwiftData**, and fluid timer animations.

## Features

- Focus / short break / long break phases with configurable durations
- Circular progress ring with numeric text transitions
- Session history with 14-day bar chart, totals, and streak
- Local notifications when a phase ends
- Home Screen widgets (small / medium) + Lock Screen accessories
- Live Activity + Dynamic Island countdown (device / supported simulator)
- Haptics, auto-start toggles, App Group–shared widget snapshot
- Dark Mode friendly, VoiceOver labels on primary controls

## Architecture

```
Sources/
  App/            FocusTimerApp, RootTabView
  Models/         FocusSession (SwiftData), TimerSettings
  Services/       Notification, Live Activity, Haptics
  ViewModels/     TimerViewModel, HistoryViewModel
  Views/          Timer, History, Settings
  Utilities/      Formatters
Shared/           App Group, TimerPhase, WidgetSnapshot, ActivityAttributes
Widgets/          Home, Lock Screen, Live Activity
```

MVVM: views observe `@Observable` view models; persistence via SwiftData `ModelContext`.

## Requirements

- iOS 17.0+
- Xcode 15+
- Apple Developer Team for device + Live Activities
- App Group: `group.com.jianwang.portfolio.focustimer` (already in entitlements)

## Run

```bash
brew install xcodegen
cd Apps/FocusTimer
xcodegen generate
open FocusTimer.xcodeproj
```

Select the **FocusTimer** scheme, set your signing team, and run on a simulator or device.  
To try widgets: long-press Home Screen → Add Widget → Focus Timer.  
Live Activities need a physical device (or a simulator with Dynamic Island).

## Interview talking points

- Shared App Group state between app and widget extension
- `TimelineProvider` refresh policy vs Live Activity push-less updates
- SwiftData predicates for “today’s completed focus sessions”
- Clean separation of timer ticking (`Task` loop) from UI

## Screenshots

_Add PNGs under `../../docs/screenshots/focus-timer/` when available._

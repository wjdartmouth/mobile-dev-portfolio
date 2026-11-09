# Sync Notes

A notes app with a **CloudKit-ready sync layer** and a **local fallback** that uses the same protocol. Demonstrates account status UI, soft deletes, revision-based conflict handling, search, and list/detail editing.

## Features

- SwiftData local store (source of truth on device)
- `NoteSyncing` protocol with:
  - `CloudKitNoteSyncService` — private CloudKit database
  - `LocalNoteSyncService` — in-memory stand-in when iCloud is unavailable
- Account status UI (available / no account / restricted / …)
- Autosave editor with revision bump + deferred push
- Pin / search / soft-delete
- Documented last-writer-wins conflict policy

## Architecture

```
Sources/
  App/                 SyncNotesApp
  Models/              Note (@Model)
  Services/Sync/       NoteSyncing, CloudKitNoteSyncService,
                       LocalNoteSyncService, NoteRepository
  ViewModels/          NotesListViewModel
  Views/               Root list, Editor, AccountStatusView
```

`NoteRepository` decides the active backend after checking `CKContainer.accountStatus()`.

## CloudKit setup (device / TestFlight)

1. Apple Developer Program membership required.
2. In Xcode → Signing & Capabilities → add **iCloud** with CloudKit.
3. Ensure container `iCloud.com.jianwang.portfolio.syncnotes` exists (entitlements file already references it).
4. Sign in to iCloud on the simulator/device.
5. Tap the sync button — status banner should show **iCloud Ready**.

Without a team / container / account, the app **fully works** in local fallback mode.

## Conflict handling

| Situation | Resolution |
|-----------|------------|
| Remote `revision` > local | Apply remote fields |
| Revisions equal | Prefer later `updatedAt` |
| Remote `isDeleted` | Delete local copy |
| Push fails | Keep local; surface message; retry on next sync |

This is intentional LWW (last-writer-wins) — simple to explain in interviews. For production you might add merge UI for title/body conflicts.

## Requirements

- iOS 17.0+
- CloudKit optional (local fallback always works)
- Xcode signing team for real iCloud sync

## Run

```bash
brew install xcodegen
cd Apps/SyncNotes
xcodegen generate
open SyncNotes.xcodeproj
```

## Screenshots

_Add PNGs under `../../docs/screenshots/sync-notes/` when available._

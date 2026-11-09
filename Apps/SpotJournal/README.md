# Spot Journal

A location-aware photo journal. Capture a shot (camera or library), write a short note, and drop a map pin — then browse entries in a list or on a MapKit map.

## Features

- SwiftData persistence with external storage for photo blobs
- PhotosPicker + UIImagePickerController camera
- CoreLocation with reverse geocoding for place names
- MapKit map with custom photo annotations and user location
- Search + sort (newest / oldest / title)
- Empty states, validation, Dark Mode, accessibility labels

## Architecture

```
Sources/
  App/           SpotJournalApp
  Models/        JournalEntry (@Model)
  Services/      LocationService, ImageStore
  ViewModels/    JournalListViewModel, EntryEditorViewModel
  Views/         List, Map, Editor, CameraPicker, Detail
```

## Requirements

- iOS 17.0+
- Location, Camera, and Photo Library permissions (Info.plist keys included)
- Simulator: use the Features → Location menu to simulate a fix; camera falls back to photo library

## Run

```bash
brew install xcodegen
cd Apps/SpotJournal
xcodegen generate
open SpotJournal.xcodeproj
```

## Interview talking points

- `@Attribute(.externalStorage)` for large photo data in SwiftData
- Bridging `async` location updates with `CheckedContinuation`
- MapKit `Annotation` with thumbnail images
- Separating list filtering from the persistence layer

## Screenshots

_Add PNGs under `../../docs/screenshots/spot-journal/` when available._

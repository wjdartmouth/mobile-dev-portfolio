# Ledger Lite

A personal expense tracker with **SwiftData**, category charts, search/filter, form validation, CSV export, and strong accessibility labels.

## Features

- Seeded categories (Food, Transport, Shopping, Bills, Health, Entertainment, Other)
- Add / edit / delete expenses with amount validation
- Date range filters (all / this month / last 7 days) + category chips
- Donut chart via Swift Charts (`SectorMark`)
- CSV export through the system share sheet
- VoiceOver-friendly rows, chips, and validation messages
- Dark Mode friendly

## Architecture

```
Sources/
  App/            LedgerLiteApp
  Models/         Expense, ExpenseCategory
  Services/       SeedData, CSVExporter
  ViewModels/     LedgerViewModel, ExpenseFormViewModel
  Views/          Root, Form, Components (chart, chips, share)
  Utilities/      MoneyFormat
```

## Requirements

- iOS 17.0+
- No special entitlements

## Run

```bash
brew install xcodegen
cd Apps/LedgerLite
xcodegen generate
open LedgerLite.xcodeproj
```

## Interview talking points

- SwiftData relationships (`Expense` ↔ `ExpenseCategory`) with nullify delete rule
- Keeping export scoped to the *filtered* dataset users see
- Accessibility: combining children + explicit labels/hints
- Form validation before hitting the store

## Screenshots

_Add PNGs under `../../docs/screenshots/ledger-lite/` when available._

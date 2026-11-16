# Ledger Lite (Android)

Expense tracker with Room, category filters, donut chart, form validation, CSV share via Intent, and accessibility labels.

```bash
cd Android/LedgerLite && ./gradlew :app:assembleDebug :app:testDebugUnitTest
```

`LedgerAnalytics` filters and totals the list the screen shows. `LedgerCsv` builds the export string (commas in titles and notes become spaces, matching the existing share sheet).

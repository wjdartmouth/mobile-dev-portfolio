# Sync Notes (Android)

Notes app with **Room always on**, last-write-wins by revision, and an optional Firestore sync layer.

## Default behavior
- Fully functional **local-only** mode — no `google-services.json` required.
- Sync mode toggle in the UI; selecting Firestore without Firebase enabled shows a snackbar and stays local.

## Enable Firebase (optional)
1. Create a Firebase project and download `google-services.json` into `app/`.
2. In the root `build.gradle.kts`, add:
   `id("com.google.gms.google-services") version "4.4.2" apply false`
3. In `app/build.gradle.kts`:
   - `id("com.google.gms.google-services")`
   - Uncomment the Firebase BOM / Firestore / Auth dependencies
   - Set `buildConfigField("boolean", "FIREBASE_SYNC_ENABLED", "true")`
4. Implement the real Firestore calls inside `FirestoreNoteSyncService` (stubs are marked).
5. Conflict policy: higher `revision` wins; if equal, higher `updatedAt` wins.

```bash
cd Android/SyncNotes && ./gradlew :app:assembleDebug
```

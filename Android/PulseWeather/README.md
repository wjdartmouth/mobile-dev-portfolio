# Pulse Weather (Android)

Open-Meteo weather client (no API key) with city search, device location, °C/°F toggle, hourly chart, and offline file cache.

```bash
cd Android/PulseWeather && ./gradlew :app:assembleDebug :app:testDebugUnitTest
```

Fixtures under `app/src/test/resources/openmeteo/` decode the legacy `current_weather` / `weathercode` fields, including a missing `results` list and optional `precipitation_probability`. Unknown WMO codes map to `Unknown`.

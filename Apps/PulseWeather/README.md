# Pulse Weather

A clean weather client powered by **Open-Meteo** (no API key). Shows current conditions, hourly chart, and a 7-day forecast with offline cache + city search.

## Features

- async/await networking + `Codable` models for Open-Meteo forecast & geocoding
- Offline JSON cache in Caches directory (shows banner when stale/offline)
- Pull-to-refresh, °C/°F toggle, SF Symbol weather icons by WMO code
- City search autocomplete + “use current location”
- Swift Charts line/area for hourly temperatures
- Loading / error / empty-capable UI, Dark Mode

## Architecture

```
Sources/
  App/            PulseWeatherApp
  Models/         WeatherBundle, GeoResult, forecast DTOs
  Services/       OpenMeteoClient (WeatherServing), WeatherCache actor, LocationProvider
  ViewModels/     WeatherViewModel
  Views/          Root, Dashboard
  Utilities/      WeatherCode → SF Symbol + copy
```

Protocol-based `WeatherServing` makes the client swappable for tests/previews.

## Requirements

- iOS 17.0+
- Network for live data (cache works offline after first successful fetch)
- Location permission optional (city search always available)

## Run

```bash
brew install xcodegen
cd Apps/PulseWeather
xcodegen generate
open PulseWeather.xcodeproj
```

## API

- Forecast: `https://api.open-meteo.com/v1/forecast`
- Geocoding: `https://geocoding-api.open-meteo.com/v1/search`

No keys, no secrets.

## Screenshots

_Add PNGs under `../../docs/screenshots/pulse-weather/` when available._

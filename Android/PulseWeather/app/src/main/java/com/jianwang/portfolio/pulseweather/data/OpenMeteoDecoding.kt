package com.jianwang.portfolio.pulseweather.data

import kotlinx.serialization.json.Json

object OpenMeteoDecoding {
    private val json = Json { ignoreUnknownKeys = true }

    fun decodeCities(body: String): List<GeoResult> =
        json.decodeFromString<GeoResponse>(body).results.orEmpty()

    fun decodeForecast(body: String): ForecastResponse =
        json.decodeFromString(body)

    fun toBundle(
        decoded: ForecastResponse,
        placeName: String,
        latitude: Double,
        longitude: Double,
        fetchedAt: Long
    ): WeatherBundle {
        val current = decoded.currentWeather ?: error("No current weather")
        val hourly = decoded.hourly
        val points = if (hourly != null) {
            hourly.time.indices.take(24).map { i ->
                HourlyPoint(
                    time = hourly.time[i],
                    tempC = hourly.temperature2m.getOrElse(i) { current.temperature },
                    code = hourly.weathercode.getOrElse(i) { current.weathercode }
                )
            }
        } else {
            emptyList()
        }
        val daily = decoded.daily
        val days = if (daily != null) {
            daily.time.indices.map { i ->
                DailyPoint(
                    date = daily.time[i],
                    code = daily.weathercode.getOrElse(i) { 0 },
                    maxC = daily.tempMax.getOrElse(i) { current.temperature },
                    minC = daily.tempMin.getOrElse(i) { current.temperature }
                )
            }
        } else {
            emptyList()
        }
        return WeatherBundle(
            placeName = placeName,
            latitude = latitude,
            longitude = longitude,
            currentTempC = current.temperature,
            windspeed = current.windspeed,
            weatherCode = current.weathercode,
            hourly = points,
            daily = days,
            fetchedAt = fetchedAt
        )
    }
}

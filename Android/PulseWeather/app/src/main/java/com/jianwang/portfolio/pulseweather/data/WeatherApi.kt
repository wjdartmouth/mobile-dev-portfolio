package com.jianwang.portfolio.pulseweather.data

import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json

class WeatherApi {
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun searchCities(query: String): List<GeoResult> = withContext(Dispatchers.IO) {
        val q = query.trim()
        require(q.isNotEmpty()) { "Enter a city name" }
        val url = "https://geocoding-api.open-meteo.com/v1/search?name=${URLEncoder.encode(q, "UTF-8")}&count=8&language=en&format=json"
        val body = httpGet(url)
        json.decodeFromString<GeoResponse>(body).results.orEmpty()
    }

    suspend fun forecast(lat: Double, lon: Double, placeName: String): WeatherBundle = withContext(Dispatchers.IO) {
        val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon" +
            "&current_weather=true" +
            "&hourly=temperature_2m,weathercode" +
            "&daily=weathercode,temperature_2m_max,temperature_2m_min" +
            "&timezone=auto&forecast_days=7"
        val body = httpGet(url)
        val decoded = json.decodeFromString<ForecastResponse>(body)
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
        } else emptyList()
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
        } else emptyList()
        WeatherBundle(
            placeName = placeName,
            latitude = lat,
            longitude = lon,
            currentTempC = current.temperature,
            windspeed = current.windspeed,
            weatherCode = current.weathercode,
            hourly = points,
            daily = days,
            fetchedAt = System.currentTimeMillis()
        )
    }

    private fun httpGet(url: String): String {
        val conn = (URL(url).openConnection() as HttpURLConnection).apply {
            connectTimeout = 15_000
            readTimeout = 15_000
            requestMethod = "GET"
        }
        try {
            val code = conn.responseCode
            val stream = if (code in 200..299) conn.inputStream else conn.errorStream
            val body = stream.bufferedReader().use { it.readText() }
            if (code !in 200..299) error("HTTP $code: $body")
            return body
        } finally {
            conn.disconnect()
        }
    }
}

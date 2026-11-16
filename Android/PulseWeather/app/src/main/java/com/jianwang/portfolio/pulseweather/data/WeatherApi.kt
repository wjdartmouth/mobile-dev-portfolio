package com.jianwang.portfolio.pulseweather.data

import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class WeatherApi {
    suspend fun searchCities(query: String): List<GeoResult> = withContext(Dispatchers.IO) {
        val q = query.trim()
        require(q.isNotEmpty()) { "Enter a city name" }
        val url = "https://geocoding-api.open-meteo.com/v1/search?name=${URLEncoder.encode(q, "UTF-8")}&count=8&language=en&format=json"
        val body = httpGet(url)
        OpenMeteoDecoding.decodeCities(body)
    }

    suspend fun forecast(lat: Double, lon: Double, placeName: String): WeatherBundle = withContext(Dispatchers.IO) {
        val url = "https://api.open-meteo.com/v1/forecast?latitude=$lat&longitude=$lon" +
            "&current_weather=true" +
            "&hourly=temperature_2m,weathercode" +
            "&daily=weathercode,temperature_2m_max,temperature_2m_min" +
            "&timezone=auto&forecast_days=7"
        val body = httpGet(url)
        val decoded = OpenMeteoDecoding.decodeForecast(body)
        OpenMeteoDecoding.toBundle(
            decoded = decoded,
            placeName = placeName,
            latitude = lat,
            longitude = lon,
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

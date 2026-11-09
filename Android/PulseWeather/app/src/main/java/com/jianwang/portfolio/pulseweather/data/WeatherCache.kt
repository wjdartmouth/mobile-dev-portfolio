package com.jianwang.portfolio.pulseweather.data

import android.content.Context
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import java.io.File
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

private val Context.dataStore by preferencesDataStore("weather_prefs")

@kotlinx.serialization.Serializable
data class CachedWeather(
    val placeName: String,
    val latitude: Double,
    val longitude: Double,
    val currentTempC: Double,
    val windspeed: Double,
    val weatherCode: Int,
    val hourly: List<HourlyPoint>,
    val daily: List<DailyPoint>,
    val fetchedAt: Long
)

class WeatherCache(private val context: Context) {
    private val json = Json { ignoreUnknownKeys = true }
    private val useFahrenheit = booleanPreferencesKey("use_f")
    private val lastPlace = stringPreferencesKey("last_place")

    val preferFahrenheit: Flow<Boolean> = context.dataStore.data.map { it[useFahrenheit] ?: false }

    suspend fun setPreferFahrenheit(value: Boolean) {
        context.dataStore.edit { it[useFahrenheit] = value }
    }

    suspend fun save(bundle: WeatherBundle) {
        val cached = CachedWeather(
            placeName = bundle.placeName,
            latitude = bundle.latitude,
            longitude = bundle.longitude,
            currentTempC = bundle.currentTempC,
            windspeed = bundle.windspeed,
            weatherCode = bundle.weatherCode,
            hourly = bundle.hourly,
            daily = bundle.daily,
            fetchedAt = bundle.fetchedAt
        )
        File(context.filesDir, "weather_cache.json").writeText(json.encodeToString(cached))
        context.dataStore.edit { it[lastPlace] = bundle.placeName }
    }

    fun load(): WeatherBundle? {
        val file = File(context.filesDir, "weather_cache.json")
        if (!file.exists()) return null
        return runCatching {
            val c = json.decodeFromString<CachedWeather>(file.readText())
            WeatherBundle(
                placeName = c.placeName,
                latitude = c.latitude,
                longitude = c.longitude,
                currentTempC = c.currentTempC,
                windspeed = c.windspeed,
                weatherCode = c.weatherCode,
                hourly = c.hourly,
                daily = c.daily,
                fetchedAt = c.fetchedAt,
                fromCache = true
            )
        }.getOrNull()
    }

    suspend fun lastPlaceName(): String? = context.dataStore.data.first()[lastPlace]
}

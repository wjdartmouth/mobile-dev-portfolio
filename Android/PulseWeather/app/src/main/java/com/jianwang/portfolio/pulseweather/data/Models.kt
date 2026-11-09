package com.jianwang.portfolio.pulseweather.data
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

@Serializable
data class GeoResponse(val results: List<GeoResult>? = null)

@Serializable
data class GeoResult(
    val id: Long? = null,
    val name: String,
    val latitude: Double,
    val longitude: Double,
    @SerialName("country_code") val countryCode: String? = null,
    val admin1: String? = null
) {
    val display: String get() = listOfNotNull(name, admin1, countryCode).joinToString(", ")
}

@Serializable
data class ForecastResponse(
    val latitude: Double,
    val longitude: Double,
    @SerialName("current_weather") val currentWeather: CurrentWeather? = null,
    val hourly: HourlyBlock? = null,
    val daily: DailyBlock? = null
)

@Serializable
data class CurrentWeather(
    val temperature: Double,
    val windspeed: Double,
    val weathercode: Int,
    val time: String
)

@Serializable
data class HourlyBlock(
    val time: List<String> = emptyList(),
    @SerialName("temperature_2m") val temperature2m: List<Double> = emptyList(),
    @SerialName("weathercode") val weathercode: List<Int> = emptyList()
)

@Serializable
data class DailyBlock(
    val time: List<String> = emptyList(),
    @SerialName("weathercode") val weathercode: List<Int> = emptyList(),
    @SerialName("temperature_2m_max") val tempMax: List<Double> = emptyList(),
    @SerialName("temperature_2m_min") val tempMin: List<Double> = emptyList()
)

data class WeatherBundle(
    val placeName: String,
    val latitude: Double,
    val longitude: Double,
    val currentTempC: Double,
    val windspeed: Double,
    val weatherCode: Int,
    val hourly: List<HourlyPoint>,
    val daily: List<DailyPoint>,
    val fetchedAt: Long,
    val fromCache: Boolean = false
)

@Serializable
data class HourlyPoint(val time: String, val tempC: Double, val code: Int)

@Serializable
data class DailyPoint(val date: String, val code: Int, val maxC: Double, val minC: Double)

fun weatherLabel(code: Int): String = when (code) {
    0 -> "Clear"
    1, 2 -> "Partly cloudy"
    3 -> "Overcast"
    45, 48 -> "Fog"
    51, 53, 55 -> "Drizzle"
    61, 63, 65 -> "Rain"
    71, 73, 75 -> "Snow"
    80, 81, 82 -> "Showers"
    95, 96, 99 -> "Thunderstorm"
    else -> "Weather"
}

package com.jianwang.portfolio.pulseweather.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class OpenMeteoDecodingTest {
    @Test
    fun forecastFixtureDecodesLegacyOpenMeteoFields() {
        val decoded = OpenMeteoDecoding.decodeForecast(fixture("openmeteo/forecast.json"))
        val bundle = OpenMeteoDecoding.toBundle(
            decoded = decoded,
            placeName = "Tokyo",
            latitude = 35.6895,
            longitude = 139.6917,
            fetchedAt = 1_763_270_400_000
        )

        assertEquals(61, bundle.weatherCode)
        assertEquals(18.2, bundle.currentTempC, 0.0001)
        assertEquals(12.4, bundle.windspeed, 0.0001)
        assertEquals(listOf(61, 0), bundle.hourly.map { it.code })
        assertEquals(listOf(61, 95), bundle.daily.map { it.code })
        assertEquals("Tokyo", bundle.placeName)
        assertNull(decoded.hourly?.precipitationProbability)
    }

    @Test
    fun precipitationProbabilityIsOptional() {
        val missing = OpenMeteoDecoding.decodeForecast(fixture("openmeteo/forecast.json"))
        assertNull(missing.hourly?.precipitationProbability)

        val present = OpenMeteoDecoding.decodeForecast(fixture("openmeteo/forecast_with_precip.json"))
        assertEquals(listOf(80, 10), present.hourly?.precipitationProbability)
        val bundle = OpenMeteoDecoding.toBundle(present, "Tokyo", 35.0, 139.0, 0L)
        assertEquals(listOf(61, 0, 95), bundle.hourly.map { it.code })
        assertEquals(95, bundle.daily.single().code)
    }

    @Test
    fun missingCurrentWeatherDecodesButCannotMap() {
        val decoded = OpenMeteoDecoding.decodeForecast(fixture("openmeteo/forecast_no_current.json"))
        assertNull(decoded.currentWeather)
        val error = runCatching {
            OpenMeteoDecoding.toBundle(decoded, "Tokyo", 35.0, 139.0, 0L)
        }.exceptionOrNull()
        assertTrue(error is IllegalStateException)
    }

    @Test
    fun geocodingMissingResultsAreEmpty() {
        val cities = OpenMeteoDecoding.decodeCities(fixture("openmeteo/geocoding.json"))
        assertEquals("Tokyo", cities.single().name)
        assertEquals("Tokyo, Tokyo, JP", cities.single().display)
        assertEquals(
            emptyList<GeoResult>(),
            OpenMeteoDecoding.decodeCities(fixture("openmeteo/geocoding_missing_results.json"))
        )
    }

    @Test
    fun hourlyMappingKeepsTheFirst24Hours() {
        val times = (0 until 25).map { index -> "2026-11-16T%02d:00".format(index) }
        val decoded = ForecastResponse(
            latitude = 1.0,
            longitude = 2.0,
            currentWeather = CurrentWeather(temperature = 10.0, windspeed = 1.0, weathercode = 0, time = times[0]),
            hourly = HourlyBlock(
                time = times,
                temperature2m = List(25) { 10.0 },
                weathercode = List(25) { 1 },
                precipitationProbability = null
            ),
            daily = null
        )
        val bundle = OpenMeteoDecoding.toBundle(decoded, "UTC", 1.0, 2.0, 0L)
        assertEquals(24, bundle.hourly.size)
        assertTrue(bundle.daily.isEmpty())
    }
}

class WeatherLabelTest {
    @Test
    fun knownAndUnknownCodes() {
        assertEquals("Clear", weatherLabel(0))
        assertEquals("Rain", weatherLabel(61))
        assertEquals("Thunderstorm", weatherLabel(95))
        assertEquals("Unknown", weatherLabel(999))
    }
}

private fun fixture(path: String): String {
    val stream = checkNotNull(Thread.currentThread().contextClassLoader?.getResourceAsStream(path)) {
        "Missing fixture $path"
    }
    return stream.bufferedReader().use { it.readText() }
}

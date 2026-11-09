package com.jianwang.portfolio.pulseweather.ui

import android.Manifest
import android.app.Application
import android.content.pm.PackageManager
import android.location.Geocoder
import androidx.core.content.ContextCompat
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import com.jianwang.portfolio.pulseweather.PulseWeatherApp
import com.jianwang.portfolio.pulseweather.data.GeoResult
import com.jianwang.portfolio.pulseweather.data.WeatherApi
import com.jianwang.portfolio.pulseweather.data.WeatherBundle
import java.util.Locale
import kotlin.coroutines.resume
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine

sealed interface WeatherUi {
    data object Idle : WeatherUi
    data object Loading : WeatherUi
    data class Ready(val bundle: WeatherBundle, val useF: Boolean) : WeatherUi
    data class Error(val message: String, val cached: WeatherBundle? = null, val useF: Boolean = false) : WeatherUi
}

class WeatherViewModel(app: Application) : AndroidViewModel(app) {
    private val api = WeatherApi()
    private val cache = (app as PulseWeatherApp).cache

    private val _ui = MutableStateFlow<WeatherUi>(WeatherUi.Idle)
    val ui: StateFlow<WeatherUi> = _ui.asStateFlow()

    private val _results = MutableStateFlow<List<GeoResult>>(emptyList())
    val results: StateFlow<List<GeoResult>> = _results.asStateFlow()

    private val _query = MutableStateFlow("")
    val query: StateFlow<String> = _query.asStateFlow()

    private var useF = false

    init {
        viewModelScope.launch {
            cache.preferFahrenheit.collect { f ->
                useF = f
                val cur = _ui.value
                if (cur is WeatherUi.Ready) _ui.value = cur.copy(useF = f)
            }
        }
        viewModelScope.launch {
            cache.load()?.let { _ui.value = WeatherUi.Ready(it, useF) }
        }
    }

    fun setQuery(q: String) { _query.value = q }

    fun search() {
        viewModelScope.launch {
            try {
                _results.value = api.searchCities(_query.value)
            } catch (e: Exception) {
                _results.value = emptyList()
                _ui.value = WeatherUi.Error(e.message ?: "Search failed", cache.load(), useF)
            }
        }
    }

    fun selectCity(geo: GeoResult) {
        loadForecast(geo.latitude, geo.longitude, geo.display)
    }

    fun refresh() {
        val ready = (_ui.value as? WeatherUi.Ready)?.bundle ?: cache.load() ?: return
        loadForecast(ready.latitude, ready.longitude, ready.placeName)
    }

    fun toggleUnit() {
        viewModelScope.launch { cache.setPreferFahrenheit(!useF) }
    }

    fun useDeviceLocation() {
        viewModelScope.launch {
            _ui.value = WeatherUi.Loading
            val loc = currentLocation()
            if (loc == null) {
                _ui.value = WeatherUi.Error("Location permission or fix unavailable", cache.load(), useF)
                return@launch
            }
            val name = reverseGeocode(loc.first, loc.second) ?: "Current location"
            loadForecast(loc.first, loc.second, name)
        }
    }

    private fun loadForecast(lat: Double, lon: Double, name: String) {
        viewModelScope.launch {
            _ui.value = WeatherUi.Loading
            try {
                val bundle = api.forecast(lat, lon, name)
                cache.save(bundle)
                _ui.value = WeatherUi.Ready(bundle, useF)
                _results.value = emptyList()
            } catch (e: Exception) {
                val cached = cache.load()
                _ui.value = WeatherUi.Error(e.message ?: "Network error", cached, useF)
                if (cached != null) _ui.value = WeatherUi.Ready(cached.copy(fromCache = true), useF)
            }
        }
    }

    private suspend fun currentLocation(): Pair<Double, Double>? {
        val ctx = getApplication<Application>()
        val fine = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        if (!fine && !coarse) return null
        return try {
            val client = LocationServices.getFusedLocationProviderClient(ctx)
            val cts = CancellationTokenSource()
            suspendCancellableCoroutine { cont ->
                client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                    .addOnSuccessListener { loc ->
                        cont.resume(loc?.let { it.latitude to it.longitude })
                    }
                    .addOnFailureListener { cont.resume(null) }
                cont.invokeOnCancellation { cts.cancel() }
            }
        } catch (_: Exception) { null }
    }

    private fun reverseGeocode(lat: Double, lon: Double): String? {
        return try {
            if (!Geocoder.isPresent()) return null
            val geo = Geocoder(getApplication(), Locale.getDefault())
            @Suppress("DEPRECATION")
            geo.getFromLocation(lat, lon, 1)?.firstOrNull()?.let {
                listOfNotNull(it.locality, it.adminArea, it.countryCode).joinToString(", ")
            }
        } catch (_: Exception) { null }
    }

    fun hasLocationPermission(): Boolean {
        val ctx = getApplication<Application>()
        return ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED ||
            ContextCompat.checkSelfPermission(ctx, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
    }
}

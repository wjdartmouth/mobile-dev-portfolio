package com.jianwang.portfolio.spotjournal.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Geocoder
import android.location.Location
import android.location.LocationManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import java.util.Locale
import kotlin.coroutines.resume
import kotlinx.coroutines.suspendCancellableCoroutine

data class LatLngLabel(val lat: Double, val lng: Double, val label: String?)

class LocationHelper(private val context: Context) {
    fun hasPermission(): Boolean {
        val fine = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED
        val coarse = ContextCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED
        return fine || coarse
    }

    @SuppressLint("MissingPermission")
    suspend fun current(): LatLngLabel? {
        if (!hasPermission()) return fallbackLastKnown()
        return try {
            val client = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()
            suspendCancellableCoroutine { cont ->
                client.getCurrentLocation(Priority.PRIORITY_BALANCED_POWER_ACCURACY, cts.token)
                    .addOnSuccessListener { loc ->
                        if (loc != null) cont.resume(labelFor(loc.latitude, loc.longitude))
                        else cont.resume(null)
                    }
                    .addOnFailureListener { cont.resume(fallbackLastKnown()) }
                cont.invokeOnCancellation { cts.cancel() }
            }
        } catch (_: Exception) {
            fallbackLastKnown()
        }
    }

    @SuppressLint("MissingPermission")
    private fun fallbackLastKnown(): LatLngLabel? {
        if (!hasPermission()) return null
        val lm = context.getSystemService(Context.LOCATION_SERVICE) as LocationManager
        val providers = listOf(LocationManager.GPS_PROVIDER, LocationManager.NETWORK_PROVIDER)
        var best: Location? = null
        for (p in providers) {
            try {
                val loc = lm.getLastKnownLocation(p) ?: continue
                if (best == null || loc.time > best.time) best = loc
            } catch (_: Exception) { }
        }
        return best?.let { labelFor(it.latitude, it.longitude) }
    }

    private fun labelFor(lat: Double, lng: Double): LatLngLabel {
        val label = try {
            if (Geocoder.isPresent()) {
                val geo = Geocoder(context, Locale.getDefault())
                @Suppress("DEPRECATION")
                geo.getFromLocation(lat, lng, 1)?.firstOrNull()?.let { a ->
                    listOfNotNull(a.thoroughfare, a.locality, a.adminArea).joinToString(", ").ifBlank { null }
                }
            } else null
        } catch (_: Exception) { null }
        return LatLngLabel(lat, lng, label)
    }
}

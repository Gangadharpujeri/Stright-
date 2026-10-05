package com.example.data.location

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import android.location.Address
import android.location.Geocoder
import android.location.Location
import android.os.Build
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale

object LocationHelper {

    fun hasLocationPermission(context: Context): Boolean {
        val fineLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_FINE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        val coarseLocation = ContextCompat.checkSelfPermission(
            context,
            Manifest.permission.ACCESS_COARSE_LOCATION
        ) == PackageManager.PERMISSION_GRANTED
        return fineLocation || coarseLocation
    }

    @SuppressLint("MissingPermission")
    fun requestRealTimeLocation(
        context: Context,
        onSuccess: (latitude: Double, longitude: Double, address: String) -> Unit,
        onError: (String) -> Unit
    ) {
        if (!hasLocationPermission(context)) {
            onError("Location permission not granted. Please enable GPS permissions.")
            return
        }

        try {
            val fusedClient = LocationServices.getFusedLocationProviderClient(context)
            val cts = CancellationTokenSource()

            fusedClient.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token)
                .addOnSuccessListener { location: Location? ->
                    if (location != null) {
                        val lat = location.latitude
                        val lng = location.longitude
                        val address = resolveAddress(context, lat, lng)
                        onSuccess(lat, lng, address)
                    } else {
                        // Fallback to last known location
                        fusedClient.lastLocation.addOnSuccessListener { lastLoc: Location? ->
                            if (lastLoc != null) {
                                val lat = lastLoc.latitude
                                val lng = lastLoc.longitude
                                val address = resolveAddress(context, lat, lng)
                                onSuccess(lat, lng, address)
                            } else {
                                // Default civic fallback coordinates (e.g. city center)
                                val fallbackLat = 37.7749
                                val fallbackLng = -122.4194
                                val fallbackAddr = "Market St & 8th St, Civic Center"
                                onSuccess(fallbackLat, fallbackLng, fallbackAddr)
                            }
                        }.addOnFailureListener {
                            onSuccess(37.7749, -122.4194, "Market St & 8th St, Civic Center")
                        }
                    }
                }
                .addOnFailureListener { exception ->
                    onError(exception.localizedMessage ?: "Failed to acquire GPS fix")
                }
        } catch (e: Exception) {
            onError(e.localizedMessage ?: "Location services error")
        }
    }

    fun resolveAddress(context: Context, latitude: Double, longitude: Double): String {
        return try {
            val geocoder = Geocoder(context, Locale.getDefault())
            val addresses: List<Address>? = geocoder.getFromLocation(latitude, longitude, 1)
            if (!addresses.isNullOrEmpty()) {
                val addr = addresses[0]
                val street = addr.thoroughfare ?: addr.featureName ?: ""
                val subThoroughfare = addr.subThoroughfare ?: ""
                val fullStreet = if (subThoroughfare.isNotEmpty() && street.isNotEmpty()) {
                    "$subThoroughfare $street"
                } else if (street.isNotEmpty()) {
                    street
                } else {
                    addr.getAddressLine(0) ?: ""
                }

                val locality = addr.locality ?: addr.subAdminArea ?: ""
                if (fullStreet.isNotEmpty() && locality.isNotEmpty() && !fullStreet.contains(locality)) {
                    "$fullStreet, $locality"
                } else if (fullStreet.isNotEmpty()) {
                    fullStreet
                } else {
                    String.format(Locale.US, "GPS: %.4f, %.4f", latitude, longitude)
                }
            } else {
                String.format(Locale.US, "GPS: %.4f, %.4f", latitude, longitude)
            }
        } catch (e: Exception) {
            String.format(Locale.US, "GPS: %.4f, %.4f", latitude, longitude)
        }
    }
}

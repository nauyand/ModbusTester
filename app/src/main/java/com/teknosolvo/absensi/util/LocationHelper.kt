package com.teknosolvo.absensi.util

import android.annotation.SuppressLint
import android.content.Context
import android.location.Geocoder
import android.location.Location
import android.os.Build
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

object LocationHelper {

    /** Lokasi GPS terkini. Panggil hanya setelah izin lokasi diberikan. */
    @SuppressLint("MissingPermission")
    suspend fun current(context: Context): Location? {
        val client = LocationServices.getFusedLocationProviderClient(context)
        val cts = CancellationTokenSource()
        return client.getCurrentLocation(Priority.PRIORITY_HIGH_ACCURACY, cts.token).await(cts)
    }

    /** True kalau lokasi berasal dari aplikasi fake GPS / mock location. */
    fun isMock(location: Location): Boolean =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            location.isMock
        } else {
            @Suppress("DEPRECATION")
            location.isFromMockProvider
        }

    fun distance(lat1: Double, lng1: Double, lat2: Double, lng2: Double): Float {
        val result = FloatArray(1)
        Location.distanceBetween(lat1, lng1, lat2, lng2, result)
        return result[0]
    }

    /** Alamat dari koordinat; null kalau offline atau Geocoder tidak tersedia. */
    suspend fun address(context: Context, lat: Double, lng: Double): String? =
        withContext(Dispatchers.IO) {
            runCatching {
                if (!Geocoder.isPresent()) return@runCatching null
                @Suppress("DEPRECATION")
                Geocoder(context, LOCALE_ID).getFromLocation(lat, lng, 1)
                    ?.firstOrNull()
                    ?.getAddressLine(0)
            }.getOrNull()
        }
}

package com.oguzhanp.motorum.data

import android.Manifest
import android.annotation.SuppressLint
import android.content.Context
import android.content.pm.PackageManager
import androidx.core.content.ContextCompat
import com.google.android.gms.location.CurrentLocationRequest
import com.google.android.gms.location.LocationServices
import com.google.android.gms.location.Priority
import com.google.android.gms.tasks.CancellationTokenSource
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class Koordinat(val enlem: Double, val boylam: Double)

@Singleton
class KonumSaglayici @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    private val istemci = LocationServices.getFusedLocationProviderClient(context)

    // Ikisinden biri yetiyor: kullanici "Yaklasik" secmis olabilir, hava
    // durumu icin sehir duzeyi zaten yeterli.
    fun izinVarMi(): Boolean =
        izinVerildi(Manifest.permission.ACCESS_FINE_LOCATION) ||
                izinVerildi(Manifest.permission.ACCESS_COARSE_LOCATION)

    // Ekran da izni kontrol ediyor; burada bir kez daha bakiyoruz cunku
    // "Yalnizca bu sefer" izni ekran acikken sona erebilir.
    @SuppressLint("MissingPermission")
    suspend fun konumAl(): Koordinat? {
        if (!izinVarMi()) return null

        // Once taze olcum. BALANCED: wifi ve baz istasyonundan ~100 m alir,
        // GPS acmaz; hava durumu icin o hassasiyet gereksiz.
        val taze = try {
            val istek = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                .build()
            istemci.getCurrentLocation(istek, CancellationTokenSource().token).await()
        } catch (_: Exception) {
            null
        }

        if (taze != null) return Koordinat(taze.latitude, taze.longitude)

        // Taze olcum gelmedi (kapali alan, pil kisiti, emulator). Yedek olarak
        // son bilinen konum: biraz eski olabilir ama sehir genelde ayni kalir.
        val son = try {
            istemci.lastLocation.await()
        } catch (_: Exception) {
            null
        }

        return son?.let { Koordinat(it.latitude, it.longitude) }
    }

    private fun izinVerildi(izin: String): Boolean =
        ContextCompat.checkSelfPermission(context, izin) == PackageManager.PERMISSION_GRANTED
}

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

// Bu yastan genc bir olcum varsa yenisini istemiyoruz. Hava durumu icin on
// dakika onceki konum bugunku konumdur; yeni olcum beklemek bosuna gecikme.
private const val OLCUM_YASI_MS = 10 * 60 * 1000L

// Taze olcum icin ust sinir. Once bes saniyeydi: hizliydi ama kapali alanda
// olcum o surede gelmeyince konum hic bulunamiyor ve kart "alinamadi" diyordu.
// Taze olcume ancak son bilinen konum da yokken basvuruyoruz, o yuzden burada
// beklemek dogru; alternatifi hava durumunu hic gosterememek.
private const val OLCUM_SURESI_MS = 15_000L

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
    // Sira onemli: once telefonun elindeki konum, sonra yeni olcum.
    // Tersi de denendi ve yanlisti; her acilista sifirdan olcum almak karti
    // saniyelerce bekletiyordu, sureyi kisinca da olcum yetismeyip konum hic
    // bulunamiyordu. Hava durumu icin dunku konum bile ise yarar.
    @SuppressLint("MissingPermission")
    suspend fun konumAl(): Koordinat? {
        if (!izinVarMi()) return null
        return sonBilinen() ?: tazeOlcum()
    }

    // Telefonun baska uygulamalar icin zaten aldigi son konum. Anlik doner,
    // pil harcamaz. Sehir duzeyinde dogruluk hava durumu icin fazlasiyla yeterli.
    @SuppressLint("MissingPermission")
    private suspend fun sonBilinen(): Koordinat? {
        val son = try {
            istemci.lastLocation.await()
        } catch (_: Exception) {
            null
        }
        return son?.let { Koordinat(it.latitude, it.longitude) }
    }

    // Elde hic konum yoksa olculuyor. BALANCED: wifi ve baz istasyonundan
    // ~100 m alir, GPS acmaz; hava durumu icin o hassasiyet gereksiz.
    @SuppressLint("MissingPermission")
    private suspend fun tazeOlcum(): Koordinat? {
        val taze = try {
            val istek = CurrentLocationRequest.Builder()
                .setPriority(Priority.PRIORITY_BALANCED_POWER_ACCURACY)
                // Son on dakika icinde alinmis bir olcum varsa Play Services
                // yenisini almadan onu veriyor.
                .setMaxUpdateAgeMillis(OLCUM_YASI_MS)
                .setDurationMillis(OLCUM_SURESI_MS)
                .build()
            istemci.getCurrentLocation(istek, CancellationTokenSource().token).await()
        } catch (_: Exception) {
            null
        }
        return taze?.let { Koordinat(it.latitude, it.longitude) }
    }

    private fun izinVerildi(izin: String): Boolean =
        ContextCompat.checkSelfPermission(context, izin) == PackageManager.PERMISSION_GRANTED
}

package com.oguzhanp.motorum.data

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AgDurumu @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    // Iki ozellik birlikte soruluyor:
    // INTERNET  -> bu ag internet sundugunu SOYLUYOR.
    // VALIDATED -> Android disari cikabildigini gercekten SINADI.
    // Tek basina INTERNET'e bakmak, giris sayfasina takilmis otel wifi'sinde
    // "internet var" der ve yanlis yonlendirir.
    fun internetVar(): Boolean {
        val yonetici = context.getSystemService(ConnectivityManager::class.java) ?: return false
        val ag = yonetici.activeNetwork ?: return false
        val ozellikler = yonetici.getNetworkCapabilities(ag) ?: return false
        return ozellikler.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET) &&
                ozellikler.hasCapability(NetworkCapabilities.NET_CAPABILITY_VALIDATED)
    }
}

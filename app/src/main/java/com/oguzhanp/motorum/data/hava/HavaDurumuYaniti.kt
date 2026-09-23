package com.oguzhanp.motorum.data.hava

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

// OpenWeather'in cevabinin sekli. Alan adlarini servis seciyor, @SerialName
// onlari bizim adlarimiza bagliyor. Servis bir alanin adini degistirirse
// sadece bu dosya duzeliyor. Uygulamanin kendi sekli: model/HavaDurumu.kt

@Serializable
data class HavaDurumuYaniti(
    // Servis liste donduruyor ama icinde tek eleman oluyor.
    @SerialName("weather") val havaListesi: List<HavaBilgisi> = emptyList(),
    @SerialName("main") val olcumler: OlcumBilgisi = OlcumBilgisi(),
    @SerialName("wind") val ruzgar: RuzgarBilgisi = RuzgarBilgisi(),
    // Metre; servis en fazla 10000 donduruyor.
    @SerialName("visibility") val gorusMesafesi: Int = 10000,
    @SerialName("name") val sehir: String = ""
)

@Serializable
data class HavaBilgisi(
    // Surus karari bu sayidan cikiyor: 5xx yagmur, 6xx kar, 800 acik.
    @SerialName("id") val kod: Int = 800,
    // lang=tr istedigimiz icin Turkce geliyor.
    @SerialName("description") val aciklama: String = ""
)

@Serializable
data class OlcumBilgisi(
    // units=metric istedigimiz icin Santigrat.
    @SerialName("temp") val sicaklik: Double = 0.0
)

@Serializable
data class RuzgarBilgisi(
    // units=metric ile m/s.
    @SerialName("speed") val hiz: Double = 0.0
)

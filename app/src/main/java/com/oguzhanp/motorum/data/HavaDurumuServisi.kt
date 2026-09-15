package com.oguzhanp.motorum.data

import retrofit2.http.GET
import retrofit2.http.Query

internal const val OPENWEATHER_ADRESI = "https://api.openweathermap.org/"

interface HavaDurumuServisi {
    // Govde yok: Retrofit bu arayuzu calisma aninda kendisi uretiyor.
    // Her @Query adresin sonuna bir parametre ekliyor.
    @GET("data/2.5/weather")
    suspend fun havaDurumu(
        @Query("lat") enlem: Double,
        @Query("lon") boylam: Double,
        @Query("appid") anahtar: String,
        @Query("units") birim: String = "metric",
        @Query("lang") dil: String = "tr"
    ): HavaDurumuYaniti
}

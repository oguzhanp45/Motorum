package com.oguzhanp.motorum.di

import com.oguzhanp.motorum.data.hava.HavaDurumuServisi
import com.oguzhanp.motorum.data.hava.OPENWEATHER_ADRESI
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import kotlinx.serialization.json.Json
import okhttp3.MediaType.Companion.toMediaType
import retrofit2.Retrofit
import retrofit2.converter.kotlinx.serialization.asConverterFactory
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AgModulu {

    // ignoreUnknownKeys sart: servis bizim tanimlamadigimiz onlarca alan
    // donduruyor (nem, basinc, gun dogumu). Bu ayar olmasa bilmedigi ilk
    // alanda hata firlatir ve cevabin tamami cope giderdi.
    @Provides
    @Singleton
    fun json(): Json = Json { ignoreUnknownKeys = true }

    // OkHttp'yi ayrica kurmuyoruz, Retrofit varsayilan bir tane uretiyor.
    @Provides
    @Singleton
    fun retrofit(json: Json): Retrofit = Retrofit.Builder()
        .baseUrl(OPENWEATHER_ADRESI)
        .addConverterFactory(json.asConverterFactory("application/json".toMediaType()))
        .build()

    // Arayuzun govdesini Retrofit uretiyor, biz sadece tipini soyluyoruz.
    @Provides
    @Singleton
    fun havaDurumuServisi(retrofit: Retrofit): HavaDurumuServisi =
        retrofit.create(HavaDurumuServisi::class.java)
}

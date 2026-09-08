package com.oguzhanp.motorum.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

// DataStore diske gercek bir dosya yaziyor; uygulama kapansa da kalir.
// Delegate dosya duzeyinde olmak zorunda: ayni isimle ikinci bir ornek
// acilirsa calisma zamaninda hata verir, bu yazim onu garantiliyor.
private val Context.ayarlarDataStore: DataStore<Preferences> by preferencesDataStore(name = "ayarlar")

private val ONBOARDING_BITTI = booleanPreferencesKey("onboarding_bitti")

@Singleton
class AyarlarDeposu @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    // first(): akistan ilk degeri al ve bitir. Bayragi baska kimse degistirmiyor,
    // surekli dinlemeye gerek yok.
    suspend fun onboardingBittiMi(): Boolean =
        context.ayarlarDataStore.data.first()[ONBOARDING_BITTI] ?: false

    suspend fun onboardingiTamamla() {
        context.ayarlarDataStore.edit { tercihler -> tercihler[ONBOARDING_BITTI] = true }
    }
}

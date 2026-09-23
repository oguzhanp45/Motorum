package com.oguzhanp.motorum.data.ayarlar

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.oguzhanp.motorum.model.TemaSecimi
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

// DataStore diske gercek bir dosya yaziyor; uygulama kapansa da kalir.
// Delegate dosya duzeyinde olmak zorunda: ayni isimle ikinci bir ornek
// acilirsa calisma zamaninda hata verir, bu yazim onu garantiliyor.
private val Context.ayarlarDataStore: DataStore<Preferences> by preferencesDataStore(name = "ayarlar")

private val ONBOARDING_BITTI = booleanPreferencesKey("onboarding_bitti")
// Enum'un adi yaziliyor ("KARANLIK"); sirasi degil. Enum'a yeni deger eklenince
// sira kayar ama ad ayni kalir.
private val TEMA = stringPreferencesKey("tema")

// Anahtar kullaniciya bagli. DataStore cihazda duruyor, hesapta degil: duz bir
// "secili_motor" anahtari olsaydi A hesabindan cikip B ile girildiginde B'ye
// A'nin motor kimligi secili gelir ve kayitlar bos gorunurdu.
private fun seciliMotorAnahtari(uid: String) = stringPreferencesKey("secili_motor_$uid")

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

    // Tema cihaza ait, hesaba degil: anahtar kullaniciya bagli degil.
    // Akis olarak veriliyor cunku secim degisince uygulama aninda yeniden
    // renklenmeli; tek seferlik okuma yetmez. Taninmayan deger Sistem'e dusuyor.
    val temaSecimi: Flow<TemaSecimi> = context.ayarlarDataStore.data.map { tercihler ->
        TemaSecimi.entries.firstOrNull { it.name == tercihler[TEMA] } ?: TemaSecimi.SISTEM
    }

    suspend fun temaSeciminiYaz(secim: TemaSecimi) {
        context.ayarlarDataStore.edit { tercihler -> tercihler[TEMA] = secim.name }
    }

    suspend fun seciliMotorId(uid: String): String? =
        context.ayarlarDataStore.data.first()[seciliMotorAnahtari(uid)]

    suspend fun seciliMotoruYaz(uid: String, motorId: String) {
        context.ayarlarDataStore.edit { tercihler ->
            tercihler[seciliMotorAnahtari(uid)] = motorId
        }
    }

    suspend fun seciliMotoruTemizle(uid: String) {
        context.ayarlarDataStore.edit { tercihler ->
            tercihler.remove(seciliMotorAnahtari(uid))
        }
    }
}

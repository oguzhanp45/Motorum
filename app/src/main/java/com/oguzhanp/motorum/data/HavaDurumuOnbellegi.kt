package com.oguzhanp.motorum.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.doublePreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.oguzhanp.motorum.model.HavaDurumu
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import javax.inject.Inject
import javax.inject.Singleton

// Ayarlardan ayri bir DataStore dosyasi. Ayarlar kullanicinin tercihi ve
// kalmali; burasi atilabilir onbellek. Ayri olunca tek dosya silmek yetiyor.
private val Context.havaDataStore: DataStore<Preferences> by preferencesDataStore(name = "hava_durumu")

private val SEHIR = stringPreferencesKey("sehir")
private val SICAKLIK = doublePreferencesKey("sicaklik")
private val ACIKLAMA = stringPreferencesKey("aciklama")
private val KOD = intPreferencesKey("kod")
private val RUZGAR_HIZI = doublePreferencesKey("ruzgar_hizi")
private val GORUS_MESAFESI = intPreferencesKey("gorus_mesafesi")
private val ZAMAN = longPreferencesKey("zaman")

// Bu surenin altindaki veri taze sayiliyor.
private const val TAZELIK_SURESI_MS = 60 * 60 * 1000L

@Singleton
class HavaDurumuOnbellegi @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    // Bayatsa null donuyor. Tarih kontrolu burada bitiyor, cagiran tarafin
    // ayrica bakmasi gerekmiyor.
    suspend fun tazeOku(): HavaDurumu? {
        val tercihler = context.havaDataStore.data.first()
        val zaman = tercihler[ZAMAN] ?: return null
        if (System.currentTimeMillis() - zaman > TAZELIK_SURESI_MS) return null

        return HavaDurumu(
            sehir = tercihler[SEHIR] ?: return null,
            sicaklik = tercihler[SICAKLIK] ?: return null,
            aciklama = tercihler[ACIKLAMA] ?: return null,
            kod = tercihler[KOD] ?: return null,
            ruzgarHizi = tercihler[RUZGAR_HIZI] ?: return null,
            gorusMesafesi = tercihler[GORUS_MESAFESI] ?: return null
        )
    }

    // Ayarlar sayfasinda "Son guncelleme: 14:30" yazisi icin. Bayat olsa da
    // donuyor: burada sorulan "ne zaman yazildi", "hala gecerli mi" degil.
    suspend fun sonGuncelleme(): Long? = context.havaDataStore.data.first()[ZAMAN]

    // Dosyanin tamami gidiyor; bir sonraki acilista hava sunucudan taze geliyor.
    suspend fun temizle() {
        context.havaDataStore.edit { it.clear() }
    }

    // Tek edit blogu: DataStore yedi anahtari butun olarak yaziyor,
    // yarim kalmis bir onbellek olusmuyor.
    suspend fun yaz(hava: HavaDurumu) {
        context.havaDataStore.edit { tercihler ->
            tercihler[SEHIR] = hava.sehir
            tercihler[SICAKLIK] = hava.sicaklik
            tercihler[ACIKLAMA] = hava.aciklama
            tercihler[KOD] = hava.kod
            tercihler[RUZGAR_HIZI] = hava.ruzgarHizi
            tercihler[GORUS_MESAFESI] = hava.gorusMesafesi
            tercihler[ZAMAN] = System.currentTimeMillis()
        }
    }
}

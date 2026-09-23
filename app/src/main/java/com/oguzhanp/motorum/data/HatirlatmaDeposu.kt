package com.oguzhanp.motorum.data

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.first
import kotlinx.serialization.Serializable
import kotlinx.serialization.builtins.ListSerializer
import kotlinx.serialization.json.Json
import javax.inject.Inject
import javax.inject.Singleton

// Kurulu bir hatirlatmanin telefondaki kopyasi. Bildirimde gosterilecek her
// sey burada: alarm caldiginda uygulama kapali ve internet yok olabilir,
// motor adini ya da bakim turunu o an buluttan soramayiz.
@Serializable
data class HatirlatmaKaydi(
    val kayitId: String,
    val motorId: String,
    val motorAdi: String,
    val bakimTuru: String,
    // Hatirlatmanin kuruldugu bakimin tarihi: "12 Mart'taki bakimda..."
    val bakimTarihi: Long,
    val zaman: Long,
    // Ertelendi ama internet yoktu, buluttaki tarih hala eski. Uygulama bir
    // sonraki acilista bunu buluta yaziyor.
    val buluttaGuncellenecek: Boolean = false,
    // Bildirimden "Yaptirdim" dendi ama internet yoktu. Alarmi yok, sadece
    // buluta yazilmayi bekliyor.
    val yapildiBekliyor: Boolean = false,
    // Ayni altyapi belgeleri de tasiyor. Eski kayitlarda bu alan yok:
    // varsayilan bakim. Belgede kayitId "belge-{motor}-{belge}" bicimde.
    val tur: String = TUR_BAKIM,
    // Sadece belgede dolu: bildirim metni ve bir sonraki donem icin.
    val belgeAdi: String = "",
    val belgeBitis: Long = 0L,
    val belgeYenileAy: Int? = null,
    val belgeKacGunOnce: Int = 0
)

internal const val TUR_BAKIM = "BAKIM"
internal const val TUR_BELGE = "BELGE"

// Ayarlardan ve hava onbelleginden ayri dosya: silinmesi digerlerini etkilemesin.
private val Context.hatirlatmaDataStore: DataStore<Preferences> by preferencesDataStore(name = "hatirlatmalar")

private val LISTE = stringPreferencesKey("liste")
// Liste <-> metin cevirici. Acik yazildi; tur cikarimina birakilan surum
// kutuphane surumune gore ek import istiyor.
private val LISTE_SERILESTIRICI = ListSerializer(HatirlatmaKaydi.serializer())

// Liste tek bir JSON metni olarak duruyor. Birkac on kayit icin veritabani
// (Room) kurmak fazla; DataStore zaten projede var.
@Singleton
class HatirlatmaDeposu @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    // Ileride alan eklenirse eski kayitlar okunmaya devam etsin.
    private val json = Json { ignoreUnknownKeys = true }

    suspend fun hepsi(): List<HatirlatmaKaydi> = coz(context.hatirlatmaDataStore.data.first()[LISTE])

    suspend fun bul(kayitId: String): HatirlatmaKaydi? = hepsi().firstOrNull { it.kayitId == kayitId }

    // Ayni kayit varsa yerine yaziliyor: bir kaydin tek hatirlatmasi olur.
    suspend fun yaz(kayit: HatirlatmaKaydi) {
        context.hatirlatmaDataStore.edit { tercihler ->
            val liste = coz(tercihler[LISTE]).filterNot { it.kayitId == kayit.kayitId } + kayit
            tercihler[LISTE] = json.encodeToString(LISTE_SERILESTIRICI, liste)
        }
    }

    suspend fun sil(kayitId: String) {
        context.hatirlatmaDataStore.edit { tercihler ->
            val liste = coz(tercihler[LISTE]).filterNot { it.kayitId == kayitId }
            tercihler[LISTE] = json.encodeToString(LISTE_SERILESTIRICI, liste)
        }
    }

    // Bozuk ya da bos metin bos liste: tek bozuk kayit yuzunden hatirlatmalar cokmesin.
    private fun coz(metin: String?): List<HatirlatmaKaydi> =
        metin?.let { runCatching { json.decodeFromString(LISTE_SERILESTIRICI, it) }.getOrNull() }
            ?: emptyList()
}

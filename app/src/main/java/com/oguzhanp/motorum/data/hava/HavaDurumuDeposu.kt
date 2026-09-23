package com.oguzhanp.motorum.data.hava

import com.oguzhanp.motorum.BuildConfig
import com.oguzhanp.motorum.model.HavaDurumu
import com.oguzhanp.motorum.core.util.DilAyari
import kotlinx.coroutines.withTimeoutOrNull
import javax.inject.Inject
import javax.inject.Singleton

internal const val ANAHTAR_YOK = "local.properties icinde OPENWEATHER_KEY yok"
internal const val CEVAP_BOS = "Cevapta hava bilgisi yok"
internal const val KONUM_YOK = "Konum alinamadi"

// Son guvenlik agi: Play Services kendi suresini uyguluyor ama cagrilar askida
// kalabiliyor. Konum saglayici en kotu durumda 15 saniye olcum bekliyor, bu sinir
// onun bir tik ustunde olmali; yoksa olcum tamamlanmadan biz iptal ederiz.
private const val KONUM_ZAMAN_ASIMI_MS = 20_000L

data class HavaSonucu(
    val hava: HavaDurumu? = null,
    // Ekranda gosterilmiyor: kart tek bir "alinamadi" hali gosteriyor.
    // Buradaki ayrinti hata ararken isimize yariyor.
    val hata: String? = null
)

// Deponun tek isi sirayi bilmek: once onbellek, sonra konum, sonra servis.
// Uc isi de ayri siniflar yapiyor, yarin biri degisirse digerleri duruyor.
@Singleton
class HavaDurumuDeposu @Inject constructor(
    private val servis: HavaDurumuServisi,
    private val konumSaglayici: KonumSaglayici,
    private val onbellek: HavaDurumuOnbellegi
) {

    // zorla = true sadece kullanici listeyi asagi cekince geliyor; o zaman
    // onbellege bakmadan taze veri aliyoruz.
    suspend fun getir(zorla: Boolean = false): HavaSonucu {
        // Anahtar local.properties'e yazilmamis. Istegi gondermeden
        // soyluyoruz, yoksa 401'i yanlis yorumlardik.
        if (BuildConfig.OPENWEATHER_KEY.isBlank()) return HavaSonucu(hata = ANAHTAR_YOK)

        // Taze veri varsa konumu hic sormuyoruz. Pili yiyen sey istek degil,
        // konum olcumu; asil tasarruf burada.
        // Servisin aciklamayi hangi dilde gonderecegi: uygulama Turkce ise tr,
        // diger her durumda en. Baska dil eklersek burasi da buyur.
        val dil = if (DilAyari.etkinDilKodu() == "tr") "tr" else "en"

        if (!zorla) {
            // Onbellek baska dildeyse taze sayilmiyor: eski dildeki aciklama gosterilmesin.
            onbellek.tazeOku(dil)?.let { return HavaSonucu(hava = it) }
        }

        val koordinat = withTimeoutOrNull(KONUM_ZAMAN_ASIMI_MS) { konumSaglayici.konumAl() }
            ?: return HavaSonucu(hata = KONUM_YOK)

        return try {
            val yanit = servis.havaDurumu(
                enlem = koordinat.enlem,
                boylam = koordinat.boylam,
                anahtar = BuildConfig.OPENWEATHER_KEY,
                dil = dil
            )
            val hava = yanit.havayaCevir() ?: return HavaSonucu(hata = CEVAP_BOS)
            // Onbellege ancak basarili cevap yaziliyor.
            onbellek.yaz(hava, dil)
            HavaSonucu(hava = hava)
        } catch (hata: Exception) {
            // Retrofit 2xx disi cevapta istisna firlatiyor ve mesajinda
            // "HTTP 401" gibi kodu tasiyor; oldugu gibi aktariyoruz.
            HavaSonucu(hata = hata.message ?: "Bilinmeyen hata")
        }
    }
}

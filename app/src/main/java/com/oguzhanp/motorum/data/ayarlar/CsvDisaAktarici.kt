package com.oguzhanp.motorum.data.ayarlar

import android.content.Context
import android.net.Uri
import com.oguzhanp.motorum.model.Kayit
import com.oguzhanp.motorum.core.util.formatTarih
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private val TR: Locale = Locale.forLanguageTag("tr")

// Kayitlari telefonda bir dosyaya yaziyor. Dosyanin yerini kullanici seciyor
// (sistemin "farkli kaydet" ekrani); bu yuzden depolama izni gerekmiyor,
// uygulama sadece kullanicinin verdigi o tek adrese yazabiliyor.
@Singleton
class CsvDisaAktarici @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    // null = yazildi, degilse kullaniciya gosterilecek hata.
    // Dosya yazmak ana is parcaciginda yapilmaz; IO'ya geciyoruz.
    suspend fun yaz(hedef: Uri, kayitlar: List<Kayit>): String? = withContext(Dispatchers.IO) {
        try {
            val akis = context.contentResolver.openOutputStream(hedef, "wt")
                ?: return@withContext "Dosya açılamadı"
            akis.use { it.write(kayitlariCsvYap(kayitlar).toByteArray(Charsets.UTF_8)) }
            null
        } catch (hata: Exception) {
            "Dosya yazılamadı"
        }
    }
}

// Saf fonksiyon: dosyaya degil metne ceviriyor, test edilebilir.
//
// Uc bicim karari, hepsi Turkce Excel icin:
// - Ayrac ";" : Turkce Excel virgulu ondalik isareti sayiyor, "," ile ayrilmis
//   dosyada 12,50 iki hucreye bolunurdu.
// - Basta BOM isareti (\uFEFF): Excel onu gormezse dosyayi UTF-8 okumuyor,
//   Turkce harfler bozuk cikiyor.
// - Satir sonu \r\n: CSV standardi bunu istiyor.
fun kayitlariCsvYap(kayitlar: List<Kayit>): String {
    val satirlar = mutableListOf(
        listOf("Tarih", "Kategori", "Tutar (₺)", "Litre", "Km", "Açıklama", "Not")
    )
    // Eskiden yeniye: tabloda zaman asagi dogru aksin.
    kayitlar.sortedBy { it.tarihMillis }.forEach { kayit ->
        val (litre, km, aciklama) = when (kayit) {
            is Kayit.Yakit -> Triple(sayi(kayit.litre), kayit.km?.toString().orEmpty(), "")
            is Kayit.RoadTrip -> Triple(
                "",
                kayit.baslangic.km.toString(),
                kayit.bitis?.let { "${kayit.baslangic.sehir} → ${it.sehir}, ${kayit.mesafe} km" }
                    ?: "${kayit.baslangic.sehir} → (devam ediyor)"
            )
            is Kayit.Bakim -> Triple("", "", kayit.bakimTuru)
            is Kayit.Aksesuar -> Triple("", "", kayit.aksesuarAdi)
        }
        satirlar += listOf(
            formatTarih(kayit.tarihMillis),
            kayit.kategori.etiket,
            sayi(kayit.tutar),
            litre,
            km,
            aciklama,
            kayit.not
        )
    }
    return "\uFEFF" + satirlar.joinToString("\r\n") { satir -> satir.joinToString(";") { hucre(it) } }
}

// Binlik ayraci yok, ondalik virgul: Excel bunu sayi olarak taniyor.
private fun sayi(deger: Double): String = String.format(TR, "%.2f", deger)

// Hucrede ayrac, tirnak ya da satir sonu varsa tirnak icine aliniyor; icindeki
// tirnak iki kez yaziliyor. Yoksa not alanindaki bir ";" satiri kaydirirdi.
private fun hucre(metin: String): String =
    if (metin.any { it == ';' || it == '"' || it == '\n' || it == '\r' })
        "\"" + metin.replace("\"", "\"\"") + "\""
    else metin

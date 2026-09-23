package com.oguzhanp.motorum.feature.istatistik

import androidx.annotation.StringRes
import com.oguzhanp.motorum.R
import com.oguzhanp.motorum.model.Kategori

// Donem bir ekran filtresi, model degil: kayitta boyle bir alan yok,
// sadece listeyi daraltmak icin var. O yuzden model/ altinda degil burada.
// etiket metin degil kimligi: cipin yazisini ekran seciyor.
enum class Donem(@StringRes val etiket: Int) {
    BU_AY(R.string.donem_bu_ay),
    BU_YIL(R.string.donem_bu_yil),
    TUMU(R.string.donem_tumu)
}

data class KategoriPayi(
    val kategori: Kategori,
    val tutar: Double,
    val adet: Int,
    // 0f..1f arasi. Yuzde olarak degil oran olarak tutuluyor: cubugu cizen
    // bilesen de yuzdeyi yazan metin de ayni degerden turuyor.
    val oran: Float
)

// Hem grafik hem en ucuz/en pahali dolum ayni sekli kullaniyor:
// tarih + o dolumun litre fiyati.
data class YakitNoktasi(
    val tarihMillis: Long,
    val birimFiyat: Double
)

data class IstatistikUiState(
    val donem: Donem = Donem.TUMU,
    val kayitAdedi: Int = 0,
    val toplamTutar: Double = 0.0,
    val aylikOrtalama: Double = 0.0,
    val paylar: List<KategoriPayi> = emptyList(),
    val toplamLitre: Double = 0.0,
    val ortalamaBirimFiyat: Double = 0.0,
    val enUcuzDolum: YakitNoktasi? = null,
    val enPahaliDolum: YakitNoktasi? = null,
    // Sayac okumalarindan turuyor, gezi mesafelerinin toplamindan degil.
    val gidilenYol: Int = 0,
    // Bu ise tek bir gezinin kendi iki ucu arasindaki fark: ayri bir sey.
    val enUzunMesafe: Int = 0,
    // Donemin yakit harcamasi / donemde gidilen yol. null = yol ya da yakit yok.
    val kmBasiMaliyet: Double? = null,
    val yakitNoktalari: List<YakitNoktasi> = emptyList()
) {
    // Ekran "hic kayit yok" ile "0 TL harcadin"i ayirt edebilsin diye.
    val bos: Boolean get() = kayitAdedi == 0
}

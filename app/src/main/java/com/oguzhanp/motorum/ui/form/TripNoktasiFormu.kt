package com.oguzhanp.motorum.ui.form

import com.oguzhanp.motorum.util.dakikaAl
import com.oguzhanp.motorum.util.saatAl

// Yolculugun bir ucunun form hali. Gun, saat ve dakika ayri tutuluyor cunku
// iki secici iki ayri sey donduruyor ve modeldeki tek Long'a ancak
// kaydederken birlestiriliyor.
// Ucu de su ana ayarli geliyor: yolculuk genelde yola cikarken kaydediliyor,
// isteyen degistirir.
data class TripNoktasiFormu(
    val tarihMillis: Long = System.currentTimeMillis(),
    val saat: Int = saatAl(System.currentTimeMillis()),
    val dakika: Int = dakikaAl(System.currentTimeMillis()),
    val kmYazi: String = "",
    val sehir: String = "",
    val kmHatali: Boolean = false,
    val sehirHatali: Boolean = false
) {

    // Tek nullable alan: metin sayiya cevrilemezse null doner.
    // Digerlerinde boyle bir cevrim yok, o yuzden onlar nullable degil.
    val km: Int? get() = kmYazi.trim().toIntOrNull()

    // Bitis bolumu doldurulmus mu? Sadece km ve sehre bakiliyor: tarih ve saat
    // varsayilan olarak dolu geldigi icin onlardan doluluk anlasilmaz.
    val bos: Boolean
        get() = kmYazi.isBlank() && sehir.isBlank()

    val gecerli: Boolean
        get() = (km ?: 0) > 0 && sehir.isNotBlank()

    fun dogrula(): TripNoktasiFormu = copy(
        kmHatali = (km ?: 0) <= 0,
        sehirHatali = sehir.isBlank()
    )
}

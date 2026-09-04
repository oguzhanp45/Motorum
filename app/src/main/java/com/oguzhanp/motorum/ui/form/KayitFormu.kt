package com.oguzhanp.motorum.ui.form

import com.oguzhanp.motorum.model.Kategori


 //Kayit formunun ortak verisi ve dogrulama kurallari.
 //Hem ekleme hem detay ekrani ayni formu gosterdigi icin burada duruyor.
 //Kural tek yerde: birini guncelleyip digerini unutma riski yok.
 //googleda 2 ilke var birisi her ekranın kendi uistate olacak ona uymaya çalıştım
 //diger ilkeye göre kontrol tek yerde olmalı o yüzden ayrı bir form clası açıp 2 yerde kullandım.
data class KayitFormu(
    val kategori: Kategori = Kategori.YAKIT,
    val tarihMillis: Long = System.currentTimeMillis(),
    val litreYazi: String = "",
    val tutarYazi: String = "",
    val not: String = "",
    val litreHatali: Boolean = false,
    val tutarHatali: Boolean = false
) {

    // Metni sayiya cevirir. Cevrilemiyorsa (bos, harf, bozuk format) null doner.
    val litre: Double? get() = litreYazi.replace(',', '.').toDoubleOrNull()
    val tutar: Double? get() = tutarYazi.replace(',', '.').toDoubleOrNull()

    // null -> 0.0 sayilir, yani bos da gecersiz, harf de gecersiz, 0 da gecersiz.
    private val litreGecersiz: Boolean get() = (litre ?: 0.0) <= 0.0
    private val tutarGecersiz: Boolean get() = (tutar ?: 0.0) <= 0.0

    // Form kaydedilebilir mi? girdiye bakar
    val gecerli: Boolean get() = !litreGecersiz && !tutarGecersiz

    fun dogrula(): KayitFormu = copy(
        litreHatali = litreGecersiz,
        tutarHatali = tutarGecersiz
    )
}

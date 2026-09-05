package com.oguzhanp.motorum.ui.form

import com.oguzhanp.motorum.model.Kategori


 //Kayit formunun ortak verisi ve dogrulama kurallari.
 //Hem ekleme hem detay ekrani ayni formu gosterdigi icin burada duruyor.
 //Kural tek yerde: birini guncelleyip digerini unutma riski yok.
 //googleda 2 ilke var birisi her ekranın kendi uistate olacak ona uymaya çalıştım
 //diger ilkeye göre kontrol tek yerde olmalı o yüzden ayrı bir form clası açıp 2 yerde kullandım.
sealed interface KayitFormu {
    val kategori: Kategori
    val not: String
    val gecerli: Boolean
    fun dogrula(): KayitFormu

    // not her kategoride ortak ama copy() arayuzde yok, her data class'in kendine ait.
    // Bu fonksiyon sayesinde ekran formun tipini bilmeden notu degistirebiliyor.
    fun notDegistir(yeni: String): KayitFormu

    data class Yakit(
        val tarihMillis: Long = System.currentTimeMillis(),
        val litreYazi: String = "",
        val tutarYazi: String = "",
        override val not: String = "",
        val litreHatali: Boolean = false,
        val tutarHatali: Boolean = false
    ) : KayitFormu {

        override val kategori get() = Kategori.YAKIT

        // Metni sayiya cevirir. Cevrilemiyorsa (bos, harf, bozuk format) null doner.
        val litre: Double? get() = litreYazi.replace(',', '.').toDoubleOrNull()
        val tutar: Double? get() = tutarYazi.replace(',', '.').toDoubleOrNull()

        // null -> 0.0 sayilir, yani bos da gecersiz, harf de gecersiz, 0 da gecersiz.
        private val litreGecersiz: Boolean get() = (litre ?: 0.0) <= 0.0
        private val tutarGecersiz: Boolean get() = (tutar ?: 0.0) <= 0.0

        // Form kaydedilebilir mi? girdiye bakar
        override val gecerli: Boolean get() = !litreGecersiz && !tutarGecersiz

        override fun notDegistir(yeni: String): Yakit = copy(not = yeni)

        override fun dogrula(): Yakit = copy(
            litreHatali = litreGecersiz,
            tutarHatali = tutarGecersiz
        )
    }
}

// Kategori degisince o kategorinin bos formu kurulur (form sifirlanir karari).
// Yeni kategori eklendiginde derleyici bu when'i de gosterir.
fun bosForm(kategori: Kategori): KayitFormu = when (kategori) {
    Kategori.YAKIT -> KayitFormu.Yakit()
}

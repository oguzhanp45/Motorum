package com.oguzhanp.motorum.ui.form

import com.oguzhanp.motorum.model.Kategori
import com.oguzhanp.motorum.model.Mola


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

    data class RoadTrip(
        val baslangic: TripNoktasiFormu = TripNoktasiFormu(),
        val bitis: TripNoktasiFormu = TripNoktasiFormu(),
        val molalar: List<Mola> = emptyList(),
        val masrafYazi: String = "",
        override val not: String = ""
    ) : KayitFormu {

        override val kategori get() = Kategori.ROAD_TRIP

        // Masraf istege bagli: bos birakilirsa 0 sayilir, hata verilmez.
        val masraf: Double get() = masrafYazi.replace(',', '.').toDoubleOrNull() ?: 0.0

        // Mola istege bagli, dogrulamasi yok. Adi bos kalan satir kayda gitmiyor.
        // Kural burada duruyor ki ekleme ve detay ekrani ayni sekilde davransin.
        val doluMolalar: List<Mola> get() = molalar.filter { it.isim.isNotBlank() }

        // Bitis bolumu tamamen bossa yolculuk devam ediyor demektir; zorunlu degil.
        // Bir alani bile doldurulmussa uc alanin hepsi zorunlu olur.
        val bitisVar: Boolean get() = !bitis.bos

        private val bitisKmGecersiz: Boolean
            get() = bitisVar && (bitis.km ?: 0) <= (baslangic.km ?: 0)

        override val gecerli: Boolean
            get() = baslangic.gecerli && (!bitisVar || (bitis.gecerli && !bitisKmGecersiz))

        override fun notDegistir(yeni: String): RoadTrip = copy(not = yeni)

        override fun dogrula(): RoadTrip = copy(
            baslangic = baslangic.dogrula(),
            bitis = if (bitisVar) {
                // Once ucun kendi kurallari isaretleniyor, sonra sadece bitise ozel
                // km kurali ekleniyor: bos olmasi da yetmiyor, baslangictan buyuk olmali.
                val kontrol = bitis.dogrula()
                kontrol.copy(kmHatali = kontrol.kmHatali || bitisKmGecersiz)
            } else {
                bitis
            }
        )
    }
}

// Kategori degisince o kategorinin bos formu kurulur (form sifirlanir karari).
// Yeni kategori eklendiginde derleyici bu when'i de gosterir.
fun bosForm(kategori: Kategori): KayitFormu = when (kategori) {
    Kategori.YAKIT -> KayitFormu.Yakit()
    Kategori.ROAD_TRIP -> KayitFormu.RoadTrip()
}

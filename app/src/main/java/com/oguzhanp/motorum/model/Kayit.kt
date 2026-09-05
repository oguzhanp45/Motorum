package com.oguzhanp.motorum.model


import java.util.UUID


// Kategoriler artik ayri tipler. tutar arayuzde cunku her kategorinin parasal
// karsiligi var; litre sadece Yakit'te cunku baska kategoride yok.
// Yeni kategori eklenince derleyici eksik when dallarini gosterir.
sealed interface Kayit {
    val id: String                //id — LazyColumn'a satır kimliği vermek için.
    val kategori: Kategori
    val tarihMillis: Long         //string olmamasının nedeni sıralama hata olmaması
    //string sıralamasında "02.01.2025" > "01.09.2026" çıkar.
    val tutar: Double
    val not: String

    data class Yakit(
        override val id: String = UUID.randomUUID().toString(), //rastgele id üreticisi
        override val tarihMillis: Long,
        override val tutar: Double,   //o dolumda ödenen toplam TL
        override val not: String = "",
        val litre: Double
    ) : Kayit {
        // Kategori tipten turuyor, ayri alan olarak tasinmiyor.
        override val kategori get() = Kategori.YAKIT
    }
}

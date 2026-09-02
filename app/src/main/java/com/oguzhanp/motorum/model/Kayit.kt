package com.oguzhanp.motorum.model


import java.util.UUID


//data class
data class Kayit(
    val id: String = UUID.randomUUID().toString(), //id — LazyColumn'a satır kimliği vermek için.
    //rastgele id üreticisi

    val kategori: Kategori,

    val tarihMillis: Long, //string olmamasının nedeni sıralama hata olmaması
    //string sıralamasında "02.01.2025" > "01.09.2026" çıkar.

    val litre: Double, //litre ve tutar= o dolumda ödenen toplam TL
    val tutar: Double,
    val not: String = ""
)


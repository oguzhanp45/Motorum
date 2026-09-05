package com.oguzhanp.motorum.model

// Yolculugun bir ucu. Baslangic ve bitis ayni uc bilgiyi tasidigi icin tek sinif,
// iki kez kullaniliyor. Alanlar zorunlu oldugu icin yarim bir uc uretilemez.
data class TripNoktasi(
    val tarihMillis: Long,   // gun + saat birlikte
    val km: Int,
    val sehir: String
)

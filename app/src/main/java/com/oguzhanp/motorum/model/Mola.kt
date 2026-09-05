package com.oguzhanp.motorum.model

// Mola sadece ayni gunun icinde bir saat: tarih tasimiyor, yolculugun tarihine bagli.
// Saat ve dakika ayri cunku secilmemis olabilir ve SaatSecici bu iki degeri donduruyor.
data class Mola(
    val isim: String = "",
    val saat: Int? = null,
    val dakika: Int? = null
)

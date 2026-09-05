package com.oguzhanp.motorum.ui.form

// Yolculugun bir ucunun form hali. Gun, saat ve dakika ayri tutuluyor cunku
// iki secici iki ayri sey donduruyor ve "secilmedi" durumu null ile ifade ediliyor.
// Modeldeki tek Long'a ancak kaydederken birlestiriliyor.
data class TripNoktasiFormu(
    val tarihMillis: Long? = null,
    val saat: Int? = null,
    val dakika: Int? = null,
    val kmYazi: String = "",
    val sehir: String = "",
    val tarihHatali: Boolean = false,
    val saatHatali: Boolean = false,
    val kmHatali: Boolean = false,
    val sehirHatali: Boolean = false
) {

    val km: Int? get() = kmYazi.trim().toIntOrNull()

    val bos: Boolean
        get() = tarihMillis == null && saat == null && kmYazi.isBlank() && sehir.isBlank()

    val gecerli: Boolean
        get() = tarihMillis != null && saat != null && dakika != null &&
                (km ?: 0) > 0 && sehir.isNotBlank()

    fun dogrula(): TripNoktasiFormu = copy(
        tarihHatali = tarihMillis == null,
        saatHatali = saat == null || dakika == null,
        kmHatali = (km ?: 0) <= 0,
        sehirHatali = sehir.isBlank()
    )
}

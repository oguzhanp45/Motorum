package com.oguzhanp.motorum.ui.motorlarim

import com.oguzhanp.motorum.model.Motor

data class MotorlarimUiState(
    val motorlar: List<Motor> = emptyList(),
    val seciliMotorId: String? = null,
    val yukleniyor: Boolean = false,
    val hata: String? = null,
    // Silme onayi acikken hangi motor ve altinda kac kayit var.
    // Sayiyi diyalogu acarken cekiyoruz: uyari gercek rakami soylesin.
    val silinecekMotor: Motor? = null,
    val silinecekKayitSayisi: Int = 0,
    // Tek seferlik olay: secim GERCEKTEN kaydedildikten sonra true oluyor,
    // ekran bunu gorup ana sayfaya geciyor ve hemen kapatiyor.
    val secimTamam: Boolean = false
)

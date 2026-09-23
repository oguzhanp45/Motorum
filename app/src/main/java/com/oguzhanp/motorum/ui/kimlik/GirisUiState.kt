package com.oguzhanp.motorum.ui.kimlik

data class GirisUiState(
    val form: KimlikFormu = KimlikFormu(),
    val yukleniyor: Boolean = false,
    val hata: String? = null,
    // Hata olmayan geri bildirim: "sifre yenileme baglantisi gonderildi".
    val bilgi: String? = null,
    val basarili: Boolean = false
)

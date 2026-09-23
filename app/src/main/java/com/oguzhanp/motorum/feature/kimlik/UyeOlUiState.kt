package com.oguzhanp.motorum.feature.kimlik

data class UyeOlUiState(
    val form: KimlikFormu = KimlikFormu(),
    val yukleniyor: Boolean = false,
    val hata: String? = null,
    val basarili: Boolean = false
)

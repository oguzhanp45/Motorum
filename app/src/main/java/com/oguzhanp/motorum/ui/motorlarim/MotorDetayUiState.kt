package com.oguzhanp.motorum.ui.motorlarim

data class MotorDetayUiState(
    val form: MotorFormu = MotorFormu(),
    val yeniMi: Boolean = true,
    val calisiyor: Boolean = false,
    val hata: String? = null,
    val bitti: Boolean = false
)

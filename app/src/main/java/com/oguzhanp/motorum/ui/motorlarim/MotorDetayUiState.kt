package com.oguzhanp.motorum.ui.motorlarim

import androidx.compose.ui.graphics.ImageBitmap

data class MotorDetayUiState(
    val form: MotorFormu = MotorFormu(),
    val yeniMi: Boolean = true,
    val calisiyor: Boolean = false,
    val hata: String? = null,
    val bitti: Boolean = false,
    // Cozulmus hali tutuluyor, base64 metin degil: 250 KB'lik metni cozmek
    // ana is parcaciginda takilma yapar, o is ViewModel'de arka planda olur.
    val fotograf: ImageBitmap? = null,
    val fotografCalisiyor: Boolean = false
)

package com.oguzhanp.motorum.ui.motorlarim

import com.oguzhanp.motorum.model.Motor

data class MotorSeciciUiState(
    val motorlar: List<Motor> = emptyList(),
    val seciliMotor: Motor? = null,
    val panelAcik: Boolean = false,
    // Motorlarim'daki ile ayni kalip: secim GERCEKTEN kaydedildikten sonra
    // kalkiyor, ana sayfa bunu gorup kayitlari yeniden cekiyor.
    val secimTamam: Boolean = false
)

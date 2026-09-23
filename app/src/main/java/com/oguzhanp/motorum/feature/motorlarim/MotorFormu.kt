package com.oguzhanp.motorum.feature.motorlarim

import com.oguzhanp.motorum.model.Motor

// Kayit formlarindaki iki asamali dogrulama kalibi:
// gecerli   -> "kaydedilebilir mi?" sorusuna cevap, ekrani degistirmez.
// dogrula() -> hatali alanlari isaretlenmis YENI bir form dondurur.
data class MotorFormu(
    val marka: String = "",
    val model: String = "",
    val plaka: String = "",
    val markaHatali: Boolean = false,
    val modelHatali: Boolean = false
) {
    // Plaka zorunlu degil: bazi kullanicilar icin gizlilik meselesi.
    val gecerli: Boolean get() = marka.isNotBlank() && model.isNotBlank()

    fun dogrula() = copy(markaHatali = marka.isBlank(), modelHatali = model.isBlank())
}

fun Motor.formaCevir() = MotorFormu(marka = marka, model = model, plaka = plaka)

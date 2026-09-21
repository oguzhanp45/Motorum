package com.oguzhanp.motorum.core.constants

import androidx.compose.animation.core.CubicBezierEasing

// Uygulamadaki butun gecisler tek bir egriyi paylasiyor: hizli baslayip
// yavaslayarak duruyor. Her animasyon kendi egrisini secseydi ekranlar ayri
// ayri dogru ama birlikte huzursuz gorunurdu.
object AppMotion {
    val egri = CubicBezierEasing(0.2f, 0.9f, 0.25f, 1f)

    // Dokunma geri bildirimi: basma kisa, birakma biraz uzun. Birakmanin
    // uzun olmasi hareketin "yankisi" gibi duruyor.
    const val BASMA = 110
    const val BIRAKMA = 220

    // Menu ve panel aciliskapanislari.
    const val PANEL = 180

    // Sayfa ve bolum degisimleri; ozet kartinin tek satira inmesi de bu.
    const val SAYFA = 320
}

package com.oguzhanp.motorum.feature.motorlarim

import com.oguzhanp.motorum.model.Motor

// Ust bardaki cipin dort hali. Eskiden tek soru vardi ("motor var mi") ve
// internet yokken de "Motor yok" yaziyordu, yani kullaniciya yalan
// soyluyordu. Cizen yer: ui/motorlarim/MotorSecici.kt icindeki MotorCipi.
sealed interface CipDurumu {
    data object Yukleniyor : CipDurumu
    data class Secili(val motor: Motor) : CipDurumu
    data object MotorYok : CipDurumu
    data object BaglantiYok : CipDurumu
}

data class MotorSeciciUiState(
    val motorlar: List<Motor> = emptyList(),
    val seciliMotor: Motor? = null,
    val panelAcik: Boolean = false,
    // Motorlarim'daki ile ayni kalip: secim GERCEKTEN kaydedildikten sonra
    // kalkiyor, ana sayfa bunu gorup kayitlari yeniden cekiyor.
    val secimTamam: Boolean = false,
    val yukleniyor: Boolean = true,
    val hata: String? = null
) {

    // Hal alanlardan turuyor, ayrica saklanmiyor. Sira onemli: hata varken
    // elimizdeki bos liste "motor yok" degil, "bilmiyoruz" demek.
    val cipDurumu: CipDurumu
        get() = when {
            yukleniyor -> CipDurumu.Yukleniyor
            hata != null -> CipDurumu.BaglantiYok
            seciliMotor != null -> CipDurumu.Secili(seciliMotor)
            else -> CipDurumu.MotorYok
        }
}

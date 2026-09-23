package com.oguzhanp.motorum.feature.hava

import com.oguzhanp.motorum.model.HavaDurumu

// Hava kartinin gosterebilecegi dort hal. Boolean'larla yazsaydik "hem
// yukleniyor hem hata" gibi imkansiz birlesimler mumkun olurdu.
// Hangi hal oldugunu HavaDurumuViewModel belirliyor, HavaDurumuKarti ciziyor.
sealed interface HavaDurumuHali {

    // izinIstendi: kullanici butona basti ama izin gelmedi. Kart o zaman
    // "ayarlardan da verebilirsin" satirini ekliyor. Acilista false.
    data class IzinYok(val izinIstendi: Boolean = false) : HavaDurumuHali

    data object Yukleniyor : HavaDurumuHali

    data class Hazir(val hava: HavaDurumu) : HavaDurumuHali

    // Konum hatasi ve ag hatasi ayni hale dusuyor: kullanicinin
    // yapabilecegi sey ikisinde de ayni, tekrar denemek.
    data object Alinamadi : HavaDurumuHali
}

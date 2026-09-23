package com.oguzhanp.motorum.data

import com.google.firebase.firestore.DocumentId
import com.oguzhanp.motorum.model.Belge
import com.oguzhanp.motorum.model.BelgeTuru

// Belgenin Firestore'daki hali. Gorevi KayitBelgesi ve MotorBelgesi ile ayni;
// adi "BelgeBelgesi" olsaydi okurken kafa karistirirdi.
// Tur String: taninmayan bir deger gelirse cevirici null donup o belgeyi eliyor.
data class BelgeVerisi(
    @DocumentId val id: String = "",
    val tur: String = "",
    val ad: String = "",
    val bitisMillis: Long = 0L,
    val yenileAy: Int? = null,
    val kacGunOnce: Int = 15
)

fun Belge.veriyeCevir(): BelgeVerisi = BelgeVerisi(
    tur = tur.name,
    ad = ad,
    bitisMillis = bitisMillis,
    yenileAy = yenileAy,
    kacGunOnce = kacGunOnce
)

fun BelgeVerisi.belgeyeCevir(): Belge? {
    val belgeTuru = BelgeTuru.entries.firstOrNull { it.name == tur } ?: return null
    return Belge(
        id = id,
        tur = belgeTuru,
        ad = ad,
        bitisMillis = bitisMillis,
        yenileAy = yenileAy,
        kacGunOnce = kacGunOnce
    )
}

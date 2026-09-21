package com.oguzhanp.motorum.core.constants

import androidx.compose.ui.unit.dp

// Bosluk olcegi. Ara deger yazmak yerine buradan seciliyor: ekranlar arasinda
// 12 ile 14 dp farki gozle secilmez ama yan yana konunca duzen dagilir.
// Dortlu adimlarla ilerliyor, tek istisna kartIci.
object AppSpacing {
    // Ikon ile metin arasi, cip ici gibi en dar yerler.
    val cokKucuk = 4.dp

    // Bir basligin altindaki metin, satir ici ayrimlar.
    val kucuk = 8.dp

    // Ayni kartin icindeki satirlar arasi.
    val orta = 12.dp

    // Ekran kenar boslugu ve kartlar arasi mesafe.
    val normal = 16.dp

    // Kartin kendi ic dolgusu. 16 degil 18: kart kosesi yuvarlak oldugu icin
    // icerik kenara 16'da gozle daha yakin duruyor.
    val kartIci = 18.dp

    // Bolumler arasi nefes payi, bos durumlarin ust-alt boslugu.
    val genis = 24.dp
}

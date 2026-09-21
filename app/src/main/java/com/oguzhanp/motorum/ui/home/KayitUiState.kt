package com.oguzhanp.motorum.ui.home

import com.oguzhanp.motorum.model.Kayit

// Ana sayfanin ihtiyac duydugu her sey tek nesnede.
// Toplamlar burada ayri alan olarak duruyor: ekran her cizildiginde
// yeniden hesaplanmasin, liste her cekildiginde bir kez hesaplanip yazilsin.
data class KayitUiState(
    val kayitlar: List<Kayit> = emptyList(),
    val toplamTutar: Double = 0.0,
    val toplamLitre: Double = 0.0,
    // Sayac okumalarindan turuyor: en yuksek okuma eksi en dusuk.
    // Road Trip mesafelerinin toplami degil, kayit tutulan her yerden geliyor.
    val gidilenYol: Int = 0,
    val yukleniyor: Boolean = false,
    // Ilk yukleme ile asagi cekerek yenileme ayri: ilki listeyi daireyle
    // degistiriyor, ikincisi listeyi yerinde birakip ustte gosterge cikariyor.
    val yenileniyor: Boolean = false,
    val hata: String? = null,
    val motorYok: Boolean = false,
    // Kaydirarak silinen kayit. null degilse ekran "Geri Al" teklif ediyor
    // demektir. Hem bayrak hem tasinan veri: ikisini ayirmak gereksizdi.
    val geriAlinabilir: Kayit? = null,
    // Hatirlatma paneli acik olan kayit. Cipten, detay seridinden ya da
    // bildirimden aciliyor; hepsi ayni paneli gosteriyor.
    val panelKayitId: String? = null
)

package com.oguzhanp.motorum.ui.home

import com.oguzhanp.motorum.model.Kayit

// Ana sayfanin ihtiyac duydugu her sey tek nesnede.
// Toplamlar burada ayri alan olarak duruyor: ekran her cizildiginde
// yeniden hesaplanmasin, degistiren fonksiyon bir kez hesaplayip yazsin.
data class KayitUiState(
    val kayitlar: List<Kayit> = emptyList(),
    val toplamTutar: Double = 0.0,
    val toplamLitre: Double = 0.0,
    val toplamKm: Int = 0
)

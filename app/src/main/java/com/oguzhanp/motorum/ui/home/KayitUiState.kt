package com.oguzhanp.motorum.ui.home

import com.oguzhanp.motorum.model.Kayit

// Ana sayfanin ihtiyac duydugu her sey tek nesnede.
// isLoading / error simdilik kullanilmiyor, ileride veri kaynagi geldiginde devreye girecek.
data class KayitUiState(
    val kayitlar: List<Kayit> = emptyList(),
    val isLoading: Boolean = false,
    val error: String = "",
    val toplamTutar: Double = 0.0,
    val toplamLitre: Double = 0.0
)

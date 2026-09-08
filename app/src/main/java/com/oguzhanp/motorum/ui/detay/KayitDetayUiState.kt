package com.oguzhanp.motorum.ui.detay

import com.oguzhanp.motorum.ui.form.KayitFormu

data class KayitDetayUiState(
    val form: KayitFormu = KayitFormu.Yakit(),
    val silmeOnayiGoster: Boolean = false, // Diyalog acik mi?
    val calisiyor: Boolean = false,
    val hata: String? = null,
    val bitti: Boolean = false
)

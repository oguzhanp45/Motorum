package com.oguzhanp.motorum.feature.kayit.detay

import com.oguzhanp.motorum.feature.kayit.form.KayitFormu

data class KayitDetayUiState(
    val form: KayitFormu = KayitFormu.Yakit(),
    val silmeOnayiGoster: Boolean = false, // Diyalog acik mi?
    val calisiyor: Boolean = false,
    val hata: String? = null,
    val bitti: Boolean = false
)

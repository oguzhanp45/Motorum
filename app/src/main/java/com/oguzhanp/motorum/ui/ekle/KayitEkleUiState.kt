package com.oguzhanp.motorum.ui.ekle

import com.oguzhanp.motorum.model.Kategori

data class KayitEkleUiState(
    val kategori: Kategori = Kategori.YAKIT,
    val tarihMillis: Long = System.currentTimeMillis(),
    val litreYazi: String = "",
    val tutarYazi: String = "",
    val not: String = "",
    val litreHatali: Boolean = false,
    val tutarHatali: Boolean = false
) {

    // Metni sayiya cevirir. Cevrilemiyorsa (bos, harf, bozuk format) null doner.
    val litre: Double? get() = litreYazi.replace(',', '.').toDoubleOrNull()
    val tutar: Double? get() = tutarYazi.replace(',', '.').toDoubleOrNull()

    // null -> 0.0 sayilir, yani bos da gecersiz, harf de gecersiz, 0 da gecersiz.
    private val litreGecersiz: Boolean get() = (litre ?: 0.0) <= 0.0
    private val tutarGecersiz: Boolean get() = (tutar ?: 0.0) <= 0.0

    val gecerli: Boolean get() = !litreGecersiz && !tutarGecersiz

    fun dogrula(): KayitEkleUiState = copy(
        litreHatali = litreGecersiz,
        tutarHatali = tutarGecersiz
    )
}

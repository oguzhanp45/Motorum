package com.oguzhanp.motorum.feature.kimlik

private val EPOSTA_KALIBI = Regex("^[^@\\s]+@[^@\\s]+\\.[^@\\s]+$")

const val EN_AZ_SIFRE = 6

data class KimlikFormu(
    val eposta: String = "",
    val sifre: String = "",
    val sifreGorunur: Boolean = false,
    val epostaHatali: Boolean = false,
    val sifreHatali: Boolean = false
) {

    private val epostaGecersiz: Boolean get() = !EPOSTA_KALIBI.matches(eposta.trim())
    private val sifreGecersiz: Boolean get() = sifre.length < EN_AZ_SIFRE

    val gecerli: Boolean get() = !epostaGecersiz && !sifreGecersiz

    // Sifremi unuttum icin sadece e-postanin dogru olmasi yetiyor.
    val epostaGecerli: Boolean get() = !epostaGecersiz

    fun dogrula(): KimlikFormu = copy(
        epostaHatali = epostaGecersiz,
        sifreHatali = sifreGecersiz
    )
}

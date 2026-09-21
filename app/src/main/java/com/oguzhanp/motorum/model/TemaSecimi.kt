package com.oguzhanp.motorum.model

// Kullanicinin tema tercihi. SISTEM telefonun ayarini izliyor; digerleri
// telefon ne derse desin sabit kaliyor.
enum class TemaSecimi(val etiket: String) {
    ACIK("Açık"),
    KARANLIK("Karanlık"),
    SISTEM("Sistem")
}

package com.oguzhanp.motorum.model

// Kullanicinin dil tercihi. SISTEM telefonun dilini izliyor; digerleri
// telefon ne derse desin sabit kaliyor. Tema secimiyle ayni mantik, tek
// farki: bunu biz saklamiyoruz, Android'in "uygulama dili" ozelligi sakliyor.
//
// kod: Android'in bekledigi dil etiketi. SISTEM'de yok, cunku "tercih yok"
// demek bos liste vermek demek.
enum class DilSecimi(val kod: String?) {
    SISTEM(null),
    TURKCE("tr"),
    INGILIZCE("en")
}

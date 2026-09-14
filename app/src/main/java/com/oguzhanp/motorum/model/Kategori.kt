package com.oguzhanp.motorum.model

// Liste sabit olduğu için enum: yeni bir kategori eklendiğinde derleyici
// bütün when bloklarını eksik dal olarak gösteriyor.
enum class Kategori(val etiket: String) { //etiket ekranda görünen metin.
    YAKIT("Yakıt"),
    ROAD_TRIP("Road Trip"),
    BAKIM("Bakım"),
    AKSESUAR("Aksesuar")
}

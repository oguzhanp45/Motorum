package com.oguzhanp.motorum.feature.kayit.ekle

import com.oguzhanp.motorum.core.util.formatLitre
import com.oguzhanp.motorum.core.util.formatTl
import com.oguzhanp.motorum.core.util.sayiyiYaziya
import com.oguzhanp.motorum.feature.kayit.form.KayitFormu
import com.oguzhanp.motorum.feature.kayit.form.TripNoktasiFormu
import com.oguzhanp.motorum.model.Kayit

// "Son kayittan doldur" ve Kayit Detayi'ndaki "tekrarla" icin: bir kaydin
// degerleriyle dolu yeni bir form. Tarih hep bugun (formun varsayilani).
// Bilerek kopyalanmayanlar: km (sayac ilerledi, eskisini yazmak uyari
// dogururdu), hatirlatma (yeni kayda ozgu), yolculugun bitisi (yeni yolculuk
// henuz bitmedi).
fun kayittanForm(kayit: Kayit): KayitFormu = when (kayit) {
    is Kayit.Yakit -> KayitFormu.Yakit(
        litreYazi = sayiyiYaziya(kayit.litre),
        tutarYazi = sayiyiYaziya(kayit.tutar),
        not = kayit.not
    )
    is Kayit.RoadTrip -> KayitFormu.RoadTrip(
        baslangic = TripNoktasiFormu(sehir = kayit.baslangic.sehir),
        not = kayit.not
    )
    is Kayit.Bakim -> KayitFormu.Bakim(
        bakimTuru = kayit.bakimTuru,
        tutarYazi = sayiyiYaziya(kayit.tutar),
        not = kayit.not
    )
    is Kayit.Aksesuar -> KayitFormu.Aksesuar(
        aksesuarAdi = kayit.aksesuarAdi,
        tutarYazi = sayiyiYaziya(kayit.tutar),
        not = kayit.not
    )
}

// Seritteki tek satirlik ozet: "Shell · 12,40 L · 580,00 ₺".
fun kayitOzeti(kayit: Kayit): String = when (kayit) {
    is Kayit.Yakit -> listOfNotNull(kayit.not.ifBlank { null }, formatLitre(kayit.litre), formatTl(kayit.tutar))
    is Kayit.RoadTrip -> listOf("${kayit.baslangic.sehir} → ${kayit.bitis?.sehir ?: "…"}")
    is Kayit.Bakim -> listOf(kayit.bakimTuru, formatTl(kayit.tutar))
    is Kayit.Aksesuar -> listOf(kayit.aksesuarAdi, formatTl(kayit.tutar))
}.joinToString(" · ")

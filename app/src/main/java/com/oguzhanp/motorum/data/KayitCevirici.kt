package com.oguzhanp.motorum.data

import com.oguzhanp.motorum.model.Kategori
import com.oguzhanp.motorum.model.Kayit
import com.oguzhanp.motorum.model.Mola
import com.oguzhanp.motorum.model.TripNoktasi

// Model -> veritabani. Uzanti fonksiyonu cunku model/ katmani data/ katmanini
// tanimamali: bagimlilik tek yone baksin diye cevirme isi burada duruyor.
// when (this) sealed uzerinde: besinci kategori eklenirse derleyici burayi da gosterir.
fun Kayit.belgeyeCevir(): KayitBelgesi = when (this) {
    is Kayit.Yakit -> KayitBelgesi(
        kategori = kategori.name,
        tarihMillis = tarihMillis,
        tutar = tutar,
        not = not,
        litre = litre,
        km = km
    )

    is Kayit.RoadTrip -> KayitBelgesi(
        kategori = kategori.name,
        tarihMillis = tarihMillis,
        tutar = tutar,
        not = not,
        baslangic = baslangic.belgeyeCevir(),
        bitis = bitis?.belgeyeCevir(),
        molalar = molalar.map { it.belgeyeCevir() }
    )

    is Kayit.Bakim -> KayitBelgesi(
        kategori = kategori.name,
        tarihMillis = tarihMillis,
        tutar = tutar,
        not = not,
        bakimTuru = bakimTuru,
        hatirlatmaMillis = hatirlatmaMillis,
        hatirlatmaYapildiMillis = hatirlatmaYapildiMillis
    )

    is Kayit.Aksesuar -> KayitBelgesi(
        kategori = kategori.name,
        tarihMillis = tarihMillis,
        tutar = tutar,
        not = not,
        aksesuarAdi = aksesuarAdi
    )
}

// Veritabani -> model. Yazarken tip kesin, okurken degil: Console'dan bozulmus,
// eski surumden kalma ya da kategorisi taninmayan bir belge gelebilir.
// Boyle durumlarda null donuyoruz, depo mapNotNull ile eliyor.
// Ifade govdesi degil blok govde: icinde return kullaniliyor, ifade govdeli
// fonksiyonda return yazilamaz.
fun KayitBelgesi.kayidaCevir(): Kayit? {
    return when (kategori) {
        Kategori.YAKIT.name -> Kayit.Yakit(
            id = id,
            tarihMillis = tarihMillis,
            tutar = tutar,
            not = not,
            // return bir ifade olarak kullanilabiliyor: alan eksikse belgeyi hic uretme.
            litre = litre ?: return null,
            // Km istege bagli: yoksa null kaliyor, belge bozuk sayilmiyor.
            km = km
        )

        // tarihMillis verilmiyor: modelde baslangic'tan turuyor. DTO'daki kopya
        // sadece yazma yonunde var.
        Kategori.ROAD_TRIP.name -> Kayit.RoadTrip(
            id = id,
            tutar = tutar,
            not = not,
            baslangic = baslangic?.noktayaCevir() ?: return null,
            bitis = bitis?.noktayaCevir(),
            molalar = molalar.map { it.molayaCevir() }
        )

        Kategori.BAKIM.name -> Kayit.Bakim(
            id = id,
            tarihMillis = tarihMillis,
            tutar = tutar,
            not = not,
            bakimTuru = bakimTuru ?: return null,
            // Hatirlatma istege bagli: yoksa null kaliyor, belge bozuk sayilmiyor.
            hatirlatmaMillis = hatirlatmaMillis,
            hatirlatmaYapildiMillis = hatirlatmaYapildiMillis
        )

        Kategori.AKSESUAR.name -> Kayit.Aksesuar(
            id = id,
            tarihMillis = tarihMillis,
            tutar = tutar,
            not = not,
            aksesuarAdi = aksesuarAdi ?: return null
        )

        else -> null
    }
}

// Ic tipler icin ayni koprunun kucuk hali. private: disaridan kimsenin
// TripNoktasi'ni tek basina cevirmesine gerek yok.
private fun TripNoktasi.belgeyeCevir() = TripNoktasiBelgesi(tarihMillis, km, sehir)

private fun TripNoktasiBelgesi.noktayaCevir() = TripNoktasi(tarihMillis, km, sehir)

private fun Mola.belgeyeCevir() = MolaBelgesi(isim, saat, dakika)

private fun MolaBelgesi.molayaCevir() = Mola(isim, saat, dakika)

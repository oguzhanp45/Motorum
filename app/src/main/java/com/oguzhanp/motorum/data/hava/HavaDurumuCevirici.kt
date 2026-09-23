package com.oguzhanp.motorum.data.hava

import com.oguzhanp.motorum.model.HavaDurumu

// Servisin sekli -> uygulamanin sekli.
// Liste bos gelirse durum kodu yok, yani surus karari verilemez.
// Uydurmak yerine null donuyoruz; depo bunu hata sayiyor.
fun HavaDurumuYaniti.havayaCevir(): HavaDurumu? {
    val bilgi = havaListesi.firstOrNull() ?: return null
    return HavaDurumu(
        sehir = sehir,
        sicaklik = olcumler.sicaklik,
        aciklama = bilgi.aciklama,
        kod = bilgi.kod,
        ruzgarHizi = ruzgar.hiz,
        gorusMesafesi = gorusMesafesi
    )
}

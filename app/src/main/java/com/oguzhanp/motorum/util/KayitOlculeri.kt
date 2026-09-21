package com.oguzhanp.motorum.util

import com.oguzhanp.motorum.model.Kayit

// Kilometre artik tek bir sey: sayacin o andaki degeri. Hangi kayittan geldigi
// onemli degil, hepsi ayni sayac cizgisi uzerinde birer okuma.
// Bakim ve Aksesuar'da km sorulmuyor, o yuzden bos liste donuyorlar.
fun kmOkumalari(kayitlar: List<Kayit>): List<Int> = kayitlar.flatMap { kayit ->
    when (kayit) {
        is Kayit.Yakit -> listOfNotNull(kayit.km)
        is Kayit.RoadTrip -> listOfNotNull(kayit.baslangic.km, kayit.bitis?.km)
        is Kayit.Bakim, is Kayit.Aksesuar -> emptyList()
    }
}

// Ayni okumalar, ne zaman alindiklariyla birlikte: (tarih, km). Donemsel
// hesap icin gerekli; "bu ay" demek icin her okumanin hangi gune ait oldugunu
// bilmek lazim. Road Trip'in iki ucu kendi tarihlerini tasiyor: yolculuk
// ayin son gunu baslayip ertesi ay bittiyse bitis okumasi yeni aya yaziliyor.
fun tarihliOkumalar(kayitlar: List<Kayit>): List<Pair<Long, Int>> = kayitlar.flatMap { kayit ->
    when (kayit) {
        is Kayit.Yakit -> listOfNotNull(kayit.km?.let { kayit.tarihMillis to it })
        is Kayit.RoadTrip -> listOfNotNull(
            kayit.baslangic.tarihMillis to kayit.baslangic.km,
            kayit.bitis?.let { it.tarihMillis to it.km }
        )
        is Kayit.Bakim, is Kayit.Aksesuar -> emptyList()
    }
}

// Takip basladigindan beri gidilen yol: en yuksek okuma ile en dusugun farki.
// Motorun omur boyu kilometresi degil, uygulamaya kayit girmeye basladiktan
// sonrasi. Tek okuma varsa mesafe olusmuyor: 0.
fun gidilenYolHesapla(kayitlar: List<Kayit>): Int {
    val okumalar = kmOkumalari(kayitlar)
    if (okumalar.size < 2) return 0
    return okumalar.max() - okumalar.min()
}

// Formdaki "sayac geriye gidiyor" uyarisi icin: bugune kadar girilmis en yuksek
// okuma. Uyari amacli, engel degil: kullanici gecmise ait bir kayit giriyor
// olabilir ve o zaman kucuk bir deger normaldir.
fun sonKmOkumasi(kayitlar: List<Kayit>): Int? = kmOkumalari(kayitlar).maxOrNull()

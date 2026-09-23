package com.oguzhanp.motorum.data.kayit

import com.google.firebase.firestore.DocumentId

// Firestore'un okuyabilecegi duz kayit sekli. model/Kayit.kt sealed bir arayuz,
// Firestore onu kuramaz: belgeyi okurken elinde sadece bir Map var, hangi tip
// oldugunu bilemez. Bu yuzden veritabaninin sekli burada, uygulamanin sekli
// model/ icinde ayri duruyor.

// Tum alanlarin varsayilani var: Firestore nesneyi bos yapiciyla kurup alanlari
// sonra dolduruyor. TripNoktasi'nda bilerek varsayilan yok, o kisit orada kalsin
// diye buraya gevsek bir kopyasi yazildi.
data class MolaBelgesi(
    val isim: String = "",
    val saat: Int? = null,
    val dakika: Int? = null
)

data class TripNoktasiBelgesi(
    val tarihMillis: Long = 0L,
    val km: Int = 0,
    val sehir: String = ""
)

// Dort kayit tipinin birlesimi. Ortak alanlar her belgede dolu, tipe ozel alanlar
// null. Hangi tip oldugunu nullable alanlardan tahmin etmiyoruz: kategori alani
// soyluyor.
data class KayitBelgesi(
    // Belge adini okurken buraya yazar, yazarken bu alani atlar.
    @DocumentId val id: String = "",
    // Enum degil String: Firestore'dan taninmayan bir deger gelirse cevirici
    // null donup o belgeyi eliyor, uygulama cokmuyor. Ama koruma bu kadar:
    // yazarken kategori.name kullaniliyor, yani sabitin adini degistirmek eski
    // belgeleri yine okunamaz yapar. Cokmeye karsi koruyor, veri kaybina karsi degil.
    val kategori: String = "",
    // Road Trip'te baslangic.tarihMillis buraya da kopyalaniyor: siralamayi ileride
    // sunucuya tasimak istersek bu alan olmadan yapilamaz.
    val tarihMillis: Long = 0L,
    val tutar: Double = 0.0,
    val not: String = "",
    val litre: Double? = null,
    // Sadece yakit kayitlarinda, orada da istege bagli. Eski belgelerde bu alan
    // yok; varsayilan null oldugu icin onlar da sorunsuz okunuyor.
    val km: Int? = null,
    val bakimTuru: String? = null,
    val aksesuarAdi: String? = null,
    // Sadece bakim kayitlarinda dolu. Eski belgelerde bu alan yok, varsayilan
    // null oldugu icin onlar da sorunsuz okunuyor.
    val hatirlatmaMillis: Long? = null,
    // "Yaptirdim" ani. Eski belgelerde yok; null = yapilmadi.
    val hatirlatmaYapildiMillis: Long? = null,
    val baslangic: TripNoktasiBelgesi? = null,
    val bitis: TripNoktasiBelgesi? = null,
    val molalar: List<MolaBelgesi> = emptyList()
)

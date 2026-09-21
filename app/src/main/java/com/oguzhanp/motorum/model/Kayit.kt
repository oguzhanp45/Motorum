package com.oguzhanp.motorum.model


import java.util.UUID


// Kategoriler artik ayri tipler. tutar arayuzde cunku her kategorinin parasal
// karsiligi var; litre sadece Yakit'te cunku baska kategoride yok.
// Yeni kategori eklenince derleyici eksik when dallarini gosterir.
sealed interface Kayit {
    val id: String                //id — LazyColumn'a satır kimliği vermek için.
    val kategori: Kategori
    val tarihMillis: Long         //string olmamasının nedeni sıralama hata olmaması
    //string sıralamasında "02.01.2025" > "01.09.2026" çıkar.
    val tutar: Double
    val not: String

    data class Yakit(
        override val id: String = UUID.randomUUID().toString(), //rastgele id üreticisi
        override val tarihMillis: Long,
        override val tutar: Double,   //o dolumda ödenen toplam TL
        override val not: String = "",
        val litre: Double,
        // Dolum anindaki kilometre sayaci. Istege bagli: eski kayitlarda yok,
        // kullanici da her dolumda girmek zorunda degil. Sayac okumasi sayiliyor:
        // gidilen yolu ve km basi maliyeti besliyor.
        val km: Int? = null
    ) : Kayit {

        val birimFiyat: Double get() = tutar / litre
        // Kategori tipten turuyor, ayri alan olarak tasinmiyor.
        override val kategori get() = Kategori.YAKIT
    }

    data class RoadTrip(
        override val id: String = UUID.randomUUID().toString(),
        override val tutar: Double = 0.0,   // yol masrafi, girilmezse 0
        override val not: String = "",
        val baslangic: TripNoktasi,
        val bitis: TripNoktasi? = null,     // null = yolculuk devam ediyor
        // Mola iki ucun degil, yolculugun kendisinin olayi: baslangic ile bitis
        // arasinda gerceklesiyor, o yuzden burada duruyor.
        val molalar: List<Mola> = emptyList()
    ) : Kayit {
        override val kategori get() = Kategori.ROAD_TRIP
        // Tarih baslangictan turuyor, ayrica kopyalanmiyor.
        override val tarihMillis get() = baslangic.tarihMillis

        // Gidilen yol iki km degerinin farki; ayri alan olarak saklanmiyor ki
        // km duzeltilince mesafe eskimesin. Yolculuk bitmediyse henuz belli degil: 0.
        val mesafe: Int get() = bitis?.let { it.km - baslangic.km } ?: 0
    }

    // Bakim ve Aksesuar bugun ayni sekle sahip, yine de ayri tipler:
    // farkli kavramlar, ileride farkli alanlar kazanacaklar.
    data class Bakim(
        override val id: String = UUID.randomUUID().toString(),
        override val tarihMillis: Long,
        override val tutar: Double,
        override val not: String = "",
        val bakimTuru: String,
        // Istege bagli hatirlatma zamani. null = hatirlatma kurulmamis.
        // Sadece bakimda var: yakit alimi ya da aksesuar icin hatirlatmanin
        // anlami yok, o yuzden ortak arayuze konmadi.
        val hatirlatmaMillis: Long? = null,
        // Kullanici "Yaptirdim" dediginde o an. null = henuz yapilmadi.
        // Hatirlatmanin kendisi silinmiyor: ne zaman kuruldugu da gecmisin parcasi.
        val hatirlatmaYapildiMillis: Long? = null
    ) : Kayit {

        val hatirlatmaVar: Boolean get() = hatirlatmaMillis != null
        override val kategori get() = Kategori.BAKIM

        // Cipin ve panelin baktigi tek yer. null = hatirlatma kurulmamis.
        // simdi disaridan: fonksiyon saf kalsin, test edilebilsin.
        fun hatirlatmaDurumu(simdi: Long = System.currentTimeMillis()): HatirlatmaDurumu? {
            val zaman = hatirlatmaMillis ?: return null
            return when {
                hatirlatmaYapildiMillis != null -> HatirlatmaDurumu.YAPILDI
                zaman <= simdi -> HatirlatmaDurumu.ZAMANI_GELDI
                else -> HatirlatmaDurumu.ILERIDE
            }
        }
    }

    data class Aksesuar(
        override val id: String = UUID.randomUUID().toString(),
        override val tarihMillis: Long,
        override val tutar: Double,
        override val not: String = "",
        val aksesuarAdi: String
    ) : Kayit {
        override val kategori get() = Kategori.AKSESUAR
    }
}

// Bir bakim hatirlatmasinin uc hali: gri (bekliyor), kehribar (zamani geldi,
// yapilmadi), yesil (yapildi). "Zamani geldi" kullanici bir sey yapana kadar
// kaliyor: unutulan bakim gozden kacmasin.
enum class HatirlatmaDurumu { ILERIDE, ZAMANI_GELDI, YAPILDI }

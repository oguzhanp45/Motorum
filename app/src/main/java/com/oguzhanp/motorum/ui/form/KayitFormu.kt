package com.oguzhanp.motorum.ui.form

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import com.oguzhanp.motorum.R
import com.oguzhanp.motorum.model.Kategori
import com.oguzhanp.motorum.model.Mola
import com.oguzhanp.motorum.util.ayEkle
import com.oguzhanp.motorum.util.ayniGun
import com.oguzhanp.motorum.util.saatAl
import com.oguzhanp.motorum.util.tarihSaatBirlestir


 //Kayit formunun ortak verisi ve dogrulama kurallari.
 //Hem ekleme hem detay ekrani ayni formu gosterdigi icin burada duruyor.
 //Kural tek yerde: birini guncelleyip digerini unutma riski yok.
 //googleda 2 ilke var birisi her ekranın kendi uistate olacak ona uymaya çalıştım
 //diger ilkeye göre kontrol tek yerde olmalı o yüzden ayrı bir form clası açıp 2 yerde kullandım.
sealed interface KayitFormu {
    val kategori: Kategori
    val not: String
    // Iki asamali dogrulama:
    // gecerli   -> "kaydedilebilir mi?" sorusuna cevap, ekrani degistirmez.
    // dogrula() -> hatali alanlari isaretlenmis YENI bir form dondurur.
    // Kaydet'e basilinca once dogrula(), sonra gecerli kontrol ediliyor.
    val gecerli: Boolean
    fun dogrula(): KayitFormu

    // not her kategoride ortak ama copy() arayuzde yok, her data class'in kendine ait.
    // Bu fonksiyon sayesinde ekran formun tipini bilmeden notu degistirebiliyor.
    fun notDegistir(yeni: String): KayitFormu

    data class Yakit(
        val tarihMillis: Long = System.currentTimeMillis(),
        val litreYazi: String = "",
        val tutarYazi: String = "",
        val kmYazi: String = "",
        override val not: String = "",
        val litreHatali: Boolean = false,
        val tutarHatali: Boolean = false,
        val kmHatali: Boolean = false
    ) : KayitFormu {

        override val kategori get() = Kategori.YAKIT

        // Metni sayiya cevirir. Cevrilemiyorsa (bos, harf, bozuk format) null doner.
        val litre: Double? get() = litreYazi.replace(',', '.').toDoubleOrNull()
        val tutar: Double? get() = tutarYazi.replace(',', '.').toDoubleOrNull()

        // Km tam sayi: sayac ondalik gostermiyor. Bos birakilirsa null, yani
        // "girilmedi" demek; hata degil.
        val km: Int? get() = kmYazi.trim().toIntOrNull()

        // null -> 0.0 sayilir, yani bos da gecersiz, harf de gecersiz, 0 da gecersiz.
        private val litreGecersiz: Boolean get() = (litre ?: 0.0) <= 0.0
        private val tutarGecersiz: Boolean get() = (tutar ?: 0.0) <= 0.0

        // Km istege bagli oldugu icin bos olmasi sorun degil; ama bir sey
        // yazildiysa okunabilir ve sifirdan buyuk olmali.
        private val kmGecersiz: Boolean
            get() = kmYazi.isNotBlank() && (km ?: 0) <= 0

        // Form kaydedilebilir mi? girdiye bakar
        override val gecerli: Boolean get() = !litreGecersiz && !tutarGecersiz && !kmGecersiz

        override fun notDegistir(yeni: String): Yakit = copy(not = yeni)

        override fun dogrula(): Yakit = copy(
            litreHatali = litreGecersiz,
            tutarHatali = tutarGecersiz,
            kmHatali = kmGecersiz
        )
    }

    data class RoadTrip(
        val baslangic: TripNoktasiFormu = TripNoktasiFormu(),
        val bitis: TripNoktasiFormu = TripNoktasiFormu(),
        val molalar: List<Mola> = emptyList(),
        val masrafYazi: String = "",
        override val not: String = ""
    ) : KayitFormu {

        override val kategori get() = Kategori.ROAD_TRIP

        // Masraf istege bagli: bos birakilirsa 0 sayilir, hata verilmez.
        val masraf: Double get() = masrafYazi.replace(',', '.').toDoubleOrNull() ?: 0.0

        // Mola istege bagli, dogrulamasi yok. Adi bos kalan satir kayda gitmiyor.
        // Kural burada duruyor ki ekleme ve detay ekrani ayni sekilde davransin.
        val doluMolalar: List<Mola> get() = molalar.filter { it.isim.isNotBlank() }

        // Bitis bolumu tamamen bossa yolculuk devam ediyor demektir; zorunlu degil.
        // Bir alani bile doldurulmussa uc alanin hepsi zorunlu olur.
        val bitisVar: Boolean get() = !bitis.bos

        private val bitisKmGecersiz: Boolean
            get() = bitisVar && (bitis.km ?: 0) <= (baslangic.km ?: 0)

        override val gecerli: Boolean
            get() = baslangic.gecerli && (!bitisVar || (bitis.gecerli && !bitisKmGecersiz))

        override fun notDegistir(yeni: String): RoadTrip = copy(not = yeni)

        override fun dogrula(): RoadTrip = copy(
            baslangic = baslangic.dogrula(),
            bitis = if (bitisVar) {
                // Once ucun kendi kurallari isaretleniyor, sonra sadece bitise ozel
                // km kurali ekleniyor: bos olmasi da yetmiyor, baslangictan buyuk olmali.
                val kontrol = bitis.dogrula()
                kontrol.copy(kmHatali = kontrol.kmHatali || bitisKmGecersiz)
            } else {
                bitis
            }
        )
    }

    data class Bakim(
        val tarihMillis: Long = System.currentTimeMillis(),
        val bakimTuru: String = "",
        val tutarYazi: String = "",
        override val not: String = "",
        // Hatirlatma istege bagli. Gun, saat ve dakika ayri tutuluyor cunku iki
        // ayri secici iki ayri sey donduruyor; TripNoktasiFormu ile ayni kalip.
        val hatirlatmaAcik: Boolean = false,
        val hatirlatmaTarihMillis: Long = System.currentTimeMillis(),
        val hatirlatmaSaat: Int? = null,
        val hatirlatmaDakika: Int? = null,
        // Secili hazir aralik (1, 3, 6 ay). null = "Ozel": tarih elle secildi.
        val hatirlatmaAyi: Int? = null,
        // Duzenlemeye acilan kaydin mevcut hatirlatmasi. Zamani gecmis olsa da
        // kullanici ona dokunmadiysa kayit kaydedilebilmeli; bu yuzden ayri tutuluyor.
        val kayitliHatirlatma: Long? = null,
        // Kaydin "yapildi" isareti. Form onu gostermiyor ama kaydederken
        // kaybetmemeli: kullanici sadece notu duzelttiyse isaret kalsin.
        val kayitliYapildi: Long? = null,
        val bakimTuruHatali: Boolean = false,
        val tutarHatali: Boolean = false,
        val hatirlatmaHatali: Boolean = false
    ) : KayitFormu {

        override val kategori get() = Kategori.BAKIM

        val tutar: Double? get() = tutarYazi.replace(',', '.').toDoubleOrNull()

        // Anahtar kapaliysa ya da saat secilmediyse hatirlatma yok sayiliyor.
        val hatirlatmaMillis: Long?
            get() = if (!hatirlatmaAcik || hatirlatmaSaat == null || hatirlatmaDakika == null) {
                null
            } else {
                tarihSaatBirlestir(hatirlatmaTarihMillis, hatirlatmaSaat, hatirlatmaDakika)
            }

        private val bakimTuruGecersiz: Boolean get() = bakimTuru.isBlank()
        private val tutarGecersiz: Boolean get() = (tutar ?: 0.0) <= 0.0

        // Secilen an gecmiste mi. Kayitli (dokunulmamis) hatirlatma gecmiste
        // olabilir: zaten calmis bir hatirlatma, hata degil.
        val hatirlatmaGecmiste: Boolean
            get() = hatirlatmaAcik && (hatirlatmaMillis ?: 0L) <= System.currentTimeMillis()

        // Gecmise alarm kurulamaz, o yuzden yeni secilen gecmis ani kabul etmiyoruz.
        private val hatirlatmaGecersiz: Boolean
            get() = hatirlatmaGecmiste && hatirlatmaMillis != kayitliHatirlatma

        // Hatirlatma degismediyse "yapildi" isareti korunuyor. Yeni bir tarih
        // secildiyse ya da hatirlatma kapatildiysa isaret de kalkiyor: yeni
        // hatirlatma henuz yapilmadi.
        val korunanYapildi: Long?
            get() = kayitliYapildi.takeIf { hatirlatmaMillis == kayitliHatirlatma }

        // Anahtar acildiginda. Ilk kez aciliyorsa tasarimdaki varsayilanlar:
        // bakim tarihinden 3 ay sonra, saat 10:00. Daha once secilmis bir deger
        // varsa (kapatip tekrar acmak) ona dokunmuyoruz.
        fun hatirlatmayiAc(): Bakim =
            if (hatirlatmaSaat == null || hatirlatmaDakika == null) {
                copy(
                    hatirlatmaAcik = true,
                    hatirlatmaAyi = VARSAYILAN_ARALIK,
                    hatirlatmaTarihMillis = ayEkle(tarihMillis, VARSAYILAN_ARALIK),
                    hatirlatmaSaat = VARSAYILAN_SAAT,
                    hatirlatmaDakika = 0
                )
            } else {
                copy(hatirlatmaAcik = true)
            }

        // Hazir aralik cipine basildi: tarih bakim tarihinden sayiliyor.
        fun araligiSec(ay: Int): Bakim = copy(
            hatirlatmaAyi = ay,
            hatirlatmaTarihMillis = ayEkle(tarihMillis, ay),
            hatirlatmaHatali = false
        )

        // Bakim tarihi degisince secili aralik da onunla kayiyor: "3 ay sonra"
        // secilmisse hala 3 ay sonrasi olsun. Ozel tarihe dokunulmuyor.
        fun tarihDegistir(yeni: Long): Bakim = copy(
            tarihMillis = yeni,
            hatirlatmaTarihMillis = hatirlatmaAyi?.let { ayEkle(yeni, it) } ?: hatirlatmaTarihMillis
        )

        // Sablon cipine basildi: tur doluyor, hatirlatma aciliyor ve sablonun
        // onerdigi aralik seciliyor. Kullanici sadece bakiyor; dogruysa dokunmuyor.
        // Cipin ekranda gorunen yazisi ve onerdigi aralik disaridan geliyor:
        // form sinifi metinleri tanimiyor.
        fun sablonSec(ad: String, onerilenAy: Int): Bakim =
            copy(bakimTuru = ad, bakimTuruHatali = false)
                .hatirlatmayiAc()
                .araligiSec(onerilenAy)

        // Tarih elle secildi: artik hazir aralik degil, "Ozel".
        // Bugun secildi ve saat (orn. 10:00) coktan gectiyse saati bir sonraki
        // tam saate aliyoruz; yoksa kullanici neden kaydedemedigini anlamiyordu.
        fun hatirlatmaTarihiSec(yeni: Long): Bakim {
            val simdi = System.currentTimeMillis()
            val saatGecmis = ayniGun(yeni, simdi) &&
                    tarihSaatBirlestir(yeni, hatirlatmaSaat ?: 0, hatirlatmaDakika ?: 0) <= simdi
            val sonrakiSaat = saatAl(simdi) + 1
            return copy(
                hatirlatmaTarihMillis = yeni,
                hatirlatmaAyi = null,
                hatirlatmaHatali = false,
                // 23'ten sonra ayni gunde tam saat kalmiyor; o zaman dokunmuyoruz.
                hatirlatmaSaat = if (saatGecmis && sonrakiSaat <= 23) sonrakiSaat else hatirlatmaSaat,
                hatirlatmaDakika = if (saatGecmis && sonrakiSaat <= 23) 0 else hatirlatmaDakika
            )
        }

        override val gecerli: Boolean
            get() = !bakimTuruGecersiz && !tutarGecersiz && !hatirlatmaGecersiz

        override fun notDegistir(yeni: String): Bakim = copy(not = yeni)

        override fun dogrula(): Bakim = copy(
            bakimTuruHatali = bakimTuruGecersiz,
            tutarHatali = tutarGecersiz,
            hatirlatmaHatali = hatirlatmaGecersiz
        )
    }

    data class Aksesuar(
        val tarihMillis: Long = System.currentTimeMillis(),
        val aksesuarAdi: String = "",
        val tutarYazi: String = "",
        override val not: String = "",
        val aksesuarAdiHatali: Boolean = false,
        val tutarHatali: Boolean = false
    ) : KayitFormu {

        override val kategori get() = Kategori.AKSESUAR

        val tutar: Double? get() = tutarYazi.replace(',', '.').toDoubleOrNull()

        private val aksesuarAdiGecersiz: Boolean get() = aksesuarAdi.isBlank()
        private val tutarGecersiz: Boolean get() = (tutar ?: 0.0) <= 0.0

        override val gecerli: Boolean get() = !aksesuarAdiGecersiz && !tutarGecersiz

        override fun notDegistir(yeni: String): Aksesuar = copy(not = yeni)

        override fun dogrula(): Aksesuar = copy(
            aksesuarAdiHatali = aksesuarAdiGecersiz,
            tutarHatali = tutarGecersiz
        )
    }
}

// Formdaki hazir araliklar ve varsayilanlar. Belgeler geldiginde ayni
// liste orada da kullanilabilir.
// 12 = 1 yil: yillik sablonlar (fren, lastik, filtre) da bir cipe denk gelsin.
val HATIRLATMA_ARALIKLARI = listOf(1, 3, 6, 12)

// Cipteki yazi: 12 ay "1 yil" olarak okunuyor. Ay sayisi dile gore tekil
// ya da cogul yaziliyor (Ingilizce "1 month" / "3 months").
@Composable
@ReadOnlyComposable
fun aralikEtiketi(ay: Int): String =
    if (ay == 12) {
        stringResource(R.string.aralik_bir_yil)
    } else {
        pluralStringResource(R.plurals.aralik_ay, ay, ay)
    }

// Bakim turu icin hazir kisayollar. Tur alani serbest metin kaliyor; sablon
// sadece kisayol. Aralik yazilan metinden tahmin edilmiyor, cipin kendisinden
// geliyor. Yeni sablon eklemek tek satir.
// ad metin degil kimligi: cipin yazisini ekran seciyor. Cipe basilinca o
// yazi bakim turu alanina giriyor, yani kayda hangi dilde goruluyorsa o adla
// kaydediliyor; alan zaten serbest metin.
data class BakimSablonu(@StringRes val ad: Int, val onerilenAy: Int)

val BAKIM_SABLONLARI = listOf(
    BakimSablonu(R.string.sablon_yag, 6),
    BakimSablonu(R.string.sablon_zincir, 1),
    BakimSablonu(R.string.sablon_fren, 12),
    BakimSablonu(R.string.sablon_lastik, 12),
    BakimSablonu(R.string.sablon_filtre, 12)
)
private const val VARSAYILAN_ARALIK = 3
private const val VARSAYILAN_SAAT = 10

// Kayitli bir hatirlatma bakim tarihinden tam 1, 3, 6 ay ya da 1 yil sonraya
// dusuyorsa o cip secili gelsin; degilse "Ozel".
fun hatirlatmaAraligiBul(tarihMillis: Long, hatirlatmaMillis: Long?): Int? {
    hatirlatmaMillis ?: return null
    return HATIRLATMA_ARALIKLARI.firstOrNull { ayniGun(ayEkle(tarihMillis, it), hatirlatmaMillis) }
}

// Kategori degisince o kategorinin bos formu kurulur (form sifirlanir karari).
// Yeni kategori eklendiginde derleyici bu when'i de gosterir.
fun bosForm(kategori: Kategori): KayitFormu = when (kategori) {
    Kategori.YAKIT -> KayitFormu.Yakit()
    Kategori.ROAD_TRIP -> KayitFormu.RoadTrip()
    Kategori.BAKIM -> KayitFormu.Bakim()
    Kategori.AKSESUAR -> KayitFormu.Aksesuar()
}

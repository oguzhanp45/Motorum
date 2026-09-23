package com.oguzhanp.motorum.feature.istatistik

import com.oguzhanp.motorum.core.util.tarihliOkumalar
import com.oguzhanp.motorum.model.Kategori
import com.oguzhanp.motorum.model.Kayit
import java.util.Calendar

// Saf fonksiyon: Compose ve Firestore tanimiyor, ayni girdiye her zaman ayni
// ciktiyi veriyor. Test yazmak istedigimizde dogrudan cagrilabilir.
fun istatistikHesapla(
    kayitlar: List<Kayit>,
    donem: Donem,
    // Disaridan aliniyor ki fonksiyon "bugun"e bagli kalmasin. Icinde
    // System.currentTimeMillis() cagirsaydik sonuc her calistirmada degisirdi.
    simdiMillis: Long = System.currentTimeMillis()
): IstatistikUiState {
    val secilenler = kayitlar.filter { donem.kapsiyorMu(it.tarihMillis, simdiMillis) }
    // Bos donemde asagidaki minOf patlar, ayrica gosterecek bir sey de yok.
    if (secilenler.isEmpty()) return IstatistikUiState(donem = donem)

    val toplamTutar = secilenler.sumOf { it.tutar }

    // Donemin ilk ani; "Tumu" icin yok. Kilometre hesaplarinda donemden onceki
    // son okumayi baslangic cizgisi yapmak icin kullaniliyor.
    val baslangic = donem.baslangici(simdiMillis)

    // Listeden degil enum'dan doniyoruz: yeni kategori eklenince burasi
    // kendiliginden kapsiyor. mapNotNull ise o donemde hic kaydi olmayan
    // kategoriyi listeye koymuyor, "Aksesuar 0 TL" satiri cikmasin.
    val paylar = Kategori.entries.mapNotNull { kategori ->
        val grup = secilenler.filter { it.kategori == kategori }
        if (grup.isEmpty()) return@mapNotNull null
        val grupTutari = grup.sumOf { it.tutar }
        KategoriPayi(
            kategori = kategori,
            tutar = grupTutari,
            adet = grup.size,
            oran = if (toplamTutar > 0) (grupTutari / toplamTutar).toFloat() else 0f
        )
    }.sortedByDescending { it.tutar }

    // litre > 0 sarti birimFiyat icin: tutar/litre sifira bolunurse Infinity
    // cikar ve ekranda sonsuz fiyat yazar.
    val dolumlar = secilenler
        .filterIsInstance<Kayit.Yakit>()
        .filter { it.litre > 0 }

    val noktalar = dolumlar
        .map { YakitNoktasi(tarihMillis = it.tarihMillis, birimFiyat = it.birimFiyat) }
        .sortedBy { it.tarihMillis }

    val toplamLitre = dolumlar.sumOf { it.litre }
    val yakitTutari = dolumlar.sumOf { it.tutar }

    // Tuketim (L/100km) kaldirildi: "her dolumda depo tam" varsayimina
    // dayaniyordu, yarim dolumlarda sayi zipliyordu. Km basi maliyet ise
    // varsayim istemiyor: donemde yakita verilen para / donemde gidilen yol.
    val gidilenYol = donemdeGidilenYol(kayitlar, donem, baslangic, simdiMillis)

    val tripler = secilenler.filterIsInstance<Kayit.RoadTrip>()

    return IstatistikUiState(
        donem = donem,
        kayitAdedi = secilenler.size,
        toplamTutar = toplamTutar,
        // Sabit 12'ye degil, en eski kayittan bugune gecen ay sayisina boluyoruz.
        // Uc haftalik gecmisi 12'ye bolmek ortalamayi gercegin dortte biri gosterirdi.
        aylikOrtalama = toplamTutar / ayAdedi(secilenler.minOf { it.tarihMillis }, simdiMillis),
        paylar = paylar,
        toplamLitre = toplamLitre,
        // Birim fiyatlarin duz ortalamasi degil, toplam tutar / toplam litre.
        // 40 litrelik dolumla 5 litrelik dolum ayni agirligi almasin: para buyuk
        // dolumda harcandi.
        ortalamaBirimFiyat = if (toplamLitre > 0) yakitTutari / toplamLitre else 0.0,
        enUcuzDolum = noktalar.minByOrNull { it.birimFiyat },
        enPahaliDolum = noktalar.maxByOrNull { it.birimFiyat },
        gidilenYol = gidilenYol,
        // Gezi mesafesi devam eden yolculukta 0 donuyor, en uzunu bozmuyor.
        enUzunMesafe = tripler.maxOfOrNull { it.mesafe } ?: 0,
        kmBasiMaliyet = if (gidilenYol > 0 && yakitTutari > 0) yakitTutari / gidilenYol else null,
        yakitNoktalari = noktalar
    )
}

// Donemde gidilen yol: donem icindeki okumalar + donemden onceki son okuma.
//
// Onceki okuma olmadan ayin ilk gunleri kayboluyordu. Ornek: 30 Agustos'ta
// sayac 19.050, 3 Eylul'de 19.100, 20 Eylul'de 19.600. Sadece Eylul'e bakinca
// 500 km cikiyordu; 1-3 Eylul arasinda gidilen 50 km de bu aya ait, dogrusu 550.
//
// Donem icinde hic okuma yoksa sifir: o ay sayaca bakilmamis, uydurmuyoruz.
private fun donemdeGidilenYol(
    kayitlar: List<Kayit>,
    donem: Donem,
    baslangic: Long?,
    simdiMillis: Long
): Int {
    val okumalar = tarihliOkumalar(kayitlar)
    val donemIci = okumalar.filter { donem.kapsiyorMu(it.first, simdiMillis) }
    if (donemIci.isEmpty()) return 0

    val onceki = baslangic?.let { b -> okumalar.filter { it.first < b }.maxByOrNull { it.first } }
    val kmler = donemIci.map { it.second } + listOfNotNull(onceki?.second)
    return if (kmler.size < 2) 0 else kmler.max() - kmler.min()
}

// Donemin ilk milisaniyesi: ayin ya da yilin ilk gunu, gece yarisi.
// "Tumu"nun baslangici yok, oncesinde okuma aranmiyor.
private fun Donem.baslangici(simdiMillis: Long): Long? {
    if (this == Donem.TUMU) return null
    return takvim(simdiMillis).apply {
        if (this@baslangici == Donem.BU_YIL) set(Calendar.MONTH, Calendar.JANUARY)
        set(Calendar.DAY_OF_MONTH, 1)
        set(Calendar.HOUR_OF_DAY, 0)
        set(Calendar.MINUTE, 0)
        set(Calendar.SECOND, 0)
        set(Calendar.MILLISECOND, 0)
    }.timeInMillis
}

// Once yil, sonra ay karsilastiriliyor. Yil kontrolu ikisinde de ortak:
// gecen yilin ayni ayi "bu ay"a girmesin.
private fun Donem.kapsiyorMu(tarihMillis: Long, simdiMillis: Long): Boolean {
    if (this == Donem.TUMU) return true
    val kayit = takvim(tarihMillis)
    val simdi = takvim(simdiMillis)
    if (kayit.get(Calendar.YEAR) != simdi.get(Calendar.YEAR)) return false
    return this == Donem.BU_YIL || kayit.get(Calendar.MONTH) == simdi.get(Calendar.MONTH)
}

private fun ayAdedi(enEskiMillis: Long, simdiMillis: Long): Int {
    val eski = takvim(enEskiMillis)
    val simdi = takvim(simdiMillis)
    val fark = (simdi.get(Calendar.YEAR) - eski.get(Calendar.YEAR)) * 12 +
            (simdi.get(Calendar.MONTH) - eski.get(Calendar.MONTH))
    // En az 1: tek aylik veride bolen sifir olmasin, ileri tarihli bir kayit
    // girilirse de negatife dusmesin.
    return maxOf(fark + 1, 1)
}

private fun takvim(millis: Long): Calendar =
    Calendar.getInstance().apply { timeInMillis = millis }

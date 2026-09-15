package com.oguzhanp.motorum.model

// Esik degerleri tek yerde. Kuralda ciplak sayi yerine isim gorunsun diye.
private const val DONMA_SICAKLIGI = 3.0
private const val SOGUK_SICAKLIK = 7.0
private const val KUVVETLI_RUZGAR = 12.0
private const val SERT_RUZGAR = 8.0
private const val DUSUK_GORUS = 2000

enum class SurusDurumu { IYI, DIKKAT, KOTU }

data class HavaDurumu(
    val sehir: String,
    val sicaklik: Double,
    val aciklama: String,
    val kod: Int,
    val ruzgarHizi: Double,
    val gorusMesafesi: Int
) {

    // Motora binilir mi? Metin degil enum donuyor, cumleyi ekran yaziyor
    // (ui/home/HavaDurumuKarti.kt). Sira onemli: once kotu kosullara bakiliyor.
    val surusDurumu: SurusDurumu
        get() = when {
            yagisVar || firtinaVar ||
                    sicaklik <= DONMA_SICAKLIGI ||
                    ruzgarHizi >= KUVVETLI_RUZGAR -> SurusDurumu.KOTU

            cisentiVar || gorusKapali ||
                    sicaklik <= SOGUK_SICAKLIK ||
                    ruzgarHizi >= SERT_RUZGAR -> SurusDurumu.DIKKAT

            else -> SurusDurumu.IYI
        }

    // Karar servisin sayisal kodundan veriliyor, Turkce aciklamadan degil:
    // metin degisebilir, kodlar sabit.
    private val yagisVar: Boolean
        get() = kod in 200..232 || kod in 500..531 || kod in 600..622

    // 771 saganak ruzgari, 781 hortum.
    private val firtinaVar: Boolean get() = kod == 771 || kod == 781

    private val cisentiVar: Boolean get() = kod in 300..321

    // Sis, pus, toz: yagis yok ama gorus dusuk.
    private val gorusKapali: Boolean
        get() = kod in 701..762 || gorusMesafesi < DUSUK_GORUS
}

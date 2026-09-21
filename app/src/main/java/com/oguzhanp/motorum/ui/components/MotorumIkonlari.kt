package com.oguzhanp.motorum.ui.components

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

// Uygulamanin ikon seti: hepsi tasarim maketlerindeki cizimlerin birebir
// karsiligi. Material'in hazir ikonlari baska bir dilde konusuyordu (dolu
// govdeler, farkli kalinliklar, scooter silueti); ikisi karisinca ekran iki
// ayri setten derlenmis gibi duruyordu.
//
// Ortak dil: 24'luk kutu, 1.9 kalinlik, yuvarlak uc ve kose. Tek istisna
// motor ikonu; o 48'lik kutuda ciziliyor.
//
// Renk burada siyah veriliyor ama ekranda siyah gorunmuyor: Icon bileseni
// vektorun uzerine tint uyguluyor, yani rengi cagiran taraf belirliyor.
object MotorumIkonlari {

    // by lazy: her vektor bir kez kuruluyor, her cizimde yeniden uretilmiyor.
    // Liste satirlari ve alt bar sik cizildigi icin bu fark ediyor.
    val Motor: ImageVector by lazy { motorKur() }

    // --- Kategoriler ---

    // Pompa: govde solda, hortum ve tabanca sagda.
    val Yakit: ImageVector by lazy {
        ikon(
            "Yakit",
            "M4 20V5a2 2 0 012-2h6a2 2 0 012 2v15",
            "M3 20h12",
            "M14 9h3a2 2 0 012 2v5a1.5 1.5 0 003 0v-7"
        )
    }

    val Bakim: ImageVector by lazy {
        ikon("Bakim", "M14.7 6.3a4.5 4.5 0 00-6 6L3 18v3h3l5.7-5.7a4.5 4.5 0 006-6l-2.6 2.6-2.1-2.1z")
    }

    // Katlanmis harita. Ikinci yol katlama cizgileri.
    val RoadTrip: ImageVector by lazy {
        ikon("RoadTrip", "M9 20l-5.5 2V6L9 4l6 2 5.5-2v16L15 22z", "M9 4v16M15 6v16")
    }

    // Kask. Kalkan degil: tasarimda ekipmani anlatiyor.
    val Aksesuar: ImageVector by lazy {
        ikon("Aksesuar", "M12 22s8-4 8-10V5l-8-3-8 3v7c0 6 8 10 8 10z")
    }

    // --- Yon ve eylem ---

    val Geri: ImageVector by lazy { ikon("Geri", "M19 12H5", "M11 6l-6 6 6 6") }
    val Ileri: ImageVector by lazy { ikon("Ileri", "M5 12h13", "M13 6l6 6-6 6") }

    // Cevron: "buraya basinca acilir" isareti. Ok degil, daha sessiz.
    val Cevron: ImageVector by lazy { ikon("Cevron", "M9 6l6 6-6 6") }
    val AsagiOk: ImageVector by lazy { ikon("AsagiOk", "M6 9l6 6 6-6") }

    val Ekle: ImageVector by lazy { ikon("Ekle", "M12 5v14M5 12h14") }

    val Sil: ImageVector by lazy {
        ikon("Sil", "M4 7h16", "M9 7V4h6v3", "M6 7l1 14h10l1-14", "M10 11v6M14 11v6")
    }

    val Duzenle: ImageVector by lazy {
        ikon("Duzenle", "M16.5 4.5a2.1 2.1 0 013 3L8 19l-4 1 1-4z")
    }

    // Uc nokta. Cok kisa cizgiler yuvarlak uc sayesinde nokta gorunuyor.
    val DahaFazla: ImageVector by lazy { ikon("DahaFazla", "M12 6h.01M12 12h.01M12 18h.01") }

    // --- Sekmeler ve ayarlar ---

    val Ev: ImageVector by lazy { ikon("Ev", "M3 10.5L12 3l9 7.5", "M5 9.5V21h14V9.5") }

    // Disli olculerek ciziliyor, hazir bir yoldan kopyalanmiyor. Onceki cizim
    // kutuyu bastan sona dolduruyordu: disler kenardan tasip kirpiliyor ve ikon
    // dortgen bir lekeye donusuyordu. Alti dis 24 dp'de birbirine karismiyor,
    // sekiz dis karisiyordu.
    val Ayarlar: ImageVector by lazy {
        ikon("Ayarlar", disliYolu(), daire(12f, 12f, 3f))
    }

    val Bildirim: ImageVector by lazy {
        ikon(
            "Bildirim",
            "M18 8a6 6 0 10-12 0c0 7-3 9-3 9h18s-3-2-3-9",
            "M13.7 21a2 2 0 01-3.4 0"
        )
    }

    val Cikis: ImageVector by lazy {
        ikon(
            "Cikis",
            "M14 20H6a2 2 0 01-2-2V6a2 2 0 012-2h8",
            "M17 15l4-3-4-3M21 12H10"
        )
    }

    val Kilitli: ImageVector by lazy {
        ikon(
            "Kilitli",
            "M6.5 10.5H17.5a2.5 2.5 0 012.5 2.5V18.5a2.5 2.5 0 01-2.5 2.5H6.5a2.5 2.5 0 01-2.5-2.5V13a2.5 2.5 0 012.5-2.5z",
            "M8 10.5V7a4 4 0 018 0v3.5"
        )
    }

    // --- Form ve giris ---

    val Eposta: ImageVector by lazy {
        ikon(
            "Eposta",
            "M5 5H19a2.5 2.5 0 012.5 2.5V16.5a2.5 2.5 0 01-2.5 2.5H5a2.5 2.5 0 01-2.5-2.5V7.5A2.5 2.5 0 015 5z",
            "M3 7l9 6 9-6"
        )
    }

    val Goz: ImageVector by lazy {
        ikon(
            "Goz",
            "M2 12s3.5-6.5 10-6.5S22 12 22 12s-3.5 6.5-10 6.5S2 12 2 12z",
            daire(12f, 12f, 2.8f)
        )
    }

    // Kapali goz ayni cizim + uzerine cizik: iki ayri sekil yerine tek fikrin
    // iki hali, gecis de dogal duruyor.
    val GozKapali: ImageVector by lazy {
        ikon(
            "GozKapali",
            "M2 12s3.5-6.5 10-6.5S22 12 22 12s-3.5 6.5-10 6.5S2 12 2 12z",
            daire(12f, 12f, 2.8f),
            "M3 3l18 18"
        )
    }

    val Saat: ImageVector by lazy { ikon("Saat", daire(12f, 12f, 9f), "M12 7v5l3 2") }

    val Takvim: ImageVector by lazy {
        ikon(
            "Takvim",
            "M5 5H19a2 2 0 012 2V19a2 2 0 01-2 2H5a2 2 0 01-2-2V7a2 2 0 012-2z",
            "M3 10h18M8 3v4M16 3v4"
        )
    }

    val Fotograf: ImageVector by lazy {
        ikon(
            "Fotograf",
            "M3 8.5A2 2 0 015 6.5h2l1.5-2h7L17 6.5h2a2 2 0 012 2V18a2 2 0 01-2 2H5a2 2 0 01-2-2z",
            daire(12f, 13f, 3.5f)
        )
    }

    // Plaka: cerceve ve icindeki uc satir.
    val Plaka: ImageVector by lazy {
        ikon(
            "Plaka",
            "M5 6H19a2.5 2.5 0 012.5 2.5V15.5a2.5 2.5 0 01-2.5 2.5H5a2.5 2.5 0 01-2.5-2.5V8.5A2.5 2.5 0 015 6z",
            "M7 11h2M12 11h5M7 14.5h10"
        )
    }

    // Yenile: ucu okla biten yay. "Tekrar dene" dugmelerinde.
    val Yenile: ImageVector by lazy { ikon("Yenile", "M20 11a8 8 0 10-2.3 5.7", "M20 5v6h-6") }

    // --- Hava seridi ---

    val Konum: ImageVector by lazy {
        ikon("Konum", "M12 21s7-6 7-11a7 7 0 10-14 0c0 5 7 11 7 11z", daire(12f, 10f, 2.5f))
    }

    // Ustu cizili bulut: "hava bilgisi yok". Cizik bulutun ortasindan
    // gectigi icin bulut iki parca yaziliyor, cizigin altinda kalan kisim yok.
    val BulutYok: ImageVector by lazy {
        ikon(
            "BulutYok",
            "M6.5 18h11a4 4 0 00.6-7.95A6 6 0 007.2 8.6",
            "M6.5 18a3.5 3.5 0 01-.4-6.98",
            "M3 3l18 18"
        )
    }

    // --- Secim ---

    // --- Ayarlar ---

    // Hilal: tema satiri.
    val Ay: ImageVector by lazy { ikon("Ay", "M20.5 14.2A8.5 8.5 0 019.8 3.5a8.5 8.5 0 1010.7 10.7z") }

    // Kure: dil satiri.
    val Dunya: ImageVector by lazy {
        ikon("Dunya", daire(12f, 12f, 9f), "M3 12h18M12 3a15 15 0 010 18M12 3a15 15 0 000 18")
    }

    // Bas ve omuz: hesap satiri.
    val Kisi: ImageVector by lazy { ikon("Kisi", daire(12f, 8f, 3.5f), "M4.5 20a7.5 7.5 0 0115 0") }

    // Tepsiye inen ok: disa aktar. Maketlerde yoktu, ayni dilde cizildi.
    val DisaAktar: ImageVector by lazy { ikon("DisaAktar", "M12 3v12", "M7 10l5 5 5-5", "M5 21h14") }

    val Onay: ImageVector by lazy { ikon("Onay", daire(12f, 12f, 9f), "M8 12.3l2.6 2.6L16 9.5") }
    val BosDaire: ImageVector by lazy { ikon("BosDaire", daire(12f, 12f, 9f)) }
}

// Motor cizimi 48'lik kutuda: tekerlek yaylari ve govde egrileri bu olcekte
// tam sayilara oturuyor, 24'te her koordinat ondalikli olurdu.
private const val MOTOR_KUTUSU = 48f
private const val TEKER_KALINLIGI = 4f
private const val CUBUK_KALINLIGI = 3.6f

private const val KUTU = 24f
private const val KALINLIK = 1.9f


// Disli silueti: her dis icin dis ucunda iki, govdede iki nokta koyup araliksiz
// birlestiriyoruz. Yuvarlak kose birlesimi kosegenleri yumusatiyor, o yuzden
// koseli gorunmuyor. Sayilari degistirip dis sayisini ya da derinligini
// ayarlamak icin tek yer burasi.
//
// Sayilar dogrudan yaziliyor, String.format ile degil: bicimlendirme cihazin
// diline uyar ve Turkce'de ondalik ayraci virgul olur, virgullu bir yol da
// okunamaz.
private fun disliYolu(
    dis: Int = 6,
    disUcu: Float = 9f,
    govde: Float = 6.4f,
    agiz: Float = 0.34f
): String {
    val adim = (2 * PI / dis).toFloat()
    val nokta = StringBuilder()
    for (i in 0 until dis) {
        val aci = i * adim - (PI / 2).toFloat()
        listOf(
            disUcu to aci - agiz / 2,
            disUcu to aci + agiz / 2,
            govde to aci + adim / 2 - agiz / 2,
            govde to aci + adim / 2 + agiz / 2
        ).forEach { (yaricap, konum) ->
            val x = 12 + yaricap * cos(konum)
            val y = 12 + yaricap * sin(konum)
            nokta.append(if (nokta.isEmpty()) "M" else "L").append(x).append(' ').append(y)
        }
    }
    return nokta.append("Z").toString()
}

// Daireyi iki yay olarak yaziyoruz: yol tanimlarinda daire komutu yok.
private fun daire(x: Float, y: Float, r: Float): String =
    "M${x - r} $y a$r $r 0 1 0 ${r * 2} 0 a$r $r 0 1 0 ${-r * 2} 0"

// Govde: on catal dibinden baslayip depo, sele ve arka sasi boyunca donuyor.
private const val GOVDE =
    "M14 26 C15.5 22 19 20.5 23 20.5 L28 20.5 C30 20.5 31.2 21.4 31.8 22.8 " +
            "L34 28 L29 28 L27.5 24.5 L20.5 24.5 L19 28 L22 30 L17 31 Z"

private fun motorKur(): ImageVector = kutu("Motor", MOTOR_KUTUSU).apply {
    // Tekerlekler once: govde onlarin uzerine binsin.
    cizgi(daire(12f, 33f, 8f), TEKER_KALINLIGI)
    cizgi(daire(36f, 33f, 8f), TEKER_KALINLIGI)

    // Gobekler. Ikisi olmadan tekerlek "delik" gibi okunuyor ve motordan cok
    // bisiklete benziyordu.
    dolgu(daire(12f, 33f, 2.2f))
    dolgu(daire(36f, 33f, 2.2f))

    dolgu(GOVDE)

    // Gidon kolu, gidon ve egzoz: uc ince cubuk.
    cizgi("M31.5 22.5 L34.5 17.5", CUBUK_KALINLIGI)
    cizgi("M31.5 17.5 H38.5", CUBUK_KALINLIGI)
    cizgi("M32.5 26 L35.5 32", CUBUK_KALINLIGI)
}.build()

// Setin tamami ayni kaliptan cikiyor: ayni kutu, ayni kalinlik, ayni yuvarlak
// uclar. Tek farklari yollari. Tutarlilik burada, cagri yerlerinde degil.
private fun ikon(ad: String, vararg yollar: String): ImageVector =
    kutu(ad, KUTU).apply { yollar.forEach { cizgi(it, KALINLIK) } }.build()


// Cizim kutusu ne olursa olsun ekrana 24 dp oturuyor.
private fun kutu(ad: String, olcu: Float) = ImageVector.Builder(
    name = ad,
    defaultWidth = 24.dp,
    defaultHeight = 24.dp,
    viewportWidth = olcu,
    viewportHeight = olcu
)

private fun ImageVector.Builder.cizgi(veri: String, kalinlik: Float) = addPath(
    pathData = PathParser().parsePathString(veri).toNodes(),
    stroke = SolidColor(Color.Black),
    strokeLineWidth = kalinlik,
    strokeLineCap = StrokeCap.Round,
    strokeLineJoin = StrokeJoin.Round
)

private fun ImageVector.Builder.dolgu(veri: String) = addPath(
    pathData = PathParser().parsePathString(veri).toNodes(),
    fill = SolidColor(Color.Black)
)

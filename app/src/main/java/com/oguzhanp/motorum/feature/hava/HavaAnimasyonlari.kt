package com.oguzhanp.motorum.feature.hava

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.State
import androidx.compose.runtime.produceState
import androidx.compose.runtime.withFrameMillis
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.unit.dp
import kotlin.math.PI
import kotlin.math.cos

// Hava seridinin hareketli parcalari: ikon, yagmur, simsek parlamasi ve
// suzulen bulutlar. Tasarimdaki CSS animasyonlarinin birebir karsiligi;
// sureler ve anahtar kareler oradan alindi.
//
// Hepsi tek bir saatten besleniyor ve saat sadece CIZIM sirasinda okunuyor.
// Bu onemli: her karede degisen bir degeri ekran duzeninde okusaydik butun
// serit saniyede altmis kez yeniden kurulurdu. Boyle sadece fircalar yeniden
// calisiyor.

// Ikon hangi resmi cizecek. Hava kodundan geliyor; seridin rengi ise surus
// durumundan. Ikisi ayri bilgi: iliman bir yagmur "dikkat", karli gun "kotu".
internal enum class HavaTuru { ACIK, PARCALI, BULUTLU, YAGMUR, KAR, FIRTINA }

// Ayni kod araliklari model/HavaDurumu.kt icinde surus kararini veriyor;
// burada sadece hangi resmin cizilecegini soyluyorlar.
internal fun havaTuru(kod: Int): HavaTuru = when (kod) {
    in 200..232 -> HavaTuru.FIRTINA
    in 300..321, in 500..531 -> HavaTuru.YAGMUR
    in 600..622 -> HavaTuru.KAR
    800 -> HavaTuru.ACIK
    in 801..804 -> HavaTuru.PARCALI
    // 700'ler: sis, pus, toz. Tasarimda ayri resmi yok, bulut ciziliyor.
    else -> HavaTuru.BULUTLU
}

// Ekran acildigindan beri gecen milisaniye. Her karede guncelleniyor; serit
// ekrandan cikinca (liste onu atinca) kendiliginden duruyor, pil yemiyor.
@Composable
internal fun rememberSaat(): State<Long> = produceState(0L) {
    val baslangic = withFrameMillis { it }
    while (true) withFrameMillis { value = it - baslangic }
}

// ---------------------------------------------------------------- IKON

private fun yol(veri: String): Path = PathParser().parsePathString(veri).toPath()

private val BUYUK_ISINLAR by lazy {
    yol("M12 2v2M12 20v2M4.9 4.9l1.4 1.4M17.7 17.7l1.4 1.4M2 12h2M20 12h2M4.9 19.1l1.4-1.4M17.7 6.3l1.4-1.4")
}
private val KUCUK_ISINLAR by lazy { yol("M8 2.2v1.4M2.2 8h1.4M3.9 3.9l1 1M12.1 3.9l-1 1") }
private val PARCALI_BULUT by lazy { yol("M17 20a4 4 0 000-8 5.4 5.4 0 00-10.2 1.9A3.5 3.5 0 007 20z") }
private val BULUT by lazy { yol("M17.5 14.5a4.5 4.5 0 000-9 6 6 0 00-11.3 2.1A3.9 3.9 0 006.5 14.5z") }
// Firtinada bulut bir buçuk birim yukarida: altina simsege yer aciliyor.
private val FIRTINA_BULUTU by lazy { yol("M17.5 13a4.5 4.5 0 000-9 6 6 0 00-11.3 2.1A3.9 3.9 0 006.5 13z") }
private val SIMSEK by lazy { yol("M13.2 11.5l-3.4 5.8h3.6l-2.6 5.2") }

private val SIMSEK_SARISI = Color(0xFFFBBF24)

@Composable
internal fun HavaIkonu(
    tur: HavaTuru,
    renk: Color,
    saat: State<Long>,
    modifier: Modifier = Modifier
) {
    Canvas(modifier.size(24.dp)) {
        val ms = saat.value
        // Cizimler 24'luk kutuda tanimli; kutuyu ekran boyutuna olcekliyoruz.
        // Olcek kalinligi da buyuttugu icin kalinlik 24'luk kutunun degeri.
        val olcek = size.width / 24f
        val kalem = Stroke(width = 1.9f, cap = StrokeCap.Round, join = StrokeJoin.Round)

        scale(olcek, olcek, pivot = Offset.Zero) {
            when (tur) {
                HavaTuru.ACIK -> gunes(ms, renk, kalem, Offset(12f, 12f), 4f, BUYUK_ISINLAR)

                HavaTuru.PARCALI -> {
                    gunes(ms, renk, kalem, Offset(8f, 8f), 3f, KUCUK_ISINLAR)
                    translate(left = bulutSalinimi(ms)) { drawPath(PARCALI_BULUT, renk, style = kalem) }
                }

                HavaTuru.BULUTLU, HavaTuru.YAGMUR, HavaTuru.KAR ->
                    translate(left = bulutSalinimi(ms)) { drawPath(BULUT, renk, style = kalem) }

                HavaTuru.FIRTINA -> {
                    drawPath(FIRTINA_BULUTU, renk, style = kalem)
                    drawPath(SIMSEK, SIMSEK_SARISI.copy(alpha = simsekParlakligi(ms)), style = kalem)
                }
            }
        }
    }
}

// Isinlar 22 saniyede bir tur donuyor, cekirdek 3.4 saniyede bir nefes aliyor.
// Ikisi farkli hizda oldugu icin hareket hic ayni kareye donmuyor, canli duruyor.
private fun DrawScope.gunes(
    ms: Long, renk: Color, kalem: Stroke, merkez: Offset, yaricap: Float, isinlar: Path
) {
    val aci = (ms % 22_000L) / 22_000f * 360f
    rotate(aci, pivot = merkez) { drawPath(isinlar, renk, style = kalem) }

    // 1 -> 1.14 -> 1. Kosinus ease-in-out gibi davraniyor: uclarda yavas.
    val t = (ms % 3_400L) / 3_400f
    val nabiz = 1f + 0.07f * (1f - cos(2f * PI.toFloat() * t))
    scale(nabiz, pivot = merkez) { drawCircle(renk, radius = yaricap, center = merkez, style = kalem) }
}

// Bulut 5 saniyede bir iki birim saga sola gidip geliyor.
private fun bulutSalinimi(ms: Long): Float {
    val t = (ms % 5_000L) / 5_000f
    return -2f * cos(2f * PI.toFloat() * t)
}

// Tasarimdaki simsekCak: cogu zaman soluk, sonlara dogru iki kez cakiyor.
private fun simsekParlakligi(ms: Long): Float = kare(
    (ms % 3_600L) / 3_600f,
    0f to 0.35f, 0.84f to 0.35f, 0.86f to 1f, 0.88f to 0.3f,
    0.90f to 1f, 0.94f to 0.35f, 1f to 0.35f
)

// ---------------------------------------------------------- SERIT ICI

// Yagmur cizgileri: konum (genislik orani), boy, saydamlik, sure, gecikme.
// Sureler farkli ki cizgiler ayni anda dusup "yagmur perdesi" gibi durmasin.
private class Damla(val sol: Float, val boy: Float, val alfa: Float, val sure: Long, val gecikme: Long)

private val DAMLALAR = listOf(
    Damla(0.14f, 13f, 0.80f, 1_100, 0),
    Damla(0.29f, 10f, 0.60f, 1_350, 400),
    Damla(0.47f, 14f, 0.75f, 1_200, 750),
    Damla(0.63f, 9f, 0.50f, 1_500, 200),
    Damla(0.78f, 12f, 0.70f, 1_250, 950),
    Damla(0.90f, 11f, 0.55f, 1_400, 550)
)

private val DAMLA_RENGI = Color(0xFFDBEAFE)

// Suzulen bulutlar: iki elipsten olusan yumusak lekeler.
private class Leke(
    val ust: Float, val sure: Long, val gecikme: Long,
    val elipsler: List<FloatArray> // cx, cy, rx, ry
)

private val LEKELER = listOf(
    Leke(8f, 26_000, 0, listOf(floatArrayOf(11f, 10f, 11f, 6f), floatArrayOf(21f, 8f, 9f, 7f))),
    Leke(6f, 19_000, 0, listOf(floatArrayOf(13f, 11f, 13f, 7f), floatArrayOf(26f, 9f, 11f, 8f))),
    Leke(22f, 31_000, 9_000, listOf(floatArrayOf(9f, 8f, 9f, 5f), floatArrayOf(18f, 7f, 8f, 6f)))
)

@Composable
internal fun HavaParcaciklari(
    tur: HavaTuru,
    saat: State<Long>,
    modifier: Modifier = Modifier,
    // Karanlik temada beyaz bulut koyu seritte leke gibi parliyor; orada gri veriliyor.
    bulutRengi: Color = Color.White
) {
    // Acik ve karli havada serit sakin: parcacik yok.
    if (tur != HavaTuru.YAGMUR && tur != HavaTuru.FIRTINA && tur != HavaTuru.PARCALI) return

    Canvas(modifier) {
        val ms = saat.value
        when (tur) {
            HavaTuru.PARCALI -> suzulenBulutlar(ms, bulutRengi)
            HavaTuru.FIRTINA -> { gokParlamasi(ms); yagmur(ms) }
            else -> yagmur(ms)
        }
    }
}

// Her cizgi yukaridan girip sola kayarak asagi iniyor. Serit kenarlari
// cizgileri kesiyor, o yuzden seridin disindan baslayip disinda bitiyorlar.
private fun DrawScope.yagmur(ms: Long) {
    val genislik = 1.5.dp.toPx()
    DAMLALAR.forEach { d ->
        val ilerleme = ((ms + d.gecikme) % d.sure) / d.sure.toFloat()
        val boy = d.boy.dp.toPx()
        val x = size.width * d.sol - 14.dp.toPx() * ilerleme
        val y = -54.dp.toPx() + 96.dp.toPx() * ilerleme
        drawRoundRect(
            brush = Brush.verticalGradient(
                listOf(DAMLA_RENGI.copy(alpha = 0f), DAMLA_RENGI.copy(alpha = d.alfa)),
                startY = y, endY = y + boy
            ),
            topLeft = Offset(x, y),
            size = Size(genislik, boy),
            cornerRadius = CornerRadius(genislik)
        )
    }
}

// Gok gurultusunde tum serit bir an aydinlaniyor, simsekle ayni ritimde.
private fun DrawScope.gokParlamasi(ms: Long) {
    val alfa = kare(
        (ms % 3_600L) / 3_600f,
        0f to 0f, 0.83f to 0f, 0.85f to 0.85f, 0.87f to 0.1f,
        0.89f to 0.6f, 0.93f to 0f, 1f to 0f
    )
    if (alfa <= 0f) return
    drawRect(
        brush = Brush.linearGradient(
            0f to Color(0xFFE2E8F0).copy(alpha = 0.9f),
            0.45f to Color(0xFFBFDBFE).copy(alpha = 0.35f),
            0.75f to Color.Transparent,
            start = Offset.Zero,
            end = Offset(size.width, size.height * 0.27f)
        ),
        alpha = alfa
    )
}

// Parcali bulutlu gunde seridin arkasindan yavasca bulut geciyor. Basta ve
// sonda saydamlasiyor ki kenardan "kesilip" girmesin.
private fun DrawScope.suzulenBulutlar(ms: Long, renk: Color) {
    LEKELER.forEach { l ->
        val ilerleme = ((ms + l.gecikme) % l.sure) / l.sure.toFloat()
        val alfa = kare(ilerleme, 0f to 0f, 0.12f to 0.5f, 0.88f to 0.5f, 1f to 0f)
        val x = -30.dp.toPx() + (size.width + 100.dp.toPx()) * ilerleme
        val ust = l.ust.dp.toPx()
        l.elipsler.forEach { (cx, cy, rx, ry) ->
            drawOval(
                color = renk.copy(alpha = alfa),
                topLeft = Offset(x + (cx - rx).dp.toPx(), ust + (cy - ry).dp.toPx()),
                size = Size((rx * 2).dp.toPx(), (ry * 2).dp.toPx())
            )
        }
    }
}

// Anahtar kareler arasinda dogrusal gecis. CSS @keyframes'in kucuk karsiligi:
// (zaman, deger) ciftleri sirali verilmeli.
private fun kare(t: Float, vararg noktalar: Pair<Float, Float>): Float {
    if (t <= noktalar.first().first) return noktalar.first().second
    for (i in 1 until noktalar.size) {
        val (t0, d0) = noktalar[i - 1]
        val (t1, d1) = noktalar[i]
        if (t <= t1) return if (t1 == t0) d1 else d0 + (d1 - d0) * ((t - t0) / (t1 - t0))
    }
    return noktalar.last().second
}

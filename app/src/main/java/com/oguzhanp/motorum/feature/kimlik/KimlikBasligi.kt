package com.oguzhanp.motorum.feature.kimlik

import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clipToBounds
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oguzhanp.motorum.core.tasarim.AcikDurumCubugu
import com.oguzhanp.motorum.core.tasarim.MotorumIkonlari
import com.oguzhanp.motorum.core.tasarim.MotorumLogosu
import com.oguzhanp.motorum.core.tasarim.MotorumTheme
import com.oguzhanp.motorum.core.tasarim.karanlikTema
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sin

// Giris ve uye ol ekranlarinin ustundeki koyu alan (tasarim: Giris).
// Arkada iki yol cizgisi; ondeki kesikli olan akiyor, uzerinde kucuk bir motor
// sagdan sola gidiyor. Ortada "M" isareti hafifce yukari asagi suzuluyor.
//
// Bu alanin renkleri tema takiminda degil, burada: iki temada da koyu kalan,
// sadece bu ekrana ait bir gorsel (hava seridi de ayni yolu izliyor).
@Composable
fun KimlikBasligi(modifier: Modifier = Modifier) {
    val karanlik = karanlikTema
    val gecis = if (karanlik) KARANLIK_GECIS else ACIK_GECIS

    AcikDurumCubugu()

    val hareket = rememberInfiniteTransition(label = "baslik")
    // Seridin bir desen boyu (10 dolu + 14 bos) kaymasi. Tam bir desen boyu
    // kaydigi icin basa donus gorunmuyor.
    val seritFazi by hareket.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(SERIT_SURE_MS, easing = LinearEasing)),
        label = "serit"
    )
    // Motor yolun sonundan (sag) basina (sol) gidiyor.
    val surus by hareket.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(SURUS_SURE_MS, easing = LinearEasing)),
        label = "surus"
    )
    val suzulme by hareket.animateFloat(
        initialValue = 0f, targetValue = -3f,
        animationSpec = infiniteRepeatable(
            tween(SUZULME_SURE_MS / 2, easing = EaseInOut), RepeatMode.Reverse
        ),
        label = "suzulme"
    )

    val motor = rememberVectorPainter(MotorumIkonlari.Motor)

    Box(
        modifier = modifier
            .fillMaxWidth()
            .clipToBounds()
            .drawWithCache {
                val firca = cssGecisi(gecis, size)
                onDrawBehind { drawRect(firca) }
            }
            // Koyu zemin durum cubugunun altina da uzaniyor; icerik onun altinda.
            .statusBarsPadding()
            .height(250.dp),
        contentAlignment = Alignment.Center
    ) {
        Spacer(Modifier.fillMaxSize().drawWithCache {
            // Cizim 390 x 250'lik maket kutusunda; ekrana esnetiyoruz. Yollar
            // boyut degisince bir kez kuruluyor, her karede sadece ciziliyor.
            val olcek = Matrix().apply { scale(size.width / MAKET_EN, size.height / MAKET_BOY) }
            val on = PathParser().parsePathString(ON_YOL).toPath().apply { transform(olcek) }
            val arka = PathParser().parsePathString(ARKA_YOL).toPath().apply { transform(olcek) }
            val olcu = PathMeasure().apply { setPath(on, false) }
            val birim = size.width / MAKET_EN
            val boy = 26.dp.toPx()
            val kalinlik = 2.dp.toPx()
            val yukari = 9.dp.toPx()

            onDrawBehind {
                drawPath(arka, Color.White.copy(alpha = 0.12f), style = Stroke(kalinlik))
                drawPath(
                    on, Color.White.copy(alpha = 0.22f),
                    style = Stroke(
                        kalinlik,
                        pathEffect = PathEffect.dashPathEffect(
                            floatArrayOf(10f * birim, 14f * birim),
                            seritFazi * 24f * birim
                        )
                    )
                )

                // Motor: yolun o noktasina oturuyor ve egime gore yatiyor.
                val mesafe = olcu.length * surus
                val nokta = olcu.getPosition(mesafe)
                val yon = olcu.getTangent(mesafe)
                val aci = Math.toDegrees(atan2(yon.y, yon.x).toDouble()).toFloat()
                translate(nokta.x, nokta.y) {
                    rotate(aci, pivot = Offset.Zero) {
                        // Aynalama: yol sola dogru gidiyor, motor da sola baksin.
                        scale(-1f, 1f, pivot = Offset.Zero) {
                            // Tekerlekler cizginin ustune bassin diye biraz yukarida.
                            translate(-boy / 2, -boy / 2 - yukari) {
                                with(motor) {
                                    draw(Size(boy, boy), colorFilter = ColorFilter.tint(SURUCU_RENGI))
                                }
                            }
                        }
                    }
                }
            }
        })

        Column(
            // Suzulme sadece cizim katmaninda: yerlesim her karede yeniden hesaplanmiyor.
            modifier = Modifier.graphicsLayer { translationY = suzulme.dp.toPx() },
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MotorumLogosu(yolRengi = LOGO_RENGI, seritRengi = LOGO_SERIT_RENGI)
            Text(
                text = "Motorum",
                style = MaterialTheme.typography.headlineMedium,
                fontSize = 26.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-0.6).sp,
                color = LOGO_RENGI
            )
        }
    }
}

// CSS'teki "linear-gradient(160deg, ...)" karsiligi. CSS acisi yukaridan saat
// yonunde; gecis cizgisinin boyu da kutunun o yondeki izdusumu.
private fun cssGecisi(renkler: List<Pair<Float, Color>>, boyut: Size): Brush {
    val radyan = Math.toRadians(GECIS_ACISI.toDouble())
    val yx = sin(radyan).toFloat()
    val yy = -cos(radyan).toFloat()
    val yarim = (boyut.width * abs(yx) + boyut.height * abs(yy)) / 2f
    val merkez = Offset(boyut.width / 2f, boyut.height / 2f)
    return Brush.linearGradient(
        colorStops = renkler.toTypedArray(),
        start = merkez - Offset(yx, yy) * yarim,
        end = merkez + Offset(yx, yy) * yarim
    )
}

private const val GECIS_ACISI = 160f
private val ACIK_GECIS = listOf(
    0f to Color(0xFF374151), 0.48f to Color(0xFF1F2937), 1f to Color(0xFF0B1220)
)
private val KARANLIK_GECIS = listOf(
    0f to Color(0xFF1E2635), 0.48f to Color(0xFF141B29), 1f to Color(0xFF07090E)
)
private val LOGO_RENGI = Color(0xFFF9FAFB)
private val LOGO_SERIT_RENGI = Color(0xFF4B5563)
private val SURUCU_RENGI = Color(0xFFF1F5F9)

private const val MAKET_EN = 390f
private const val MAKET_BOY = 250f
private const val ON_YOL = "M-20 205 C 90 175, 150 235, 250 195 S 380 160, 410 180"
private const val ARKA_YOL = "M-20 228 C 100 200, 170 252, 270 215 S 390 185, 410 202"

// Maketteki hiz: serit 2.4 sn'de 80 birim; bir desen boyu (24) bu hizla 720 ms.
private const val SERIT_SURE_MS = 720
private const val SURUS_SURE_MS = 6000
private const val SUZULME_SURE_MS = 3600

@Preview
@Composable
private fun KimlikBasligiPreview() {
    MotorumTheme(karanlik = false) { KimlikBasligi() }
}

@Preview
@Composable
private fun KimlikBasligiKaranlikPreview() {
    MotorumTheme(karanlik = true) { KimlikBasligi() }
}

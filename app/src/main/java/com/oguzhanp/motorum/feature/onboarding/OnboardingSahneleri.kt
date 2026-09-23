package com.oguzhanp.motorum.feature.onboarding

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawWithCache
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.Matrix
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.rotate
import androidx.compose.ui.graphics.drawscope.translate
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.graphics.vector.rememberVectorPainter
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oguzhanp.motorum.core.tasarim.AksesuarMetin
import com.oguzhanp.motorum.core.tasarim.AksesuarRenk
import com.oguzhanp.motorum.core.tasarim.AksesuarZemin
import com.oguzhanp.motorum.core.tasarim.AppMotion
import com.oguzhanp.motorum.core.tasarim.BakimMetin
import com.oguzhanp.motorum.core.tasarim.BakimRenk
import com.oguzhanp.motorum.core.tasarim.BakimZemin
import com.oguzhanp.motorum.core.tasarim.CizgiSolgun
import com.oguzhanp.motorum.core.tasarim.KartZemin
import com.oguzhanp.motorum.core.tasarim.Kenar
import com.oguzhanp.motorum.core.tasarim.MetinAna
import com.oguzhanp.motorum.core.tasarim.MetinIkincil
import com.oguzhanp.motorum.core.tasarim.MetinSolgun
import com.oguzhanp.motorum.core.tasarim.MotorumIkonlari
import com.oguzhanp.motorum.core.tasarim.MotorumLogosu
import com.oguzhanp.motorum.core.tasarim.MotorumTheme
import com.oguzhanp.motorum.core.tasarim.Murekkep
import com.oguzhanp.motorum.core.tasarim.RoadTripMetin
import com.oguzhanp.motorum.core.tasarim.RoadTripRenk
import com.oguzhanp.motorum.core.tasarim.RoadTripZemin
import com.oguzhanp.motorum.core.tasarim.SekmeZemin
import com.oguzhanp.motorum.core.tasarim.YakitMetin
import com.oguzhanp.motorum.core.tasarim.YakitRenk
import com.oguzhanp.motorum.core.tasarim.YakitZemin
import com.oguzhanp.motorum.core.tasarim.karanlikTema
import kotlinx.coroutines.delay
import kotlin.math.atan2

// Tanitimin uc sayfasindaki gorseller (tasarim: Onboarding).

// ------------------------------------------------------------ 1. HOS GELDIN
//
// 4.6 sn'lik tek bir dongu, parcalar sirayla: once "M" isareti ciziliyor
// (seridi icinde akarken), sonra altinda yol beliriyor, motor yoldan gecip
// sonunda asagi dusuyor. Ayni anda oynamiyorlar ki birbiriyle yarismasinlar.
// Hepsi tek bir zaman degerinden (0..1) hesaplaniyor; her parcanin kendi
// saati olsaydi zamanla birbirinden kayarlardi.
@Composable
fun HosGeldinSahnesi(modifier: Modifier = Modifier) {
    val onizleme = LocalInspectionMode.current
    val dongu = rememberInfiniteTransition(label = "hosgeldin")
    val zaman by dongu.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(DONGU_MS, easing = LinearEasing)),
        label = "zaman"
    )
    // Serit kendi hizinda akiyor: bir desen boyu (5 + 7) 450 ms'de.
    val seritFazi by dongu.animateFloat(
        initialValue = 1f, targetValue = 0f,
        animationSpec = infiniteRepeatable(tween(SERIT_MS, easing = LinearEasing)),
        label = "serit"
    )
    // Onizlemede an sabit: isaret cizilmis, motor yolun ortasinda.
    val an = { if (onizleme) 0.6f else zaman }

    val halkaDis = SekmeZemin
    val halkaIc = CizgiSolgun
    val yolRengi = Kenar
    val surucuRengi = MetinAna
    val motor = rememberVectorPainter(MotorumIkonlari.Motor)

    Box(
        modifier = modifier
            .size(190.dp, 196.dp)
            .drawWithCache {
                // Maket 190 x 196'lik kutuda; bir birim = 1 dp.
                val birim = size.width / 190f
                val yol = PathParser().parsePathString(SAHNE_YOLU).toPath()
                    .apply { transform(Matrix().apply { scale(birim, birim) }) }
                val olcu = PathMeasure().apply { setPath(yol, false) }
                val merkez = Offset(95f * birim, 88f * birim)
                val ince = Stroke(1.4f * birim)
                val boy = 20f * birim

                onDrawWithContent {
                    val t = an()
                    drawCircle(halkaDis, 82f * birim, merkez, style = ince)
                    drawCircle(halkaIc, 61f * birim, merkez, style = ince)

                    // Yol: isaret bitince beliriyor.
                    val yolGorunur = aralik(t, 0.30f, 0.40f, CIKIS_EGRISI)
                    if (yolGorunur > 0f) {
                        drawPath(
                            yol, yolRengi, alpha = yolGorunur,
                            style = Stroke(2.6f * birim, cap = StrokeCap.Round)
                        )
                    }

                    // Isaret (Box'in icerigi) yolun ustunde, motor en ustte.
                    drawContent()

                    // Motor 0.36-0.78 arasi yolu bastan sona geciyor, 0.76-0.88
                    // arasi yolun sonundan asagi dusup kayboluyor.
                    val gorunurluk = when {
                        t < 0.36f -> 0f
                        t < 0.42f -> (t - 0.36f) / 0.06f
                        t < 0.76f -> 1f
                        t < 0.88f -> 1f - (t - 0.76f) / 0.12f
                        else -> 0f
                    }
                    if (gorunurluk > 0f) {
                        val mesafe = olcu.length * aralik(t, 0.36f, 0.78f, SURUS_EGRISI)
                        val dusus = aralik(t, 0.76f, 0.88f, LinearEasing)
                        val nokta = olcu.getPosition(mesafe)
                        val yon = olcu.getTangent(mesafe)
                        val aci = Math.toDegrees(atan2(yon.y, yon.x).toDouble()).toFloat()
                        translate(nokta.x, nokta.y + dusus * 30f * birim) {
                            rotate(aci + dusus * 20f, pivot = Offset.Zero) {
                                // 7 dp yukarida: tekerlekler cizginin ustune bassin.
                                translate(-boy / 2f, -boy / 2f - 7f * birim) {
                                    with(motor) {
                                        draw(
                                            Size(boy, boy),
                                            alpha = gorunurluk,
                                            colorFilter = ColorFilter.tint(surucuRengi)
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            },
        contentAlignment = Alignment.Center
    ) {
        MotorumLogosu(
            yolRengi = Murekkep,
            seritRengi = Kenar,
            boyut = 96.dp,
            // Isaret kutunun ortasinin 22 dp ustunde.
            modifier = Modifier.padding(bottom = 44.dp),
            cizilen = { aralik(an(), 0f, 0.32f, CIZIM_EGRISI) },
            seritFazi = { seritFazi }
        )
    }
}

// Zamanin [bas, son] araligindaki payi, egriden gecirilmis olarak (0..1).
private fun aralik(t: Float, bas: Float, son: Float, egri: Easing): Float =
    egri.transform(((t - bas) / (son - bas)).coerceIn(0f, 1f))

private const val DONGU_MS = 4600
private const val SERIT_MS = 450
private val CIZIM_EGRISI = CubicBezierEasing(0.45f, 0.05f, 0.2f, 1f)
private val SURUS_EGRISI = CubicBezierEasing(0.4f, 0f, 0.6f, 1f)
private val CIKIS_EGRISI = CubicBezierEasing(0f, 0f, 0.58f, 1f)
private const val SAHNE_YOLU = "M8 150 C 62 132 132 160 182 140"

// ------------------------------------------------------------ 2. KAYITLARINI TUT
//
// Dort kategori kutusu, kodun kendi renkleriyle. Sayfa her acildiginda
// sirayla (100 ms arayla) yukaridan dusup yerine oturuyorlar.
@Composable
fun KategorilerSahnesi(gorunur: Boolean, modifier: Modifier = Modifier) {
    val kutular = listOf(
        KategoriKutusu("Yakıt", MotorumIkonlari.Yakit, YakitZemin, YakitMetin),
        KategoriKutusu("Bakım", MotorumIkonlari.Bakim, BakimZemin, BakimMetin),
        KategoriKutusu("Road Trip", MotorumIkonlari.RoadTrip, RoadTripZemin, RoadTripMetin),
        KategoriKutusu("Aksesuar", MotorumIkonlari.Aksesuar, AksesuarZemin, AksesuarMetin)
    )
    Column(modifier.width(196.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
        kutular.chunked(2).forEachIndexed { satir, ikili ->
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                ikili.forEachIndexed { sutun, kutu ->
                    KategoriKutusuGorunumu(
                        kutu = kutu,
                        sira = satir * 2 + sutun,
                        gorunur = gorunur,
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}

private data class KategoriKutusu(
    val ad: String,
    val ikon: ImageVector,
    val zemin: Color,
    val renk: Color
)

@Composable
private fun KategoriKutusuGorunumu(
    kutu: KategoriKutusu,
    sira: Int,
    gorunur: Boolean,
    modifier: Modifier = Modifier
) {
    val onizleme = LocalInspectionMode.current
    val ilerleme = remember { Animatable(if (onizleme) 1f else 0f) }
    // Sayfaya her gelindiginde bastan oynuyor; sayfa ekrandan cikinca sifirlaniyor.
    LaunchedEffect(gorunur) {
        if (onizleme) return@LaunchedEffect
        if (gorunur) {
            delay(80L + sira * 100L)
            ilerleme.animateTo(1f, tween(700, easing = AppMotion.egri))
        } else {
            ilerleme.snapTo(0f)
        }
    }
    Column(
        modifier = modifier
            .graphicsLayer {
                translationY = (1f - ilerleme.value) * -6.dp.toPx()
                alpha = 0.4f + 0.6f * ilerleme.value
            }
            .clip(RoundedCornerShape(18.dp))
            .background(kutu.zemin)
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Icon(kutu.ikon, contentDescription = null, tint = kutu.renk, modifier = Modifier.size(26.dp))
        Text(kutu.ad, fontSize = 11.sp, fontWeight = FontWeight.ExtraBold, color = kutu.renk)
    }
}

// ------------------------------------------------------------ 3. TOPLAMLARI GOR
//
// Ana sayfadaki ozet kartin kucugu: kullanici bir sonraki ekranda aynisini
// goruyor. Rakamlar ornek. Kategori cubugu sayfa acilinca soldan uzuyor.
@Composable
fun ToplamlarSahnesi(gorunur: Boolean, modifier: Modifier = Modifier) {
    val onizleme = LocalInspectionMode.current
    val uzama = remember { Animatable(if (onizleme) 1f else 0f) }
    LaunchedEffect(gorunur) {
        if (onizleme) return@LaunchedEffect
        if (gorunur) {
            delay(300)
            uzama.animateTo(1f, tween(900, easing = AppMotion.egri))
        } else {
            uzama.snapTo(0f)
        }
    }
    val sekil = RoundedCornerShape(20.dp)
    val golge = if (karanlikTema) Color.Black else Color(0xFF0F172A)
    val parcalar = listOf(47f to YakitRenk, 28f to BakimRenk, 16f to RoadTripRenk, 9f to AksesuarRenk)

    Column(
        modifier = modifier
            .width(212.dp)
            .shadow(16.dp, sekil, ambientColor = golge, spotColor = golge)
            .background(KartZemin, sekil)
            .padding(16.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
            KucukEtiket("TOPLAM HARCAMA")
            Text(
                "₺18.780", fontSize = 25.sp, fontWeight = FontWeight.ExtraBold,
                color = MetinAna, letterSpacing = (-1).sp
            )
        }
        Row(
            Modifier
                .fillMaxWidth()
                .height(8.dp)
                // Uzama sadece cizim katmaninda; sol kenar sabit kaliyor.
                .graphicsLayer {
                    scaleX = uzama.value
                    transformOrigin = TransformOrigin(0f, 0.5f)
                }
                .clip(RoundedCornerShape(50)),
            horizontalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            parcalar.forEach { (pay, renk) ->
                Box(Modifier.weight(pay).fillMaxHeight().background(renk))
            }
        }
        Box(Modifier.fillMaxWidth().height(1.dp).background(CizgiSolgun))
        // IntrinsicSize.Min: aradaki dikey cizgi iki sutunun boyu kadar olsun.
        Row(Modifier.height(IntrinsicSize.Min), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
            OzetDegeri("YAKIT", "336,4", " L", Modifier.weight(1f))
            Box(Modifier.width(1.dp).fillMaxHeight().background(CizgiSolgun))
            OzetDegeri("MESAFE", "780", " km", Modifier.weight(1f))
        }
    }
}

@Composable
private fun KucukEtiket(metin: String) {
    Text(metin, fontSize = 9.sp, fontWeight = FontWeight.Bold, letterSpacing = 0.7.sp, color = MetinSolgun)
}

@Composable
private fun OzetDegeri(etiket: String, deger: String, birim: String, modifier: Modifier = Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(3.dp)) {
        KucukEtiket(etiket)
        Text(
            text = buildAnnotatedString {
                append(deger)
                withStyle(SpanStyle(fontSize = 10.sp, color = MetinIkincil)) { append(birim) }
            },
            fontSize = 15.sp,
            fontWeight = FontWeight.ExtraBold,
            letterSpacing = (-0.4).sp,
            color = MetinAna
        )
    }
}

@Preview(showBackground = true, widthDp = 260, heightDp = 720)
@Composable
private fun SahnelerPreview() {
    MotorumTheme(karanlik = false) {
        Column(
            Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            HosGeldinSahnesi()
            KategorilerSahnesi(gorunur = true)
            ToplamlarSahnesi(gorunur = true)
        }
    }
}

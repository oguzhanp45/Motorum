package com.oguzhanp.motorum.feature.acilis

import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.EaseInOut
import androidx.compose.animation.core.Easing
import androidx.compose.animation.core.LinearEasing
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oguzhanp.motorum.core.tasarim.AcikDurumCubugu
import com.oguzhanp.motorum.core.tasarim.MotorumLogosu
import com.oguzhanp.motorum.core.tasarim.MotorumTheme

// Acilis animasyonu (tasarim: Acilis, Acilis Akisi).
//
// Sistem acilis ekrani (themes.xml) murekkep zeminde beyaz bir M gosteriyor.
// Bu ekranin ilk karesi onunla birebir ayni, yani devir teslim fark edilmiyor.
// Sonra sirayla:
//   1. Serit cizgisi M'nin icinde bastan sona cizilir.
//   2. Iki alt uc arasina kesikli bir cizgi uzanir: yol devam ediyor.
//   3. M sola kayar, "otorum" sagdan gelir; uc cizgisi soner, serit kalir.
// Tek seferlik; bitince onBitti cagriliyor ve perde kalkiyor.
//
// hazir: tema ve tanitim bilgisi diskten okundu mu. Okunana kadar sistem
// acilis ekrani ustte duruyor; animasyon onun arkasinda baslamasin diye bekliyoruz.
@Composable
fun AcilisEkrani(
    hazir: Boolean,
    onBitti: () -> Unit,
    modifier: Modifier = Modifier
) {
    AcikDurumCubugu()

    val onizleme = LocalInspectionMode.current
    // Tum sahne tek bir zamandan (0..1) hesaplaniyor; parcalar birbirinden kaymiyor.
    val zaman = remember { Animatable(if (onizleme) 1f else 0f) }
    LaunchedEffect(hazir) {
        if (!hazir || onizleme) return@LaunchedEffect
        zaman.animateTo(1f, tween(TOPLAM_MS, easing = LinearEasing))
        onBitti()
    }
    val t = { zaman.value }

    // Arkadaki soluk parilti yavasca nefes aliyor.
    val nefesDongusu = rememberInfiniteTransition(label = "nefes")
    val nefes by nefesDongusu.animateFloat(
        initialValue = 0f, targetValue = 1f,
        animationSpec = infiniteRepeatable(tween(NEFES_MS, easing = EaseInOut), RepeatMode.Reverse),
        label = "nefes"
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(ACILIS_ZEMINI)
            .drawBehind {
                // Ilk karede yok (sistem ekraninda da yok), sonra beliriyor.
                val gorunur = aralik(t(), 0f, 0.3f, LinearEasing)
                if (gorunur <= 0f) return@drawBehind
                val yaricap = 150.dp.toPx() * (1f + 0.06f * nefes)
                drawCircle(
                    brush = Brush.radialGradient(
                        0f to PARILTI, 0.68f to Color.Transparent,
                        center = center, radius = yaricap
                    ),
                    radius = yaricap,
                    alpha = gorunur * (0.5f + 0.25f * nefes)
                )
            },
        contentAlignment = Alignment.Center
    ) {
        Row(
            // Baslangicta M tam ortada (sistem ikonunun yerinde); sonunda
            // "M + otorum" birlikte ortada. Kayma sadece cizim katmaninda.
            modifier = Modifier.graphicsLayer {
                val kayma = aralik(t(), KAYMA_BAS, KAYMA_SON, KAYMA_EGRISI)
                translationX = (size.width - M_BOYU.toPx()) / 2f * (1f - kayma)
            },
            verticalAlignment = Alignment.CenterVertically
        ) {
            MotorumLogosu(
                yolRengi = LOGO_RENGI,
                seritRengi = SERIT_RENGI,
                boyut = M_BOYU,
                seritCizilen = { aralik(t(), 0f, SERIT_SON, CIZIM_EGRISI) },
                baglantiCizilen = { aralik(t(), BAGLANTI_BAS, BAGLANTI_SON, CIZIM_EGRISI) },
                // Isim gelirken uc cizgisi soner.
                baglantiSaydamligi = { 1f - aralik(t(), KAYMA_BAS + 0.05f, KAYMA_SON, LinearEasing) }
            )
            Text(
                text = "otorum",
                style = MaterialTheme.typography.displaySmall,
                fontSize = 49.sp,
                fontWeight = FontWeight.ExtraBold,
                letterSpacing = (-2).sp,
                color = LOGO_RENGI,
                modifier = Modifier
                    // M'nin sag kenarindaki bosluga biraz giriyor: "Motorum" tek kelime okunsun.
                    .offset(x = (-7).dp)
                    .graphicsLayer {
                        val gelis = aralik(t(), KAYMA_BAS, KAYMA_SON, KAYMA_EGRISI)
                        alpha = gelis
                        translationX = (1f - gelis) * 20.dp.toPx()
                    }
            )
        }
    }
}

// Zamanin [bas, son] araligindaki payi, egriden gecirilmis olarak (0..1).
private fun aralik(t: Float, bas: Float, son: Float, egri: Easing): Float =
    egri.transform(((t - bas) / (son - bas)).coerceIn(0f, 1f))

// Sure ve asamalar (toplam 1.7 sn):
// 0-0.7 sn serit, 0.6-1.0 uc cizgisi, 1.0-1.5 isim, 1.5-1.7 bekleme.
private const val TOPLAM_MS = 1700
private const val SERIT_SON = 0.41f
private const val BAGLANTI_BAS = 0.35f
private const val BAGLANTI_SON = 0.59f
private const val KAYMA_BAS = 0.59f
private const val KAYMA_SON = 0.88f
private const val NEFES_MS = 3000

private val CIZIM_EGRISI = CubicBezierEasing(0.45f, 0.05f, 0.2f, 1f)
private val KAYMA_EGRISI = CubicBezierEasing(0.25f, 0.9f, 0.25f, 1f)

// Sistem acilis ekraniyla ayni olmali: values/colors.xml -> acilis_zemini,
// drawable/acilis_isareti.xml. Temaya bagli degil, iki temada da murekkep.
private val ACILIS_ZEMINI = Color(0xFF111827)
private val LOGO_RENGI = Color(0xFFF9FAFB)
private val SERIT_RENGI = Color(0xFF4B5563)
private val PARILTI = Color(0x2994A3B8)
// Sistem ikonundaki M'nin boyu (288 dp kutunun ortasinda 96 dp).
private val M_BOYU = 96.dp

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun AcilisEkraniPreview() {
    MotorumTheme { AcilisEkrani(hazir = true, onBitti = {}) }
}

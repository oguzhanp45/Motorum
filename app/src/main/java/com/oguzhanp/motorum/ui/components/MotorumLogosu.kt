package com.oguzhanp.motorum.ui.components

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathMeasure
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.drawscope.scale
import androidx.compose.ui.graphics.vector.PathParser
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

// Uygulamanin "M" isareti: kalin bir yol, ortasinda kesik serit cizgisi.
// Giris ekraninda, tanitimda ve acilista ayni cizim kullaniliyor; o yuzden
// ikon setinde degil, kendi dosyasinda. Iki renkli oldugu icin tek renkli
// ImageVector (tint) ile cizilemiyor.
//
// cizilen: isaretin ne kadari cizili (0..1); tanitimda bastan sona cizilerek
// beliriyor. seritFazi: kesik cizginin kaymasi (0..1, bir desen boyu).
// seritCizilen: seridin ne kadari cizili; verilmezse isaretle birlikte.
// baglantiCizilen / baglantiSaydamligi: iki alt uc arasindaki kesikli cizgi
// (sadece acilista; yolun devam ettigini anlatiyor).
// Hepsi deger degil fonksiyon: her karede degisiyorlar ve sadece cizim
// sirasinda okunuyorlar, boylece logo her karede yeniden kurulmuyor.
@Composable
fun MotorumLogosu(
    yolRengi: Color,
    seritRengi: Color,
    modifier: Modifier = Modifier,
    boyut: Dp = 76.dp,
    cizilen: () -> Float = { 1f },
    seritFazi: () -> Float = { 0f },
    seritCizilen: () -> Float = cizilen,
    baglantiCizilen: () -> Float = { 0f },
    baglantiSaydamligi: () -> Float = { 1f }
) {
    // Yol bir kez ayristiriliyor, her karede degil.
    val yol = remember { PathParser().parsePathString(LOGO_YOLU).toPath() }
    val olcu = remember { PathMeasure().apply { setPath(yol, false) } }
    val kesit = remember { Path() }
    val seritKesiti = remember { Path() }
    val baglanti = remember { Path() }
    Canvas(modifier.size(boyut)) {
        val oran = cizilen()
        if (oran <= 0f) return@Canvas
        // Yolun cizilen kismi kadarini aliyoruz; serit de ayni sekilde
        // kesildigi icin cizilmemis yerde gorunmuyor.
        val cizim = kesitAl(yol, olcu, oran, kesit)
        val serit = kesitAl(yol, olcu, seritCizilen(), seritKesiti)
        // Cizim 64'luk kutuda tasarlandi; kutuyu istenen boyuta olcekliyoruz.
        scale(size.width / KUTU, pivot = Offset.Zero) {
            drawPath(
                cizim, yolRengi,
                style = Stroke(7f, cap = StrokeCap.Round, join = StrokeJoin.Round)
            )
            drawPath(
                serit, seritRengi,
                style = Stroke(
                    1.6f, cap = StrokeCap.Round, join = StrokeJoin.Round,
                    pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 7f), seritFazi() * 12f)
                )
            )
            // Alt uclari birlestiren cizgi soldan saga uzaniyor.
            val uzunluk = baglantiCizilen()
            if (uzunluk > 0f) {
                baglanti.reset()
                baglanti.moveTo(12f, 51f)
                baglanti.lineTo(12f + 36f * uzunluk, 51f)
                drawPath(
                    baglanti, seritRengi, alpha = baglantiSaydamligi(),
                    style = Stroke(
                        1.6f, cap = StrokeCap.Round,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5f, 7f))
                    )
                )
            }
        }
    }
}

// Yolun bastan "oran" kadarlik parcasi. Tamamiysa yolun kendisi.
private fun kesitAl(yol: Path, olcu: PathMeasure, oran: Float, hedef: Path): Path {
    if (oran >= 1f) return yol
    hedef.reset()
    if (oran > 0f) olcu.getSegment(0f, olcu.length * oran, hedef, true)
    return hedef
}

private const val KUTU = 64f
private const val LOGO_YOLU =
    "M12 51V24.5c0-6.2 9-6.2 9 0v11.8c0 6.2 9 6.2 9 0V24.5c0-6.2 9-6.2 9 0v11.8c0 6.2 9 6.2 9 0V51"

@Preview(showBackground = true, backgroundColor = 0xFF1F2937)
@Composable
private fun MotorumLogosuPreview() {
    MotorumLogosu(yolRengi = Color(0xFFF9FAFB), seritRengi = Color(0xFF4B5563))
}

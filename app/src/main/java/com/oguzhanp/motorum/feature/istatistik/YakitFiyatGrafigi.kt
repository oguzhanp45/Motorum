package com.oguzhanp.motorum.feature.istatistik

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.StrokeJoin
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.unit.dp
import com.oguzhanp.motorum.core.tasarim.YakitRenk

@Composable
fun YakitFiyatGrafigi(
    noktalar: List<YakitNoktasi>,
    modifier: Modifier = Modifier
) {
    // Iki noktadan azi cizgi olmaz. Cagiran taraf da kontrol ediyor, burasi
    // bilesenin kendi guvencesi.
    if (noktalar.size < 2) return

    // Canvas'in icinde MaterialTheme okunamiyor, rengi disarida aliyoruz:
    // son noktanin ici kart zemini olsun ki karanlik temada beyaz leke kalmasin.
    val kartZemin = MaterialTheme.colorScheme.surface
    // Tema renkleri Canvas'in icinde okunamiyor (cizim composable degil); disarida alip iceri veriyoruz.
    val cizgiRengi = YakitRenk

    Canvas(
        modifier = modifier
            .fillMaxWidth()
            .height(120.dp)
    ) {
        val enYuksek = noktalar.maxOf { it.birimFiyat }
        val enDusuk = noktalar.minOf { it.birimFiyat }
        val aralik = enYuksek - enDusuk

        val pay = 10.dp.toPx()
        val kullanilir = size.height - pay * 2
        // Nokta yatayda esit araliklarla duruyor: eksen zaman degil, dolum sirasi.
        // Zaman ekseni olsaydi ust uste gelen iki dolum tek cizgiye binerdi.
        val adim = size.width / (noktalar.size - 1)

        // Butun dolumlar ayni fiyattaysa aralik sifir: bolme yapmadan
        // cizgiyi ortadan geciriyoruz.
        fun yEkseni(fiyat: Double): Float =
            if (aralik <= 0.0) size.height / 2f
            else pay + ((enYuksek - fiyat) / aralik).toFloat() * kullanilir

        val yol = Path()
        noktalar.forEachIndexed { sira, nokta ->
            val x = sira * adim
            val y = yEkseni(nokta.birimFiyat)
            if (sira == 0) yol.moveTo(x, y) else yol.lineTo(x, y)
        }

        // Cizginin altini dolduran ikinci yol: ayni yol asagi kapatiliyor.
        val dolgu = Path().apply {
            addPath(yol)
            lineTo(size.width, size.height)
            lineTo(0f, size.height)
            close()
        }
        drawPath(
            path = dolgu,
            brush = Brush.verticalGradient(
                listOf(cizgiRengi.copy(alpha = 0.18f), Color.Transparent)
            )
        )

        drawPath(
            path = yol,
            color = cizgiRengi,
            style = Stroke(
                width = 2.5.dp.toPx(),
                cap = StrokeCap.Round,
                join = StrokeJoin.Round
            )
        )

        noktalar.forEachIndexed { sira, nokta ->
            val x = sira * adim
            val y = yEkseni(nokta.birimFiyat)
            // Son dolum ici bos buyuk nokta: "en guncel fiyat burasi" demek.
            if (sira == noktalar.lastIndex) {
                drawCircle(color = cizgiRengi, radius = 5.dp.toPx(), center = Offset(x, y))
                drawCircle(color = kartZemin, radius = 2.2.dp.toPx(), center = Offset(x, y))
            } else {
                drawCircle(color = cizgiRengi, radius = 2.5.dp.toPx(), center = Offset(x, y))
            }
        }
    }
}

package com.oguzhanp.motorum.ui.ekle.components

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.PathEffect
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oguzhanp.motorum.R
import com.oguzhanp.motorum.ui.components.MotorumIkonlari
import com.oguzhanp.motorum.ui.theme.Inter
import com.oguzhanp.motorum.ui.theme.KartZemin
import com.oguzhanp.motorum.ui.theme.Kenar
import com.oguzhanp.motorum.ui.theme.MetinAna
import com.oguzhanp.motorum.ui.theme.MetinSolgun
import com.oguzhanp.motorum.ui.theme.MotorumTheme

// "Son kayittan doldur" seridi (tasarim: Fikir 2A). Kesikli cerceve: bir alan
// degil, bir oneri oldugunu soyluyor. Dokununca alanlar doluyor, tarih bugun kaliyor.
@Composable
fun SonKayitSeridi(
    ozet: String,
    onTikla: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sekil = RoundedCornerShape(14.dp)
    val cerceve = Kenar
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(sekil)
            .background(KartZemin)
            // Kesikli cizgi icin hazir border yok; cerceveyi kendimiz ciziyoruz.
            .drawBehind {
                val kalinlik = 1.5.dp.toPx()
                drawRoundRect(
                    color = cerceve,
                    topLeft = Offset(kalinlik / 2, kalinlik / 2),
                    size = Size(size.width - kalinlik, size.height - kalinlik),
                    cornerRadius = CornerRadius(14.dp.toPx()),
                    style = Stroke(
                        width = kalinlik,
                        pathEffect = PathEffect.dashPathEffect(floatArrayOf(5.dp.toPx(), 4.dp.toPx()))
                    )
                )
            }
            .clickable(onClick = onTikla)
            .padding(horizontal = 13.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(11.dp)
    ) {
        Icon(MotorumIkonlari.Yenile, contentDescription = null, tint = MetinAna, modifier = Modifier.size(17.dp))
        Column(verticalArrangement = Arrangement.spacedBy(2.dp)) {
            Text(
                text = stringResource(R.string.son_kayittan_doldur),
                style = TextStyle(fontFamily = Inter, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold),
                color = MetinAna
            )
            Text(
                text = ozet,
                style = TextStyle(fontFamily = Inter, fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                color = MetinSolgun,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SonKayitSeridiPreview() {
    MotorumTheme {
        SonKayitSeridi(ozet = "Shell V-Power · 12,40 L · 580,00 ₺", onTikla = {}, modifier = Modifier.padding(16.dp))
    }
}

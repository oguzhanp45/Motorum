package com.oguzhanp.motorum.feature.anasayfa

import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oguzhanp.motorum.R
import com.oguzhanp.motorum.core.tasarim.AppElevation
import com.oguzhanp.motorum.core.tasarim.AppShape
import com.oguzhanp.motorum.core.tasarim.AppSpacing
import com.oguzhanp.motorum.core.tasarim.MetinIkincil
import com.oguzhanp.motorum.core.tasarim.MetinSolgun
import com.oguzhanp.motorum.core.tasarim.MotorumIkonlari
import com.oguzhanp.motorum.core.tasarim.MotorumTheme
import com.oguzhanp.motorum.core.tasarim.basilincaKucul
import com.oguzhanp.motorum.core.util.formatKm
import com.oguzhanp.motorum.core.util.formatLitre
import com.oguzhanp.motorum.core.util.formatTl

@Composable
fun ToplamCard(
    toplamTutar: Double,
    toplamLitre: Double,
    gidilenYol: Int,
    onTikla: () -> Unit,
    modifier: Modifier = Modifier
) {
    // Card'in onClick alan surumu kullaniliyor: dokunma dalgasi kartin
    // kosesine kadar dogru cikiyor, disina Modifier.clickable sarmaya gerek yok.
    // Basilinca kart hafifce kuculuyor ve golgesi iniyor (tasarim: Dokunma).
    val etkilesim = remember { MutableInteractionSource() }
    Card(
        onClick = onTikla,
        interactionSource = etkilesim,
        shape = AppShape.kart,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.kart, pressedElevation = 0.dp),
        modifier = Modifier.basilincaKucul(etkilesim).then(modifier)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(AppSpacing.kartIci),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.normal)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    text = stringResource(R.string.genel_istatistikler),
                    style = MaterialTheme.typography.labelMedium,
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 0.8.sp,
                    color = MetinSolgun
                )
                Box(modifier = Modifier.weight(1f))
                // Ok, kartin tiklanabildigini soyleyen tek isaret: basligi
                // buyutmeden ya da buton koymadan detay sayfasina isaret ediyor.
                Icon(
                    imageVector = MotorumIkonlari.Cevron,
                    contentDescription = stringResource(R.string.istatistik_detayi),
                    tint = MetinSolgun,
                    modifier = Modifier.size(20.dp)
                )
            }

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Metrik(
                    baslik = stringResource(R.string.toplam_tutar),
                    deger = formatTl(toplamTutar),
                    modifier = Modifier.weight(1f)
                )
                Ayirici()
                Metrik(
                    baslik = stringResource(R.string.toplam_litre),
                    deger = formatLitre(toplamLitre),
                    modifier = Modifier.weight(1f)
                )
                Ayirici()
                Metrik(
                    baslik = stringResource(R.string.gidilen_yol),
                    deger = formatKm(gidilenYol),
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
private fun Metrik(
    baslik: String,
    deger: String,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        Text(
            text = baslik,
            style = MaterialTheme.typography.bodySmall,
            color = MetinIkincil
        )
        Text(
            text = deger,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun Ayirici() {
    VerticalDivider(
        color = MaterialTheme.colorScheme.outlineVariant,
        modifier = Modifier
            .padding(horizontal = AppSpacing.orta)
            .width(1.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun ToplamCardPreview() {
    MotorumTheme {
        ToplamCard(
            toplamTutar = 5000.0,
            toplamLitre = 12.0,
            gidilenYol = 532,
            onTikla = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

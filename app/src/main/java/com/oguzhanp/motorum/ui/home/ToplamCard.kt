package com.oguzhanp.motorum.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.VerticalDivider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oguzhanp.motorum.ui.theme.MetinIkincil
import com.oguzhanp.motorum.ui.theme.MetinSolgun
import com.oguzhanp.motorum.ui.theme.MotorumTheme
import com.oguzhanp.motorum.util.formatKm
import com.oguzhanp.motorum.util.formatLitre
import com.oguzhanp.motorum.util.formatTl

@Composable
fun ToplamCard(
    toplamTutar: Double,
    toplamLitre: Double,
    toplamKm: Int,
    modifier: Modifier = Modifier
) {
    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp),
        modifier = modifier
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Text(
                text = "GENEL İSTATİSTİKLER",
                style = MaterialTheme.typography.labelMedium,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp,
                color = MetinSolgun
            )

            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(IntrinsicSize.Min),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Metrik(
                    baslik = "Toplam Tutar",
                    deger = formatTl(toplamTutar),
                    modifier = Modifier.weight(1f)
                )
                Ayirici()
                Metrik(
                    baslik = "Toplam Litre",
                    deger = formatLitre(toplamLitre),
                    modifier = Modifier.weight(1f)
                )
                Ayirici()
                Metrik(
                    baslik = "Toplam Km",
                    deger = formatKm(toplamKm),
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
            .padding(horizontal = 12.dp)
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
            toplamKm = 532,
            modifier = Modifier.padding(16.dp)
        )
    }
}

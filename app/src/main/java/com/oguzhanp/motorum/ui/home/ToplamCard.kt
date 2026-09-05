package com.oguzhanp.motorum.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oguzhanp.motorum.ui.theme.MotorumTheme
import com.oguzhanp.motorum.util.formatKm
import com.oguzhanp.motorum.util.formatLitre
import com.oguzhanp.motorum.util.formatTl

// Uc toplam esit onemde, o yuzden ayni yazi boyutunda.
// headlineSmall yerine titleMedium: uc rakam dar ekranda yan yana sigsin.
@Composable
fun ToplamCard(
    toplamTutar: Double,
    toplamLitre: Double,
    toplamKm: Int,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text("Toplam Tutar", style = MaterialTheme.typography.labelMedium)
                Text(formatTl(toplamTutar), style = MaterialTheme.typography.titleMedium)
            }
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text("Toplam Litre", style = MaterialTheme.typography.labelMedium)
                Text(formatLitre(toplamLitre), style = MaterialTheme.typography.titleMedium)
            }
            Column(horizontalAlignment = Alignment.End) {
                Text("Toplam Km", style = MaterialTheme.typography.labelMedium)
                Text(formatKm(toplamKm), style = MaterialTheme.typography.titleMedium)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ToplamCardPreview() {
    MotorumTheme {
        ToplamCard(
            toplamTutar = 4250.75,
            toplamLitre = 88.4,
            toplamKm = 1240,
            modifier = Modifier.padding(16.dp)
        )
    }
}

package com.oguzhanp.motorum.ui.home

import androidx.compose.foundation.clickable
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
import com.oguzhanp.motorum.model.Kategori
import com.oguzhanp.motorum.model.Kayit
import com.oguzhanp.motorum.ui.theme.MotorumTheme
import com.oguzhanp.motorum.util.formatLitre
import com.oguzhanp.motorum.util.formatTarih
import com.oguzhanp.motorum.util.formatTl

// LazyColumn'un tek bir satiri.
@Composable
fun KayitSatiri(
    kayit: Kayit,
    onTikla: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(modifier = modifier.clickable { onTikla() }) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Column {
                    Text(kayit.kategori.etiket, style = MaterialTheme.typography.titleSmall)
                    Text(formatTarih(kayit.tarihMillis), style = MaterialTheme.typography.bodySmall)
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(formatTl(kayit.tutar), style = MaterialTheme.typography.titleSmall)
                    Text(formatLitre(kayit.litre), style = MaterialTheme.typography.bodySmall)
                }
            }
            // Not bos degilse ikinci satir olarak goster
            if (kayit.not.isNotBlank()) {
                Text(
                    text = kayit.not,
                    style = MaterialTheme.typography.bodySmall,
                    modifier = Modifier.padding(top = 6.dp)
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun KayitSatiriPreview() {
    MotorumTheme {
        KayitSatiri(
            kayit = Kayit(
                kategori = Kategori.YAKIT,
                tarihMillis = System.currentTimeMillis(),
                litre = 12.5,
                tutar = 620.0,
                not = "Shell, tam depo"
            ),
            onTikla = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

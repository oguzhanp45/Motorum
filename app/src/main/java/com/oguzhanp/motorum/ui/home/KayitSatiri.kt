package com.oguzhanp.motorum.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SwipeToDismissBox
import androidx.compose.material3.SwipeToDismissBoxValue
import androidx.compose.material3.Text
import androidx.compose.material3.rememberSwipeToDismissBoxState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oguzhanp.motorum.model.Kayit
import com.oguzhanp.motorum.ui.theme.MotorumTheme
import com.oguzhanp.motorum.util.formatLitre
import com.oguzhanp.motorum.util.formatTarih
import com.oguzhanp.motorum.util.formatTl

// LazyColumn'un tek bir satiri.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KayitSatiri(
    kayit: Kayit,
    onTikla: () -> Unit,
    onSil: () -> Unit,
    modifier: Modifier = Modifier
) {
    // positionalThreshold: satir genisliginin %75'i kadar cekilmeden silinmez.
    // Kaza sonucu tetiklenmeyi engelleyen tek ayar bu.
    val kaydirmaDurumu = rememberSwipeToDismissBoxState(
        positionalThreshold = { toplamGenislik -> toplamGenislik * 0.85f }
    )

    // Kaydirma oturdugunda currentValue degisir, bu blok bir kez calisir.
    // Yon kontrolu zaten enableDismissFromStartToEnd = false ile yapiliyor.
    LaunchedEffect(kaydirmaDurumu.currentValue) {
        if (kaydirmaDurumu.currentValue == SwipeToDismissBoxValue.EndToStart) {
            onSil()
        }
    }

    SwipeToDismissBox(
        state = kaydirmaDurumu,
        modifier = modifier,
        enableDismissFromStartToEnd = false,    // Saga kaydirma tamamen kapali.
        enableDismissFromEndToStart = true,
        backgroundContent = {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .clip(CardDefaults.shape)
                    .background(MaterialTheme.colorScheme.errorContainer),
                contentAlignment = Alignment.CenterEnd
            ) {
                Icon(
                    imageVector = Icons.Default.Delete,
                    contentDescription = "Sil",
                    tint = MaterialTheme.colorScheme.onErrorContainer,
                    modifier = Modifier.padding(end = 16.dp)
                )
            }
        }
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { onTikla() }
        ) {
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
                        // litre artik Kayit arayuzunde yok, sadece Yakit'te.
                        // Erisebilmek icin tip daraltiyoruz.
                        when (kayit) {
                            is Kayit.Yakit ->
                                Text(formatLitre(kayit.litre), style = MaterialTheme.typography.bodySmall)
                        }
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
}

@Preview(showBackground = true)
@Composable
private fun KayitSatiriPreview() {
    MotorumTheme {
        KayitSatiri(
            kayit = Kayit.Yakit(
                tarihMillis = System.currentTimeMillis(),
                litre = 12.5,
                tutar = 620.0,
                not = "Shell, tam depo"
            ),
            onTikla = {},
            onSil = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

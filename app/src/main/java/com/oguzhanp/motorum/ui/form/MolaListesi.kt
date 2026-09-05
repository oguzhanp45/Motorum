package com.oguzhanp.motorum.ui.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oguzhanp.motorum.model.Mola
import com.oguzhanp.motorum.ui.ekle.components.SaatSecici
import com.oguzhanp.motorum.ui.theme.MotorumTheme

// Bir noktanin mola satirlari. Liste degismez: her islem yeni bir liste uretip
// onDegis ile yukari veriyor, mevcut liste hic elden gecirilmiyor.
// LazyColumn kullanilmiyor: form zaten dikey kayan bir Column icinde, ic ice
// dikey kaydirma Compose'da sonsuz yukseklik hatasi verir.
@Composable
fun MolaListesi(
    molalar: List<Mola>,
    onDegis: (List<Mola>) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Text("Molalar", style = MaterialTheme.typography.titleSmall)

        molalar.forEachIndexed { sira, mola ->
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedTextField(
                    value = mola.isim,
                    // mapIndexed: sadece bu siradaki mola degisiyor, digerleri oldugu gibi kopyalaniyor.
                    onValueChange = { yeni ->
                        onDegis(
                            molalar.mapIndexed { i, m ->
                                if (i == sira) m.copy(isim = yeni) else m
                            }
                        )
                    },
                    label = { Text("Yer") },
                    singleLine = true,
                    modifier = Modifier.weight(1f)
                )

                SaatSecici(
                    saat = mola.saat,
                    dakika = mola.dakika,
                    onSaatSec = { s, d ->
                        onDegis(
                            molalar.mapIndexed { i, m ->
                                if (i == sira) m.copy(saat = s, dakika = d) else m
                            }
                        )
                    },
                    modifier = Modifier.width(120.dp)
                )

                // filterIndexed: silinen sira disindaki her sey yeni listeye gecer.
                IconButton(onClick = { onDegis(molalar.filterIndexed { i, _ -> i != sira }) }) {
                    Icon(Icons.Default.Delete, contentDescription = "Molayı sil")
                }
            }
        }

        TextButton(onClick = { onDegis(molalar + Mola()) }) {
            Icon(Icons.Default.Add, contentDescription = null)
            Text("Mola ekle", modifier = Modifier.padding(start = 4.dp))
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MolaListesiPreview() {
    MotorumTheme {
        MolaListesi(
            molalar = listOf(Mola("Bolu", 12, 30), Mola()),
            onDegis = {}
        )
    }
}

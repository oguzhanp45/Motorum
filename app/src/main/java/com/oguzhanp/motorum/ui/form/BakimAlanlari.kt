package com.oguzhanp.motorum.ui.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oguzhanp.motorum.R
import com.oguzhanp.motorum.ui.ekle.components.SaatSecici
import com.oguzhanp.motorum.ui.ekle.components.TarihSecici
import com.oguzhanp.motorum.ui.theme.MetinIkincil
import com.oguzhanp.motorum.ui.theme.MotorumTheme

// Bakim kategorisinin form alanlari. Hem ekleme hem detay ekrani ayni blogu cagiriyor.
@Composable
fun BakimAlanlari(
    form: KayitFormu.Bakim,
    onDegis: (KayitFormu.Bakim) -> Unit,
    bildirimIzniVar: Boolean,
    onHatirlatmaAcilsin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TarihSecici(
            tarihMillis = form.tarihMillis,
            onTarihSec = { onDegis(form.copy(tarihMillis = it)) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = form.bakimTuru,
            onValueChange = { onDegis(form.copy(bakimTuru = it, bakimTuruHatali = false)) },
            label = { Text("Bakım türü") },
            isError = form.bakimTuruHatali,
            supportingText = { if (form.bakimTuruHatali) Text(stringResource(R.string.zorunlu_alan)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = form.tutarYazi,
            onValueChange = { onDegis(form.copy(tutarYazi = it, tutarHatali = false)) },
            label = { Text("Tutar (₺)") },
            isError = form.tutarHatali,
            supportingText = { if (form.tutarHatali) Text(stringResource(R.string.gecerli_sayi)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "Bakım zamanı hatırlat",
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Switch(
                checked = form.hatirlatmaAcik,
                // Acarken izin akisi devreye giriyor, o yuzden karari ekrana
                // biraktik; alani acmak da onun isi. Kapatmak izin gerektirmiyor.
                onCheckedChange = { acik ->
                    if (acik) {
                        onHatirlatmaAcilsin()
                    } else {
                        onDegis(form.copy(hatirlatmaAcik = false, hatirlatmaHatali = false))
                    }
                }
            )
        }

        if (form.hatirlatmaAcik) {
            TarihSecici(
                tarihMillis = form.hatirlatmaTarihMillis,
                onTarihSec = {
                    onDegis(form.copy(hatirlatmaTarihMillis = it, hatirlatmaHatali = false))
                },
                etiket = "Hatırlatma tarihi",
                modifier = Modifier.fillMaxWidth()
            )

            SaatSecici(
                saat = form.hatirlatmaSaat,
                dakika = form.hatirlatmaDakika,
                onSaatSec = { saat, dakika ->
                    onDegis(
                        form.copy(
                            hatirlatmaSaat = saat,
                            hatirlatmaDakika = dakika,
                            hatirlatmaHatali = false
                        )
                    )
                },
                etiket = "Hatırlatma saati",
                modifier = Modifier.fillMaxWidth()
            )

            if (form.hatirlatmaHatali) {
                Text(
                    text = "Hatırlatma için gelecekte bir tarih ve saat seç",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            // Izin yoksa tarih yine kaydediliyor ve kartta gorunuyor, sadece
            // bildirim gelmiyor. Kullanicinin bunu tam burada bilmesi gerekiyor.
            if (!bildirimIzniVar) {
                Text(
                    text = "Bildirim izni verilmedi. Tarih kaydedilecek ama " +
                            "hatırlatma gelmeyecek.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MetinIkincil
                )
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun BakimAlanlariPreview() {
    MotorumTheme {
        BakimAlanlari(
            form = KayitFormu.Bakim(),
            onDegis = {},
            bildirimIzniVar = true,
            onHatirlatmaAcilsin = {}
        )
    }
}

// Hatirlatma acikken ve izin yokken nasil gorundugu: iki secici ve uyari satiri.
@Preview(showBackground = true)
@Composable
private fun BakimAlanlariHatirlatmaliPreview() {
    MotorumTheme {
        BakimAlanlari(
            form = KayitFormu.Bakim(
                bakimTuru = "Yağ değişimi",
                tutarYazi = "1250",
                hatirlatmaAcik = true,
                hatirlatmaSaat = 10,
                hatirlatmaDakika = 0
            ),
            onDegis = {},
            bildirimIzniVar = false,
            onHatirlatmaAcilsin = {}
        )
    }
}

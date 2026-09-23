package com.oguzhanp.motorum.feature.kayit.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oguzhanp.motorum.R
import com.oguzhanp.motorum.core.tasarim.MotorumTheme
import com.oguzhanp.motorum.core.tasarim.UyariMetin
import com.oguzhanp.motorum.core.util.formatKm
import com.oguzhanp.motorum.feature.kayit.bilesenler.SaatSecici
import com.oguzhanp.motorum.feature.kayit.bilesenler.TarihSecici

// Yolculugun bir ucunun alanlari. Ayni blok "Baslangic" ve "Bitis" icin iki kez
// kullanilacagi icin ayri composable; baslik disaridan veriliyor.
@Composable
fun TripNoktasiAlanlari(
    baslik: String,
    form: TripNoktasiFormu,
    onDegis: (TripNoktasiFormu) -> Unit,
    modifier: Modifier = Modifier,
    // Bitis blogunda km hatasi "bos" ya da "baslangictan kucuk" olabilir;
    // tek mesajla ikisi de karsilaniyor.
    kmMesaji: String = stringResource(R.string.zorunlu_alan),
    // Yakit formundaki ile ayni is: girilen sayac onceki okumalarin altindaysa
    // uyari cikiyor, kayit yine de kaydedilebiliyor.
    sonOkuma: Int? = null
) {
    val girilenKm = form.km
    val sayacGeride = sonOkuma != null && girilenKm != null && girilenKm < sonOkuma

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Text(baslik, style = MaterialTheme.typography.titleMedium)

        TarihSecici(
            tarihMillis = form.tarihMillis,
            onTarihSec = { onDegis(form.copy(tarihMillis = it)) },
            modifier = Modifier.fillMaxWidth()
        )

        SaatSecici(
            saat = form.saat,
            dakika = form.dakika,
            onSaatSec = { s, d -> onDegis(form.copy(saat = s, dakika = d)) },
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = form.kmYazi,
            onValueChange = { onDegis(form.copy(kmYazi = it, kmHatali = false)) },
            label = { Text(stringResource(R.string.aktif_km)) },
            isError = form.kmHatali,
            supportingText = {
                when {
                    form.kmHatali -> Text(kmMesaji)
                    sayacGeride -> Text(
                        text = stringResource(R.string.sayac_son_kayit, formatKm(sonOkuma)),
                        color = UyariMetin
                    )
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = form.sehir,
            onValueChange = { onDegis(form.copy(sehir = it, sehirHatali = false)) },
            label = { Text(stringResource(R.string.sehir)) },
            isError = form.sehirHatali,
            supportingText = { if (form.sehirHatali) Text(stringResource(R.string.zorunlu_alan)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun TripNoktasiAlanlariPreview() {
    MotorumTheme {
        TripNoktasiAlanlari(
            baslik = stringResource(R.string.baslangic),
            form = TripNoktasiFormu(),
            onDegis = {}
        )
    }
}

package com.oguzhanp.motorum.ui.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oguzhanp.motorum.ui.ekle.components.TarihSecici
import com.oguzhanp.motorum.ui.theme.MotorumTheme

// Yakit kategorisinin form alanlari. Hem ekleme hem detay ekrani ayni blogu cagiriyor;
// alan eklemek/degistirmek gerektiginde tek dosya degisiyor.
private const val ALAN_HATA_MESAJI = "Zorunlu alan — geçerli bir sayı girin"

@Composable
fun YakitAlanlari(
    form: KayitFormu.Yakit,
    onDegis: (KayitFormu.Yakit) -> Unit,
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
            value = form.litreYazi,
            onValueChange = { onDegis(form.copy(litreYazi = it, litreHatali = false)) },
            label = { Text("Litre") },
            isError = form.litreHatali,
            supportingText = {
                if (form.litreHatali) Text(ALAN_HATA_MESAJI)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = form.tutarYazi,
            onValueChange = { onDegis(form.copy(tutarYazi = it, tutarHatali = false)) },
            label = { Text("Tutar (₺)") },
            isError = form.tutarHatali,
            supportingText = {
                if (form.tutarHatali) Text(ALAN_HATA_MESAJI)
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun YakitAlanlariPreview() {
    MotorumTheme {
        YakitAlanlari(
            form = KayitFormu.Yakit(),
            onDegis = {}
        )
    }
}

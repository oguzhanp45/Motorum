package com.oguzhanp.motorum.ui.form

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oguzhanp.motorum.R
import com.oguzhanp.motorum.ui.ekle.components.TarihSecici
import com.oguzhanp.motorum.ui.theme.MotorumTheme

// Bakim kategorisinin form alanlari. Hem ekleme hem detay ekrani ayni blogu cagiriyor.
@Composable
fun BakimAlanlari(
    form: KayitFormu.Bakim,
    onDegis: (KayitFormu.Bakim) -> Unit,
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
    }
}

@Preview(showBackground = true)
@Composable
private fun BakimAlanlariPreview() {
    MotorumTheme {
        BakimAlanlari(
            form = KayitFormu.Bakim(),
            onDegis = {}
        )
    }
}

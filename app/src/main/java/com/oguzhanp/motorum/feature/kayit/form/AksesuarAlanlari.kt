package com.oguzhanp.motorum.feature.kayit.form

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
import com.oguzhanp.motorum.feature.kayit.bilesenler.TarihSecici
import com.oguzhanp.motorum.core.tasarim.MotorumTheme

// Aksesuar kategorisinin form alanlari. Hem ekleme hem detay ekrani ayni blogu cagiriyor.
@Composable
fun AksesuarAlanlari(
    form: KayitFormu.Aksesuar,
    onDegis: (KayitFormu.Aksesuar) -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TarihSecici(
            tarihMillis = form.tarihMillis,
            onTarihSec = { onDegis(form.copy(tarihMillis = it)) },
            etiket = stringResource(R.string.alim_tarihi),
            aciklama = stringResource(R.string.alim_tarihi_aciklama),
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = form.aksesuarAdi,
            onValueChange = { onDegis(form.copy(aksesuarAdi = it, aksesuarAdiHatali = false)) },
            label = { Text(stringResource(R.string.aksesuar_adi)) },
            isError = form.aksesuarAdiHatali,
            supportingText = { if (form.aksesuarAdiHatali) Text(stringResource(R.string.zorunlu_alan)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = form.tutarYazi,
            onValueChange = { onDegis(form.copy(tutarYazi = it, tutarHatali = false)) },
            label = { Text(stringResource(R.string.tutar_tl)) },
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
private fun AksesuarAlanlariPreview() {
    MotorumTheme {
        AksesuarAlanlari(
            form = KayitFormu.Aksesuar(),
            onDegis = {}
        )
    }
}

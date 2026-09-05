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
import com.oguzhanp.motorum.ui.theme.MotorumTheme

private const val BITIS_KM_MESAJI = "Başlangıç km'sinden büyük bir değer girin"

// Road trip kategorisinin form alanlari. Hem ekleme hem detay ekrani bunu cagiriyor.
// Alanlarin sirasi yolculugun zaman sirasi: baslangic, molalar, bitis, masraf.
// Mola ve bitis yeni kayitta gizli: yola cikarken henuz ne mola verilmis ne de varilmistir.
@Composable
fun RoadTripAlanlari(
    form: KayitFormu.RoadTrip,
    onDegis: (KayitFormu.RoadTrip) -> Unit,
    modifier: Modifier = Modifier,
    molaGoster: Boolean = false,
    bitisGoster: Boolean = false
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TripNoktasiAlanlari(
            baslik = "Başlangıç",
            form = form.baslangic,
            onDegis = { onDegis(form.copy(baslangic = it)) },
            modifier = Modifier.fillMaxWidth()
        )

        if (molaGoster) {
            MolaListesi(
                molalar = form.molalar,
                onDegis = { onDegis(form.copy(molalar = it)) },
                modifier = Modifier.fillMaxWidth()
            )
        }

        if (bitisGoster) {
            TripNoktasiAlanlari(
                baslik = "Bitiş",
                form = form.bitis,
                onDegis = { onDegis(form.copy(bitis = it)) },
                kmMesaji = BITIS_KM_MESAJI,
                modifier = Modifier.fillMaxWidth()
            )
        }

        OutlinedTextField(
            value = form.masrafYazi,
            onValueChange = { onDegis(form.copy(masrafYazi = it)) },
            label = { Text("Masraf (₺) — isteğe bağlı") },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun RoadTripAlanlariPreview() {
    MotorumTheme {
        RoadTripAlanlari(
            form = KayitFormu.RoadTrip(),
            onDegis = {},
            molaGoster = true,
            bitisGoster = true
        )
    }
}

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
    bildirimIzniVar: Boolean,
    onHatirlatmaAcilsin: () -> Unit,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        OutlinedTextField(
            value = form.bakimTuru,
            onValueChange = { onDegis(form.copy(bakimTuru = it, bakimTuruHatali = false)) },
            label = { Text("Bakım türü") },
            isError = form.bakimTuruHatali,
            supportingText = { if (form.bakimTuruHatali) Text(stringResource(R.string.zorunlu_alan)) },
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        // Once "ne yaptirdim", sonra "ne zaman": tur ilk alan.
        TarihSecici(
            tarihMillis = form.tarihMillis,
            // Secili aralik varsa hatirlatma tarihi de onunla kayiyor.
            onTarihSec = { onDegis(form.tarihDegistir(it)) },
            etiket = "Bakım tarihi",
            aciklama = "Bakımı yaptırdığın gün",
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

        HatirlatmaBlogu(
            baslik = "Bakım zamanı hatırlat",
            acik = form.hatirlatmaAcik,
            secilenAy = form.hatirlatmaAyi,
            tarihMillis = form.hatirlatmaTarihMillis,
            saat = form.hatirlatmaSaat,
            dakika = form.hatirlatmaDakika,
            // Kayitli ve dokunulmamis hatirlatmanin zamani gectiyse: calmis.
            gecmis = form.hatirlatmaGecmiste && form.hatirlatmaMillis == form.kayitliHatirlatma,
            hatali = form.hatirlatmaHatali,
            bildirimIzniVar = bildirimIzniVar,
            // Acarken izin akisi devreye giriyor, o yuzden karari ekrana
            // biraktik; alani acmak da onun isi. Kapatmak izin gerektirmiyor.
            onAnahtar = { acik ->
                if (acik) {
                    onHatirlatmaAcilsin()
                } else {
                    onDegis(form.copy(hatirlatmaAcik = false, hatirlatmaHatali = false))
                }
            },
            onAralikSec = { onDegis(form.araligiSec(it)) },
            onTarihSec = { onDegis(form.hatirlatmaTarihiSec(it)) },
            onSaatSec = { saat, dakika ->
                onDegis(
                    form.copy(
                        hatirlatmaSaat = saat,
                        hatirlatmaDakika = dakika,
                        hatirlatmaHatali = false
                    )
                )
            }
        )
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
                hatirlatmaAyi = 3,
                hatirlatmaTarihMillis = System.currentTimeMillis() + 90L * 24 * 60 * 60 * 1000,
                hatirlatmaSaat = 10,
                hatirlatmaDakika = 0
            ),
            onDegis = {},
            bildirimIzniVar = false,
            onHatirlatmaAcilsin = {}
        )
    }
}

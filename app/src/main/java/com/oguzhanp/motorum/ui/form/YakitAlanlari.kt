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
import com.oguzhanp.motorum.ui.theme.UyariMetin
import com.oguzhanp.motorum.util.formatKm

// Yakit kategorisinin form alanlari. Hem ekleme hem detay ekrani ayni blogu cagiriyor;
// alan eklemek/degistirmek gerektiginde tek dosya degisiyor.

@Composable
fun YakitAlanlari(
    form: KayitFormu.Yakit,
    onDegis: (KayitFormu.Yakit) -> Unit,
    modifier: Modifier = Modifier,
    // Bugune kadar girilmis en yuksek sayac degeri. Sadece uyari icin.
    sonOkuma: Int? = null
) {
    // Sayac geriye gidiyorsa uyariyoruz ama engellemiyoruz: kullanici gecmise
    // ait bir kaydi sonradan giriyor olabilir, o zaman kucuk deger dogrudur.
    val girilenKm = form.km
    val sayacGeride = sonOkuma != null && girilenKm != null && girilenKm < sonOkuma

    Column(
        modifier = modifier,
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        TarihSecici(
            tarihMillis = form.tarihMillis,
            onTarihSec = { onDegis(form.copy(tarihMillis = it)) },
            etiket = "Dolum tarihi",
            aciklama = "Yakıt aldığın gün",
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = form.litreYazi,
            onValueChange = { onDegis(form.copy(litreYazi = it, litreHatali = false)) },
            label = { Text("Litre") },
            isError = form.litreHatali,
            supportingText = {
                if (form.litreHatali) Text(stringResource(R.string.gecerli_sayi))
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = form.kmYazi,
            onValueChange = { onDegis(form.copy(kmYazi = it, kmHatali = false)) },
            label = { Text("Aktif km (isteğe bağlı)") },
            isError = form.kmHatali,
            supportingText = {
                when {
                    form.kmHatali -> Text(stringResource(R.string.gecerli_sayi))
                    sayacGeride -> Text(
                        text = "Son kayıttaki sayaç ${formatKm(sonOkuma)}. Geçmişe ait bir kayıt giriyorsan sorun değil.",
                        color = UyariMetin
                    )
                    // Km gidilen yolu ve km basi maliyeti besliyor.
                    else -> Text("Sayacın o anki değeri. Gidilen yolu hesaplamaya yardım eder.")
                }
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            singleLine = true,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = form.tutarYazi,
            onValueChange = { onDegis(form.copy(tutarYazi = it, tutarHatali = false)) },
            label = { Text("Tutar (₺)") },
            isError = form.tutarHatali,
            supportingText = {
                if (form.tutarHatali) Text(stringResource(R.string.gecerli_sayi))
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

package com.oguzhanp.motorum.core.tasarim

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp

// Etiketi alanin USTUNDE duran metin alani. Material'in kayan etiketi yerine
// bunu kullaniyoruz cunku etiket her zaman gorunur kaliyor ve zorunlu alan
// yildizina yer aciyor. Giris/Uye Ol ekranlariyla ayni dil.
@Composable
fun EtiketliAlan(
    etiket: String,
    deger: String,
    onDegis: (String) -> Unit,
    modifier: Modifier = Modifier,
    zorunlu: Boolean = false,
    ipucu: String = "",
    ikon: ImageVector? = null,
    hatali: Boolean = false,
    hataMetni: String = "",
    klavye: KeyboardOptions = KeyboardOptions.Default
) {
    Column(modifier) {
        Row {
            Text(
                text = etiket,
                style = MaterialTheme.typography.labelMedium,
                color = MetinEtiket
            )
            if (zorunlu) {
                Text(
                    text = " *",
                    style = MaterialTheme.typography.labelMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }

        OutlinedTextField(
            value = deger,
            onValueChange = onDegis,
            placeholder = { Text(ipucu, color = MetinSolgun) },
            leadingIcon = ikon?.let {
                { Icon(it, contentDescription = null, tint = MetinSolgun) }
            },
            isError = hatali,
            singleLine = true,
            shape = RoundedCornerShape(12.dp),
            keyboardOptions = klavye,
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = 4.dp)
        )

        if (hatali && hataMetni.isNotBlank()) {
            Text(
                text = hataMetni,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.error,
                modifier = Modifier.padding(top = 4.dp)
            )
        }
    }
}

// Alanin uc hali tek onizlemede: normal, zorunlu (yildizli) ve hatali.
// Ayri ayri onizleme yazmak yerine boyle bakmak farklari gosteriyor.
@Preview(showBackground = true)
@Composable
private fun EtiketliAlanPreview() {
    MotorumTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EtiketliAlan(
                etiket = "Plaka (isteğe bağlı)",
                deger = "34 BKR 102",
                onDegis = {},
                ikon = MotorumIkonlari.Plaka
            )
            EtiketliAlan(
                etiket = "Marka",
                zorunlu = true,
                deger = "",
                onDegis = {},
                ipucu = "Örn: Yamaha, Honda",
                ikon = MotorumIkonlari.Motor
            )
            EtiketliAlan(
                etiket = "Model",
                zorunlu = true,
                deger = "",
                onDegis = {},
                ipucu = "Örn: MT-07",
                hatali = true,
                hataMetni = "Model zorunlu"
            )
        }
    }
}

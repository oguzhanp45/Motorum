package com.oguzhanp.motorum.ui.components

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
import androidx.compose.ui.unit.dp
import com.oguzhanp.motorum.ui.theme.MetinEtiket
import com.oguzhanp.motorum.ui.theme.MetinSolgun

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

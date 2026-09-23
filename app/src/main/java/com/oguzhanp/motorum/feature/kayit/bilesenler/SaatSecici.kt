package com.oguzhanp.motorum.feature.kayit.bilesenler

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TimePicker
import androidx.compose.material3.rememberTimePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oguzhanp.motorum.R
import com.oguzhanp.motorum.core.tasarim.MotorumTheme
import com.oguzhanp.motorum.core.util.formatSaat
import com.oguzhanp.motorum.core.tasarim.MotorumIkonlari

// Saat gosterimi + saat secici diyalogu. TarihSecici ile ayni kalip.
// Material3'te hazir bir TimePickerDialog yok, TimePicker bir AlertDialog icine konuyor.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaatSecici(
    saat: Int?,
    dakika: Int?,
    onSaatSec: (Int, Int) -> Unit,
    modifier: Modifier = Modifier,
    etiket: String = stringResource(R.string.saat)
) {
    var acik by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = if (saat == null || dakika == null) "" else formatSaat(saat, dakika),
        onValueChange = { },
        readOnly = true,
        label = { Text(etiket) },
        trailingIcon = {
            IconButton(onClick = { acik = true }) {
                Icon(MotorumIkonlari.Saat, contentDescription = stringResource(R.string.saat_sec))
            }
        },
        modifier = modifier
    )

    if (acik) {
        SaatDiyalogu(
            saat = saat,
            dakika = dakika,
            onSaatSec = onSaatSec,
            onKapat = { acik = false }
        )
    }
}

// Saat diyalogunun kendisi; TarihDiyalogu ile ayni sebeple ayri.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaatDiyalogu(
    saat: Int?,
    dakika: Int?,
    onSaatSec: (Int, Int) -> Unit,
    onKapat: () -> Unit
) {
    val durum = rememberTimePickerState(
        initialHour = saat ?: 0,
        initialMinute = dakika ?: 0,
        is24Hour = true
    )

    AlertDialog(
        onDismissRequest = onKapat,
        confirmButton = {
            TextButton(
                onClick = {
                    onSaatSec(durum.hour, durum.minute)
                    onKapat()
                }
            ) { Text(stringResource(R.string.tamam)) }
        },
        dismissButton = {
            TextButton(onClick = onKapat) { Text(stringResource(R.string.iptal)) }
        },
        text = { TimePicker(state = durum) }
    )
}

@Preview(showBackground = true)
@Composable
private fun SaatSeciciPreview() {
    MotorumTheme {
        SaatSecici(
            saat = 9,
            dakika = 30,
            onSaatSec = { _, _ -> },
            modifier = Modifier.padding(16.dp)
        )
    }
}

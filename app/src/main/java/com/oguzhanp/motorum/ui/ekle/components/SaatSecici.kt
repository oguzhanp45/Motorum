package com.oguzhanp.motorum.ui.ekle.components

import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Schedule
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oguzhanp.motorum.ui.theme.MotorumTheme
import com.oguzhanp.motorum.util.formatSaat

// Saat gosterimi + saat secici diyalogu. TarihSecici ile ayni kalip.
// Material3'te hazir bir TimePickerDialog yok, TimePicker bir AlertDialog icine konuyor.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SaatSecici(
    saat: Int?,
    dakika: Int?,
    onSaatSec: (Int, Int) -> Unit,
    modifier: Modifier = Modifier
) {
    var acik by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = if (saat == null || dakika == null) "" else formatSaat(saat, dakika),
        onValueChange = { },
        readOnly = true,
        label = { Text("Saat") },
        trailingIcon = {
            IconButton(onClick = { acik = true }) {
                Icon(Icons.Default.Schedule, contentDescription = "Saat sec")
            }
        },
        modifier = modifier
    )

    if (acik) {
        val durum = rememberTimePickerState(
            initialHour = saat ?: 0,
            initialMinute = dakika ?: 0,
            is24Hour = true
        )

        AlertDialog(
            onDismissRequest = { acik = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        onSaatSec(durum.hour, durum.minute)
                        acik = false
                    }
                ) { Text("Tamam") }
            },
            dismissButton = {
                TextButton(onClick = { acik = false }) { Text("Iptal") }
            },
            text = { TimePicker(state = durum) }
        )
    }
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

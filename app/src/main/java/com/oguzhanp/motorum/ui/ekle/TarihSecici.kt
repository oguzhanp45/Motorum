package com.oguzhanp.motorum.ui.ekle

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DateRange
import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import com.oguzhanp.motorum.util.formatTarih

// Tarih gosterimi + takvim diyalogu.
// Compose'da diyalog "gosterilmez", VAR ya da YOK olur: if (acik) { ... }
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarihSecici(
    tarihMillis: Long,
    onTarihSec: (Long) -> Unit,
    modifier: Modifier = Modifier
) {
    var acik by remember { mutableStateOf(false) }



    OutlinedTextField(
        value = formatTarih(tarihMillis),
        onValueChange = { },
        readOnly = true,
        label = { Text("Tarih") },
        trailingIcon = {
            IconButton(onClick = { acik = true }) {
                Icon(Icons.Default.DateRange, contentDescription = "Tarih sec")
            }
        },
        modifier = modifier
    )

    if (acik) {
        // Takvimin kendi ic durumu: hangi ay gorunuyor, hangi gun secili
        val durum = rememberDatePickerState(initialSelectedDateMillis = tarihMillis)

        DatePickerDialog(
            onDismissRequest = { acik = false },
            confirmButton = {
                TextButton(
                    onClick = {
                        // Kullanici gun secmeden Tamam'a basabilir -> nullable
                        durum.selectedDateMillis?.let { onTarihSec(it) }
                        acik = false
                    }
                ) { Text("Tamam") }
            },
            dismissButton = {
                TextButton(onClick = { acik = false }) { Text("Iptal") }
            }
        ) {
            DatePicker(state = durum)
        }
    }
}

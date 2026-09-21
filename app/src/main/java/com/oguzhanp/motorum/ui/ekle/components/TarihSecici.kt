package com.oguzhanp.motorum.ui.ekle.components

import java.util.Calendar
import java.util.TimeZone
import androidx.compose.foundation.layout.padding
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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oguzhanp.motorum.ui.theme.MetinSolgun
import com.oguzhanp.motorum.ui.theme.MotorumTheme
import com.oguzhanp.motorum.util.formatTarih
import com.oguzhanp.motorum.ui.components.MotorumIkonlari

// Tarih gosterimi + takvim diyalogu.
// Compose'da diyalog "gosterilmez", VAR ya da YOK olur: if (acik) { ... }
// tarihMillis her zaman dolu: formlar bugunun tarihiyle basliyor.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarihSecici(
    tarihMillis: Long,
    onTarihSec: (Long) -> Unit,
    modifier: Modifier = Modifier,
    // Ayni ekranda iki tarih alani olabiliyor (kayit tarihi ve hatirlatma),
    // ikisinin de "Tarih" yazmasi kafa karistiriyordu.
    etiket: String = "Tarih",
    // Alanin altinda soluk aciklama: "Bakimi yaptirdigin gun" gibi. Formda iki
    // tarih olunca (kayit ve hatirlatma) hangisinin ne oldugu buradan anlasiliyor.
    aciklama: String? = null
) {
    var acik by remember { mutableStateOf(false) }

    OutlinedTextField(
        value = formatTarih(tarihMillis),
        onValueChange = { },
        readOnly = true,
        label = { Text(etiket) },
        supportingText = aciklama?.let { { Text(it, color = MetinSolgun) } },
        trailingIcon = {
            IconButton(onClick = { acik = true }) {
                Icon(MotorumIkonlari.Takvim, contentDescription = "Tarih sec")
            }
        },
        modifier = modifier
    )

    if (acik) {
        TarihDiyalogu(
            tarihMillis = tarihMillis,
            onTarihSec = onTarihSec,
            onKapat = { acik = false }
        )
    }
}

// Takvim diyalogunun kendisi. Alandan ayrildi cunku hatirlatma blogu kendi
// gorunumlu alanini ciziyor ama ayni takvimi aciyor.
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TarihDiyalogu(
    tarihMillis: Long,
    onTarihSec: (Long) -> Unit,
    onKapat: () -> Unit
) {
    // Takvimin kendi ic durumu: hangi ay gorunuyor, hangi gun secili.
    // DatePicker gunleri UTC gece yarisi olarak tutuyor; bizim degerlerimiz
    // telefonun saat diliminde. Cevirmeden verince saat farki kadar kayip
    // yanlis gun isaretlenebiliyordu (ayni gune hatirlatma kurulamamasi bundandi).
    val durum = rememberDatePickerState(
        initialSelectedDateMillis = yereldenUtcGune(tarihMillis)
    )

    DatePickerDialog(
        onDismissRequest = onKapat,
        confirmButton = {
            TextButton(
                onClick = {
                    // Kullanici gun secmeden Tamam'a basabilir -> nullable
                    durum.selectedDateMillis?.let { onTarihSec(utcGundenYerele(it, tarihMillis)) }
                    onKapat()
                }
            ) { Text("Tamam") }
        },
        dismissButton = {
            TextButton(onClick = onKapat) { Text("Iptal") }
        }
    ) {
        DatePicker(state = durum)
    }
}

@Preview(showBackground = true)
@Composable
private fun TarihSeciciPreview() {
    MotorumTheme {
        TarihSecici(
            tarihMillis = System.currentTimeMillis(),
            onTarihSec = {},
            modifier = Modifier.padding(16.dp)
        )
    }
}

// Telefondaki tarihin gununu (yil/ay/gun) UTC gece yarisina tasiyor: takvim
// o gunu isaretlesin.
private fun yereldenUtcGune(yerelMillis: Long): Long {
    val yerel = Calendar.getInstance().apply { timeInMillis = yerelMillis }
    return Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply {
        clear()
        set(yerel.get(Calendar.YEAR), yerel.get(Calendar.MONTH), yerel.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

// Takvimin verdigi UTC gununu telefonun saat dilimine ceviriyor. Gunun saati
// eski degerden korunuyor: kayit tarihinin saati kaybolmasin.
private fun utcGundenYerele(utcMillis: Long, eskiYerelMillis: Long): Long {
    val utc = Calendar.getInstance(TimeZone.getTimeZone("UTC")).apply { timeInMillis = utcMillis }
    return Calendar.getInstance().apply {
        timeInMillis = eskiYerelMillis
        set(utc.get(Calendar.YEAR), utc.get(Calendar.MONTH), utc.get(Calendar.DAY_OF_MONTH))
    }.timeInMillis
}

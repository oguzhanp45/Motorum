package com.oguzhanp.motorum.ui.motorlarim

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oguzhanp.motorum.model.Motor
import com.oguzhanp.motorum.ui.theme.AksiyonMaviZemin
import com.oguzhanp.motorum.ui.theme.MetinIkincil
import com.oguzhanp.motorum.ui.theme.MetinSolgun
import com.oguzhanp.motorum.ui.theme.MotorumTheme
import com.oguzhanp.motorum.ui.theme.SekmeZemin

// Ust bardaki cip. Motor yoksa da gorunuyor ama tiklanmiyor: acilacak bir liste yok.
@Composable
fun MotorCipi(
    motor: Motor?,
    onTikla: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .padding(end = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(SekmeZemin)
            .clickable(enabled = motor != null, onClick = onTikla)
            .padding(horizontal = 10.dp, vertical = 6.dp)
            .widthIn(max = 200.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Icon(
            Icons.Default.TwoWheeler,
            contentDescription = null,
            tint = MetinIkincil,
            modifier = Modifier.size(16.dp)
        )
        Text(
            text = motor?.let { cipMetni(it) } ?: "Motor yok",
            style = MaterialTheme.typography.labelMedium,
            color = MetinIkincil,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            modifier = Modifier.weight(1f, fill = false)
        )
        if (motor != null) {
            Icon(
                Icons.Default.ExpandMore,
                contentDescription = "Motor seç",
                tint = MetinIkincil,
                modifier = Modifier.size(16.dp)
            )
        }
    }
}

// Plaka isteğe bagli: yoksa cip sadece marka model gosteriyor, bos bir ayrac
// birakmiyor.
private fun cipMetni(motor: Motor): String =
    if (motor.plaka.isBlank()) motor.adi else "${motor.adi} · ${motor.plaka}"

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotorSecimPaneli(
    motorlar: List<Motor>,
    seciliMotorId: String?,
    onMotorSec: (String) -> Unit,
    onMotorEkleTikla: () -> Unit,
    onKapat: () -> Unit
) {
    ModalBottomSheet(
        onDismissRequest = onKapat,
        sheetState = rememberModalBottomSheetState(),
        containerColor = MaterialTheme.colorScheme.surface
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
                .padding(bottom = 24.dp),
            verticalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Text(
                text = "Motor Seç",
                style = MaterialTheme.typography.titleMedium,
                modifier = Modifier.padding(bottom = 8.dp)
            )

            motorlar.forEach { motor ->
                MotorSecimSatiri(
                    motor = motor,
                    secili = motor.id == seciliMotorId,
                    onTikla = { onMotorSec(motor.id) }
                )
            }

            // Motor secerken "yenisini ekleyeyim" istegi dogal: Motorlarim
            // sekmesine ugramadan ekleme sayfasina goturuyor. Kenarlik primary,
            // cunku varsayilan gri kenarlik beyaz sheet zemininde siliktir.
            OutlinedButton(
                onClick = onMotorEkleTikla,
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = 8.dp)
                    .height(48.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(Modifier.width(8.dp))
                Text("Motor Ekle")
            }
        }
    }
}

@Composable
private fun MotorSecimSatiri(
    motor: Motor,
    secili: Boolean,
    onTikla: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(if (secili) AksiyonMaviZemin else MaterialTheme.colorScheme.surface)
            .clickable(onClick = onTikla)
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        MotorGorseli(motor = motor, boyut = 40.dp)

        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = motor.adi,
                style = MaterialTheme.typography.titleSmall,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            Text(
                text = motor.plaka.ifBlank { "Plaka yok" },
                style = MaterialTheme.typography.bodySmall,
                color = if (motor.plaka.isBlank()) MetinSolgun else MetinIkincil
            )
        }

        Icon(
            imageVector = if (secili) Icons.Default.CheckCircle
            else Icons.Default.RadioButtonUnchecked,
            contentDescription = null,
            tint = if (secili) MaterialTheme.colorScheme.primary else MetinSolgun
        )
    }
}

// Cipin uc hali. Uzun isimde metnin kesilmesi ve motor yokken okun
// kaybolmasi ancak yan yana bakinca fark ediliyor.
// MotorSecimPaneli bir ModalBottomSheet oldugu icin onizlemede cizilmiyor;
// icindeki satiri temsil eden MotorSecimSatiri ayrica onizleniyor.
@Preview(showBackground = true)
@Composable
private fun MotorCipiPreview() {
    MotorumTheme {
        Column(
            modifier = Modifier.padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            MotorCipi(
                motor = Motor(id = "a", marka = "Yamaha", model = "MT-07", plaka = "34 BKR 102"),
                onTikla = {}
            )
            MotorCipi(
                motor = Motor(id = "b", marka = "Honda", model = "Africa Twin Adventure Sports"),
                onTikla = {}
            )
            MotorCipi(motor = null, onTikla = {})
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MotorSecimSatiriPreview() {
    MotorumTheme {
        Column(modifier = Modifier.padding(16.dp)) {
            MotorSecimSatiri(
                motor = Motor(id = "a", marka = "Yamaha", model = "MT-07", plaka = "34 BKR 102"),
                secili = true,
                onTikla = {}
            )
            MotorSecimSatiri(
                motor = Motor(id = "b", marka = "Honda", model = "CRF 250"),
                secili = false,
                onTikla = {}
            )
        }
    }
}

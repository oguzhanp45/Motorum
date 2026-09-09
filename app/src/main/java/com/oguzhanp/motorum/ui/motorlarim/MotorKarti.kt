package com.oguzhanp.motorum.ui.motorlarim

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.oguzhanp.motorum.model.Motor
import com.oguzhanp.motorum.ui.theme.AksiyonMaviZemin
import com.oguzhanp.motorum.ui.theme.MetinIkincil
import com.oguzhanp.motorum.ui.theme.MetinSolgun

@Composable
fun MotorKarti(
    motor: Motor,
    secili: Boolean,
    onTikla: () -> Unit,
    onDuzenleTikla: () -> Unit,
    onSilTikla: () -> Unit,
    modifier: Modifier = Modifier
) {
    val gorunum = motorGorunumu(motor.id)
    var menuAcik by remember { mutableStateOf(false) }

    Card(
        onClick = onTikla,
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(
            // Secili kart acik mavi zeminli: bottom sheet'teki secim satiriyla
            // ayni dil, iki yerde de ayni sey anlatiliyor.
            containerColor = if (secili) AksiyonMaviZemin else MaterialTheme.colorScheme.surface
        ),
        elevation = CardDefaults.cardElevation(defaultElevation = 0.dp)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(48.dp)
                    .clip(RoundedCornerShape(12.dp))
                    .background(gorunum.zemin),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    Icons.Default.TwoWheeler,
                    contentDescription = null,
                    tint = gorunum.renk,
                    modifier = Modifier.size(26.dp)
                )
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 12.dp)
            ) {
                Text(
                    text = motor.adi,
                    style = MaterialTheme.typography.titleSmall,
                    color = if (motor.bilgisiz) MetinIkincil else MaterialTheme.colorScheme.onSurface,
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
                contentDescription = if (secili) "Seçili motor" else "Bu motora geç",
                tint = if (secili) MaterialTheme.colorScheme.primary else MetinSolgun
            )

            // Karta basmak motoru SECIYOR, o yuzden duzenleme ve silme icin ayri
            // bir kapi gerekiyor. IconButton kendi tiklamasini yutuyor, karta
            // gitmiyor.
            Box {
                IconButton(onClick = { menuAcik = true }) {
                    Icon(
                        Icons.Default.MoreVert,
                        contentDescription = "Motor menüsü",
                        tint = MetinIkincil
                    )
                }
                DropdownMenu(expanded = menuAcik, onDismissRequest = { menuAcik = false }) {
                    DropdownMenuItem(
                        text = { Text("Düzenle") },
                        leadingIcon = { Icon(Icons.Default.Edit, contentDescription = null) },
                        onClick = {
                            menuAcik = false
                            onDuzenleTikla()
                        }
                    )
                    DropdownMenuItem(
                        text = { Text("Sil", color = MaterialTheme.colorScheme.error) },
                        leadingIcon = {
                            Icon(
                                Icons.Default.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error
                            )
                        },
                        onClick = {
                            menuAcik = false
                            onSilTikla()
                        }
                    )
                }
            }
        }
    }
}

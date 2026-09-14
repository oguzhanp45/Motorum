package com.oguzhanp.motorum.ui.navigation

import androidx.compose.foundation.layout.Column
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ReceiptLong
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oguzhanp.motorum.ui.theme.AksiyonMaviZemin
import com.oguzhanp.motorum.ui.theme.MetinIkincil
import com.oguzhanp.motorum.ui.theme.MotorumTheme

enum class Sekme(val rota: String, val etiket: String, val ikon: ImageVector) {
    MOTORLARIM(Routes.MOTORLARIM, "Motorlarım", Icons.Default.TwoWheeler),
    ANA_SAYFA(Routes.ANA_SAYFA, "Ana Sayfa", Icons.Default.ReceiptLong),
    AYARLAR(Routes.AYARLAR, "Ayarlar", Icons.Default.Settings)
}

@Composable
fun AltBar(
    seciliRota: String,
    onSekmeTikla: (String) -> Unit
) {
    Column {
        HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant)
        NavigationBar(
            containerColor = MaterialTheme.colorScheme.surface,
            // tonalElevation 0: M3 varsayilani zemine hafif bir mor ton katiyor,
            // bizim beyazi kirletiyordu.
            tonalElevation = 0.dp
        ) {
            Sekme.entries.forEach { sekme ->
                NavigationBarItem(
                    selected = seciliRota == sekme.rota,
                    onClick = { onSekmeTikla(sekme.rota) },
                    icon = { Icon(sekme.ikon, contentDescription = sekme.etiket) },
                    label = { Text(sekme.etiket, style = MaterialTheme.typography.labelMedium) },
                    colors = NavigationBarItemDefaults.colors(
                        selectedIconColor = MaterialTheme.colorScheme.primary,
                        selectedTextColor = MaterialTheme.colorScheme.primary,
                        indicatorColor = AksiyonMaviZemin,
                        unselectedIconColor = MetinIkincil,
                        unselectedTextColor = MetinIkincil
                    )
                )
            }
        }
    }
}

// Ortadaki sekme secili: secili ve secili olmayan renkler birlikte gorunuyor.
@Preview(showBackground = true)
@Composable
private fun AltBarPreview() {
    MotorumTheme {
        AltBar(seciliRota = Routes.ANA_SAYFA, onSekmeTikla = {})
    }
}

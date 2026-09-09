package com.oguzhanp.motorum.ui.ayarlar

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.Logout
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.oguzhanp.motorum.core.constants.AppSpacing
import com.oguzhanp.motorum.ui.navigation.AnaBolgeKabugu
import com.oguzhanp.motorum.ui.navigation.Routes
import com.oguzhanp.motorum.ui.theme.MotorumTheme

@Composable
fun AyarlarSayfasi(
    onSekmeTikla: (String) -> Unit,
    onCikisYapildi: () -> Unit,
    viewModel: AyarlarViewModel = hiltViewModel()
) {
    AyarlarIcerik(
        onSekmeTikla = onSekmeTikla,
        onCikisTikla = {
            viewModel.cikisYap()
            onCikisYapildi()
        }
    )
}

@Composable
fun AyarlarIcerik(
    onSekmeTikla: (String) -> Unit,
    onCikisTikla: () -> Unit
) {
    // Onay diyalogu ekranin yerel durumu: ViewModel'e tasimaya deger bir bilgi
    // degil, ekran kapaninca kaybolmasi zaten dogru.
    var onayGoster by rememberSaveable { mutableStateOf(false) }

    AnaBolgeKabugu(
        baslik = "Ayarlar",
        seciliRota = Routes.AYARLAR,
        onSekmeTikla = onSekmeTikla
    ) { icPadding ->
        // Govde simdilik bos: dil ve tema ayarlari sonra gelecek.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(icPadding)
                .padding(AppSpacing.normal),
            contentAlignment = Alignment.BottomEnd
        ) {
            Button(
                onClick = { onayGoster = true },
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.error,
                    contentColor = MaterialTheme.colorScheme.onError
                )
            ) {
                Icon(
                    Icons.AutoMirrored.Filled.Logout,
                    contentDescription = null,
                    modifier = Modifier.size(18.dp)
                )
                Spacer(Modifier.width(8.dp))
                Text("Çıkış")
            }
        }
    }

    if (onayGoster) {
        AlertDialog(
            onDismissRequest = { onayGoster = false },
            title = { Text("Çıkış yap") },
            text = { Text("Hesabından çıkmak istediğine emin misin?") },
            confirmButton = {
                TextButton(onClick = {
                    onayGoster = false
                    onCikisTikla()
                }) {
                    Text("Çıkış yap", color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { onayGoster = false }) { Text("İptal") }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun AyarlarIcerikPreview() {
    MotorumTheme {
        AyarlarIcerik(onSekmeTikla = {}, onCikisTikla = {})
    }
}

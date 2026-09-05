package com.oguzhanp.motorum.ui.detay

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.oguzhanp.motorum.core.constants.AppSpacing
import com.oguzhanp.motorum.model.Kayit
import com.oguzhanp.motorum.ui.ekle.components.TarihSecici
import com.oguzhanp.motorum.ui.form.KayitFormu
import com.oguzhanp.motorum.ui.home.KayitViewModel
import com.oguzhanp.motorum.ui.theme.MotorumTheme

private const val ALAN_HATA_MESAJI = "Zorunlu alan — geçerli bir sayı girin"

@Composable
fun KayitDetaySayfasi(
    kayitViewModel: KayitViewModel,
    navController: NavController,
    kayitId: String
) {
    val uiState by kayitViewModel.uiState.collectAsStateWithLifecycle()
    val kayit = uiState.kayitlar.find { it.id == kayitId }

    if (kayit == null) {
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }

    var detay by remember(kayit.id) {
        mutableStateOf(
            KayitDetayUiState(
                form = KayitFormu(
                    kategori = kayit.kategori,
                    tarihMillis = kayit.tarihMillis,
                    litreYazi = kayit.litre.toString(),
                    tutarYazi = kayit.tutar.toString(),
                    not = kayit.not
                )
            )
        )
    }

    KayitDetayIcerik(
        form = detay.form,
        silmeOnayiGoster = detay.silmeOnayiGoster,
        onFormDegis = { yeniForm -> detay = detay.copy(form = yeniForm) },
        onSilmeOnayiDegis = { goster -> detay = detay.copy(silmeOnayiGoster = goster) },
        onSil = {
            navController.popBackStack() // Once geri don, sonra sil. Tersi olsaydi liste guncellenince ekran bir kez daha
            kayitViewModel.sil(kayitId) // cizilir, find null doner ve ustteki LaunchedEffect ikinci bir popBackStack cagirirdi.
        },
        onGeri = { navController.popBackStack() },
        onGuncelle = {
            val kontrol = detay.form.dogrula()
            if (kontrol.gecerli) {
                kayitViewModel.duzenle(
                    Kayit(
                        id = kayitId,
                        kategori = kontrol.kategori,
                        tarihMillis = kontrol.tarihMillis,
                        litre = kontrol.litre!!,
                        tutar = kontrol.tutar!!,
                        not = kontrol.not.trim()
                    )
                )
                navController.popBackStack()
            } else {
                detay = detay.copy(form = kontrol)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KayitDetayIcerik(
    form: KayitFormu,
    silmeOnayiGoster: Boolean,
    onFormDegis: (KayitFormu) -> Unit,
    onSilmeOnayiDegis: (Boolean) -> Unit,// Diyalogu acmak ve iptal etmek ayni islem.true/false yapmak.
    onSil: () -> Unit,
    onGuncelle: () -> Unit,
    onGeri: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kayıt Detayı") },
                navigationIcon = {
                    IconButton(onClick = onGeri) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                actions = {
                    IconButton(onClick = { onSilmeOnayiDegis(true) }) {
                        Icon(Icons.Default.Delete, contentDescription = "Sil")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .padding(AppSpacing.normal),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            Text(
                text = "Kategori: ${form.kategori.etiket}",
                style = MaterialTheme.typography.bodyLarge
            )

            TarihSecici(
                tarihMillis = form.tarihMillis,
                onTarihSec = { onFormDegis(form.copy(tarihMillis = it)) },
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = form.litreYazi,
                onValueChange = { onFormDegis(form.copy(litreYazi = it, litreHatali = false)) },
                label = { Text("Litre") },
                isError = form.litreHatali,
                supportingText = {
                    if (form.litreHatali) Text(ALAN_HATA_MESAJI)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = form.tutarYazi,
                onValueChange = { onFormDegis(form.copy(tutarYazi = it, tutarHatali = false)) },
                label = { Text("Tutar (₺)") },
                isError = form.tutarHatali,
                supportingText = {
                    if (form.tutarHatali) Text(ALAN_HATA_MESAJI)
                },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal),
                singleLine = true,
                modifier = Modifier.fillMaxWidth()
            )

            OutlinedTextField(
                value = form.not,
                onValueChange = { onFormDegis(form.copy(not = it)) },
                label = { Text("Not (isteğe bağlı)") },
                modifier = Modifier.fillMaxWidth()
            )

            Button(
                onClick = onGuncelle,
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Güncelle")
            }
        }
    }

    // Compose'da diyalog gosterilmez, VAR ya da YOK olur.
    // Kosul true iken AlertDialog eklenir, false iken hic cizilmez.
    if (silmeOnayiGoster) {
        AlertDialog(
            // Disari tiklama ve geri tusu buraya duser.
            // Burada false yazmazsak diyalog kapanmaz.
            onDismissRequest = { onSilmeOnayiDegis(false) },
            title = { Text("Kaydı sil") },
            text = { Text("Bu kaydı silmek istediğinize emin misiniz?") },
            confirmButton = {
                TextButton(onClick = onSil) { Text("Sil") }
            },
            dismissButton = {
                TextButton(onClick = { onSilmeOnayiDegis(false) }) { Text("İptal") }
            }
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun KayitDetayIcerikPreview() {
    MotorumTheme {
        KayitDetayIcerik(
            form = KayitFormu(),
            silmeOnayiGoster = false,
            onFormDegis = {},
            onSilmeOnayiDegis = {},
            onSil = {},
            onGuncelle = {},
            onGeri = {}
        )
    }
}

package com.oguzhanp.motorum.ui.motorlarim

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Badge
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.TwoWheeler
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.oguzhanp.motorum.core.constants.AppSpacing
import com.oguzhanp.motorum.ui.components.EtiketliAlan
import com.oguzhanp.motorum.ui.theme.MotorumTheme

@Composable
fun MotorDetaySayfasi(
    navController: NavController,
    motorId: String?,
    secilsin: Boolean = false,
    viewModel: MotorDetayViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.baslat(motorId, secilsin) }

    LaunchedEffect(uiState.bitti) {
        if (uiState.bitti) navController.popBackStack()
    }

    MotorDetayIcerik(
        uiState = uiState,
        onFormDegis = viewModel::formDegis,
        onKaydetTikla = viewModel::kaydet,
        onGeriTikla = { navController.popBackStack() }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MotorDetayIcerik(
    uiState: MotorDetayUiState,
    onFormDegis: (MotorFormu) -> Unit,
    onKaydetTikla: () -> Unit,
    onGeriTikla: () -> Unit
) {
    val form = uiState.form

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TopAppBar(
                title = { Text(if (uiState.yeniMi) "Motor Ekle" else "Motoru Düzenle") },
                navigationIcon = {
                    IconButton(onClick = onGeriTikla) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Geri")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.background
                )
            )
        }
    ) { icPadding ->
        Column(
            modifier = Modifier
                .padding(icPadding)
                .verticalScroll(rememberScrollState())
                .padding(AppSpacing.normal),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            EtiketliAlan(
                etiket = "Marka",
                zorunlu = true,
                deger = form.marka,
                onDegis = { onFormDegis(form.copy(marka = it, markaHatali = false)) },
                ipucu = "Örn: Yamaha, Honda",
                ikon = Icons.Default.TwoWheeler,
                hatali = form.markaHatali,
                hataMetni = "Marka zorunlu",
                klavye = KeyboardOptions(capitalization = KeyboardCapitalization.Words)
            )

            EtiketliAlan(
                etiket = "Model",
                zorunlu = true,
                deger = form.model,
                onDegis = { onFormDegis(form.copy(model = it, modelHatali = false)) },
                ipucu = "Örn: MT-07, CRF 250",
                ikon = Icons.Default.Speed,
                hatali = form.modelHatali,
                hataMetni = "Model zorunlu",
                klavye = KeyboardOptions(capitalization = KeyboardCapitalization.Characters)
            )

            EtiketliAlan(
                etiket = "Plaka (isteğe bağlı)",
                deger = form.plaka,
                onDegis = { onFormDegis(form.copy(plaka = it)) },
                ipucu = "Örn: 34 BKR 102",
                ikon = Icons.Default.Badge,
                klavye = KeyboardOptions(
                    capitalization = KeyboardCapitalization.Characters,
                    keyboardType = KeyboardType.Text
                )
            )

            if (uiState.hata != null) {
                Text(
                    text = uiState.hata,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = onKaydetTikla,
                enabled = !uiState.calisiyor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp)
            ) {
                if (uiState.calisiyor) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Kaydet")
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun MotorDetayIcerikPreview() {
    MotorumTheme {
        MotorDetayIcerik(
            uiState = MotorDetayUiState(),
            onFormDegis = {},
            onKaydetTikla = {},
            onGeriTikla = {}
        )
    }
}

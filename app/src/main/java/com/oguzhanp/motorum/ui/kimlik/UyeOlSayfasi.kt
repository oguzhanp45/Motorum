package com.oguzhanp.motorum.ui.kimlik

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oguzhanp.motorum.core.constants.AppSpacing
import com.oguzhanp.motorum.ui.theme.MotorumTheme

@Composable
fun UyeOlSayfasi(
    viewModel: UyeOlViewModel,
    onGirisEGit: () -> Unit,
    onUyeOlBasarili: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(uiState.basarili) {
        if (uiState.basarili) {
            onUyeOlBasarili()
            viewModel.basariliTuketildi()
        }
    }

    UyeOlIcerik(
        uiState = uiState,
        onFormDegis = viewModel::formDegis,
        onUyeOlTikla = viewModel::uyeOl,
        onGirisEGit = onGirisEGit
    )
}

@Composable
fun UyeOlIcerik(
    uiState: UyeOlUiState,
    onFormDegis: (KimlikFormu) -> Unit,
    onUyeOlTikla: () -> Unit,
    onGirisEGit: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background)
            .safeDrawingPadding()
            .verticalScroll(rememberScrollState())
            .padding(horizontal = AppSpacing.normal, vertical = 24.dp),
        verticalArrangement = Arrangement.spacedBy(20.dp)
    ) {
        KimlikBasligi()

        KimlikSekmesi(
            girisSecili = false,
            onGirisTikla = onGirisEGit,
            onUyeOlTikla = {}
        )

        KimlikKarti(
            baslik = "Yeni Hesap Oluşturun",
            butonMetni = "Kayıt Ol",
            form = uiState.form,
            yukleniyor = uiState.yukleniyor,
            hata = uiState.hata,
            onFormDegis = onFormDegis,
            onGonderTikla = onUyeOlTikla
        )

        KimlikAltBaglantisi(
            soru = "Zaten bir hesabınız var mı?",
            baglanti = "Giriş Yap",
            onTikla = onGirisEGit
        )
    }
}

@Preview(showBackground = true)
@Composable
private fun UyeOlIcerikPreview() {
    MotorumTheme {
        UyeOlIcerik(
            uiState = UyeOlUiState(),
            onFormDegis = {},
            onUyeOlTikla = {},
            onGirisEGit = {}
        )
    }
}

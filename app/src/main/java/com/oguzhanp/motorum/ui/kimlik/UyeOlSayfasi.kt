package com.oguzhanp.motorum.ui.kimlik

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.compose.collectAsStateWithLifecycle
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
    KimlikEkrani(
        girisSecili = false,
        form = uiState.form,
        yukleniyor = uiState.yukleniyor,
        hata = uiState.hata,
        onFormDegis = onFormDegis,
        onGonderTikla = onUyeOlTikla,
        onDigerEkranaGec = onGirisEGit
    )
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun UyeOlIcerikPreview() {
    MotorumTheme(karanlik = false) {
        UyeOlIcerik(
            uiState = UyeOlUiState(),
            onFormDegis = {},
            onUyeOlTikla = {},
            onGirisEGit = {}
        )
    }
}

@Preview(widthDp = 390, heightDp = 844)
@Composable
private fun UyeOlIcerikKaranlikPreview() {
    MotorumTheme(karanlik = true) {
        UyeOlIcerik(
            uiState = UyeOlUiState(),
            onFormDegis = {},
            onUyeOlTikla = {},
            onGirisEGit = {}
        )
    }
}

package com.oguzhanp.motorum.feature.motorlarim

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.ReadOnlyComposable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oguzhanp.motorum.R
import com.oguzhanp.motorum.core.navigation.AnaBolgeKabugu
import com.oguzhanp.motorum.core.navigation.Routes
import com.oguzhanp.motorum.core.tasarim.AppSpacing
import com.oguzhanp.motorum.core.tasarim.BosDurum
import com.oguzhanp.motorum.core.tasarim.BosGorsel
import com.oguzhanp.motorum.core.tasarim.MotorcuYukleniyor
import com.oguzhanp.motorum.core.tasarim.MotorumIkonlari
import com.oguzhanp.motorum.core.tasarim.MotorumTheme
import com.oguzhanp.motorum.core.tasarim.MurekkepDugme
import com.oguzhanp.motorum.data.ortak.INTERNET_YOK
import com.oguzhanp.motorum.model.Motor

@Composable
fun MotorlarimSayfasi(
    onSekmeTikla: (String) -> Unit,
    onMotorEkleTikla: () -> Unit,
    onMotorDuzenleTikla: (String) -> Unit,
    viewModel: MotorlarimViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) { viewModel.yukle() }

    // Gecis burada, tiklamanin icinde degil. Secimi kaydetmek diske yazma
    // iceriyor; tiklar tiklamaz gitseydik ana sayfa kayitlari HENUZ eski motor
    // kimligiyle cekebilirdi. Bayrak kalkinca secim gercekten tamamlanmis oluyor.
    LaunchedEffect(uiState.secimTamam) {
        if (uiState.secimTamam) {
            onSekmeTikla(Routes.ANA_SAYFA)
            viewModel.secimTuketildi()
        }
    }

    MotorlarimIcerik(
        uiState = uiState,
        onSekmeTikla = onSekmeTikla,
        onMotorTikla = viewModel::motoruSec,
        onMotorEkleTikla = onMotorEkleTikla,
        onMotorDuzenleTikla = onMotorDuzenleTikla,
        onSilTikla = viewModel::silmeOnayiAc,
        onSilmeIptal = viewModel::silmeOnayiKapat,
        onSilOnayla = viewModel::sil,
        onTekrarDeneTikla = viewModel::yukle
    )
}

@Composable
fun MotorlarimIcerik(
    uiState: MotorlarimUiState,
    onSekmeTikla: (String) -> Unit,
    onMotorTikla: (String) -> Unit,
    onMotorEkleTikla: () -> Unit,
    onMotorDuzenleTikla: (String) -> Unit,
    onSilTikla: (Motor) -> Unit,
    onSilmeIptal: () -> Unit,
    onSilOnayla: (String) -> Unit,
    onTekrarDeneTikla: () -> Unit
) {
    AnaBolgeKabugu(
        baslik = stringResource(R.string.sekme_motorlarim),
        seciliRota = Routes.MOTORLARIM,
        onSekmeTikla = onSekmeTikla
    ) { icPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(icPadding)
                .padding(horizontal = AppSpacing.normal)
        ) {
            // Liste weight(1f) aliyor, buton onun altinda: boylece buton alt
            // barin hemen ustunde SABIT duruyor, listeyle birlikte kaymiyor.
            Box(modifier = Modifier.weight(1f)) {
                when {
                    uiState.yukleniyor -> MotorcuYukleniyor(
                        mesaj = stringResource(R.string.motorlar_geliyor),
                        modifier = Modifier.align(Alignment.Center)
                    )

                    uiState.hata != null -> BosDurum(
                        gorsel = BosGorsel.BAGLANTI_YOK,
                        baslik = uiState.hata,
                        aciklama = if (uiState.hata == INTERNET_YOK) {
                            stringResource(R.string.cevrimdisi_motor_aciklama)
                        } else {
                            null
                        },
                        eylem = stringResource(R.string.tekrar_dene),
                        eylemIkonu = MotorumIkonlari.Yenile,
                        onEylem = onTekrarDeneTikla,
                        anaEylem = false,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    uiState.motorlar.isEmpty() -> BosDurum(
                        gorsel = BosGorsel.MOTOR_YOK,
                        baslik = stringResource(R.string.motor_yok_baslik),
                        aciklama = stringResource(R.string.motor_yok_aciklama),
                        eylem = stringResource(R.string.motor_ekle),
                        eylemIkonu = MotorumIkonlari.Ekle,
                        onEylem = onMotorEkleTikla,
                        modifier = Modifier.align(Alignment.Center)
                    )

                    else -> LazyColumn(
                        verticalArrangement = Arrangement.spacedBy(12.dp),
                        contentPadding = PaddingValues(bottom = 8.dp)
                    ) {
                        items(uiState.motorlar, key = { it.id }) { motor ->
                            MotorKarti(
                                motor = motor,
                                secili = motor.id == uiState.seciliMotorId,
                                onTikla = { onMotorTikla(motor.id) },
                                onDuzenleTikla = { onMotorDuzenleTikla(motor.id) },
                                onSilTikla = { onSilTikla(motor) },
                                modifier = Modifier.fillMaxWidth()
                            )
                        }
                    }
                }
            }

            // Liste bosken alttaki dugme gizli: bos durumun kendi "Motor Ekle"
            // dugmesi var, ekranda ayni isi yapan iki dugme durmasin.
            val listeDolu = !uiState.yukleniyor && uiState.hata == null && uiState.motorlar.isNotEmpty()
            if (listeDolu) {
                MurekkepDugme(
                    onClick = onMotorEkleTikla,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp)
                        .padding(top = 0.dp)
                ) {
                    Icon(MotorumIkonlari.Ekle, contentDescription = null, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(8.dp))
                    Text(stringResource(R.string.motor_ekle))
                }
            }

            Spacer(Modifier.height(12.dp))
        }
    }

    val silinecek = uiState.silinecekMotor
    if (silinecek != null) {
        AlertDialog(
            onDismissRequest = onSilmeIptal,
            title = { Text(stringResource(R.string.motoru_sil)) },
            text = { Text(silmeMetni(silinecek, uiState.silinecekKayitSayisi)) },
            confirmButton = {
                TextButton(onClick = { onSilOnayla(silinecek.id) }) {
                    Text(stringResource(R.string.sil), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = onSilmeIptal) { Text(stringResource(R.string.iptal)) }
            }
        )
    }
}

// Kayit sayisi dile gore tekil ya da cogul yaziliyor (Ingilizce
// "1 entry" / "3 entries"); kayit yoksa cumle tamamen degisiyor.
@Composable
@ReadOnlyComposable
private fun silmeMetni(motor: Motor, kayitSayisi: Int): String =
    if (kayitSayisi > 0) {
        pluralStringResource(R.plurals.motor_sil_onayi, kayitSayisi, motor.adi, kayitSayisi)
    } else {
        stringResource(R.string.motor_sil_onayi_bos, motor.adi)
    }

@Preview(showBackground = true)
@Composable
private fun MotorlarimIcerikPreview() {
    MotorumTheme {
        MotorlarimIcerik(
            uiState = MotorlarimUiState(),
            onSekmeTikla = {},
            onMotorTikla = {},
            onMotorEkleTikla = {},
            onMotorDuzenleTikla = {},
            onSilTikla = {},
            onSilmeIptal = {},
            onSilOnayla = {},
            onTekrarDeneTikla = {}
        )
    }
}

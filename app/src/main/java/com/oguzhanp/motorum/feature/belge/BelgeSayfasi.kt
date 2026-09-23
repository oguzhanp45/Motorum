package com.oguzhanp.motorum.feature.belge

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ExperimentalLayoutApi
import androidx.compose.foundation.layout.FlowRow
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.res.pluralStringResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.oguzhanp.motorum.R
import com.oguzhanp.motorum.core.tasarim.Anahtar
import com.oguzhanp.motorum.core.tasarim.AppMotion
import com.oguzhanp.motorum.core.tasarim.AppShape
import com.oguzhanp.motorum.core.tasarim.AppSpacing
import com.oguzhanp.motorum.core.tasarim.Inter
import com.oguzhanp.motorum.core.tasarim.KartZemin
import com.oguzhanp.motorum.core.tasarim.Kenar
import com.oguzhanp.motorum.core.tasarim.MetinAna
import com.oguzhanp.motorum.core.tasarim.MetinIkincil
import com.oguzhanp.motorum.core.tasarim.MetinSolgun
import com.oguzhanp.motorum.core.tasarim.MotorumIkonlari
import com.oguzhanp.motorum.core.tasarim.MotorumTheme
import com.oguzhanp.motorum.core.tasarim.Murekkep
import com.oguzhanp.motorum.core.tasarim.MurekkepDugme
import com.oguzhanp.motorum.core.tasarim.MurekkepUstu
import com.oguzhanp.motorum.core.tasarim.SekmeZemin
import com.oguzhanp.motorum.feature.kayit.bilesenler.TarihSecici
import com.oguzhanp.motorum.model.BelgeTuru

// Belge ekleme ve duzenleme sayfasi (tasarim: Belgeler ③).
@Composable
fun BelgeSayfasi(
    navController: NavController,
    viewModel: BelgeViewModel = hiltViewModel()
) {
    val durum by viewModel.uiState.collectAsStateWithLifecycle()

    LaunchedEffect(durum.bitti) {
        if (durum.bitti) navController.popBackStack()
    }

    BelgeIcerik(
        durum = durum,
        onFormDegis = viewModel::formDegis,
        onKaydet = viewModel::kaydet,
        onSilmeOnayiDegis = viewModel::silmeOnayiDegis,
        onSil = viewModel::sil,
        onGeri = { navController.popBackStack() }
    )
}

@OptIn(ExperimentalMaterial3Api::class, ExperimentalLayoutApi::class)
@Composable
fun BelgeIcerik(
    durum: BelgeUiState,
    onFormDegis: (BelgeFormu) -> Unit,
    onKaydet: () -> Unit,
    onSilmeOnayiDegis: (Boolean) -> Unit,
    onSil: () -> Unit,
    onGeri: () -> Unit
) {
    val form = durum.form
    val duzenleme = durum.duzenlenenId != null

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(if (duzenleme) R.string.belgeyi_duzenle else R.string.belge_ekle_baslik)) },
                navigationIcon = {
                    IconButton(onClick = onGeri) {
                        Icon(MotorumIkonlari.Geri, contentDescription = stringResource(R.string.geri))
                    }
                },
                actions = {
                    if (duzenleme) {
                        IconButton(onClick = { onSilmeOnayiDegis(true) }, enabled = !durum.calisiyor) {
                            Icon(MotorumIkonlari.Sil, contentDescription = stringResource(R.string.sil))
                        }
                    }
                }
            )
        }
    ) { icPadding ->
        Column(
            modifier = Modifier
                .padding(icPadding)
                .verticalScroll(rememberScrollState())
                .padding(AppSpacing.normal),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Bolum(stringResource(R.string.bolum_belge_turu)) {
                FlowRow(
                    horizontalArrangement = Arrangement.spacedBy(6.dp),
                    verticalArrangement = Arrangement.spacedBy(6.dp)
                ) {
                    BelgeTuru.entries.forEach { tur ->
                        SecimCipi(
                            metin = stringResource(tur.ad),
                            secili = form.tur == tur,
                            onTikla = { onFormDegis(form.copy(tur = tur, adHatali = false)) }
                        )
                    }
                }
            }

            if (form.tur == BelgeTuru.DIGER) {
                OutlinedTextField(
                    value = form.ad,
                    onValueChange = { onFormDegis(form.copy(ad = it, adHatali = false)) },
                    label = { Text(stringResource(R.string.belge_adi)) },
                    placeholder = { Text(stringResource(R.string.belge_adi_ipucu), color = MetinSolgun) },
                    isError = form.adHatali,
                    supportingText = { if (form.adHatali) Text(stringResource(R.string.belge_adi_zorunlu)) },
                    singleLine = true,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Tekil turde ikinci belge acilmiyor, eskisinin yerine geciyor.
            // Kullanici bunu kaydetmeden once bilsin.
            if (form.tur.tekil && form.tur in durum.kayitliTurler && durum.duzenlenenId != form.tur.name) {
                Text(
                    text = stringResource(R.string.belge_var_uyarisi, stringResource(form.tur.ad).lowercase()),
                    style = MaterialTheme.typography.bodySmall,
                    color = MetinIkincil
                )
            }

            TarihSecici(
                tarihMillis = form.bitisMillis,
                onTarihSec = { onFormDegis(form.copy(bitisMillis = it)) },
                etiket = stringResource(R.string.bitis_tarihi),
                aciklama = stringResource(R.string.bitis_tarihi_aciklama),
                modifier = Modifier.fillMaxWidth()
            )

            YenilemeBlogu(form = form, onFormDegis = onFormDegis)

            Bolum(stringResource(R.string.bolum_kac_gun_once)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    HATIRLATMA_GUNLERI.forEach { gun ->
                        SecimCipi(
                            metin = "$gun",
                            secili = form.kacGunOnce == gun,
                            onTikla = { onFormDegis(form.copy(kacGunOnce = gun)) }
                        )
                    }
                }
            }

            if (durum.hata != null) {
                Text(
                    text = durum.hata,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            MurekkepDugme(
                onClick = onKaydet,
                enabled = !durum.calisiyor && !durum.yukleniyor,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (durum.calisiyor) {
                    CircularProgressIndicator(modifier = Modifier.size(20.dp), strokeWidth = 2.dp)
                } else {
                    Text(stringResource(R.string.kaydet))
                }
            }
        }
    }

    if (durum.silmeOnayi) {
        AlertDialog(
            onDismissRequest = { onSilmeOnayiDegis(false) },
            title = { Text(stringResource(R.string.belgeyi_sil)) },
            text = { Text(stringResource(R.string.belge_sil_onayi)) },
            confirmButton = {
                TextButton(onClick = onSil) {
                    Text(stringResource(R.string.sil), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { onSilmeOnayiDegis(false) }) { Text(stringResource(R.string.iptal)) }
            }
        )
    }
}

// "Suresi dolunca yenile": kapaliyken tek satir, acilinca sure cipleri geliyor.
// Hatirlatma blogu ile ayni kalip.
@Composable
private fun YenilemeBlogu(form: BelgeFormu, onFormDegis: (BelgeFormu) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .border(1.5.dp, Kenar, RoundedCornerShape(14.dp))
            .padding(horizontal = 13.dp, vertical = 12.dp),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(11.dp)
        ) {
            Icon(MotorumIkonlari.Yenile, contentDescription = null, tint = MetinAna, modifier = Modifier.size(17.dp))
            Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(2.dp)) {
                Text(
                    text = stringResource(R.string.suresi_dolunca_yenile),
                    style = TextStyle(fontFamily = Inter, fontSize = 12.5.sp, fontWeight = FontWeight.ExtraBold),
                    color = MetinAna
                )
                Text(
                    text = stringResource(R.string.yenile_aciklama),
                    style = TextStyle(fontFamily = Inter, fontSize = 10.sp, fontWeight = FontWeight.SemiBold),
                    color = MetinSolgun
                )
            }
            Anahtar(
                acik = form.yenileAcik,
                onDegis = { onFormDegis(form.copy(yenileAcik = it, ozelAyHatali = false)) }
            )
        }

        AnimatedVisibility(
            visible = form.yenileAcik,
            enter = expandVertically(tween(AppMotion.PANEL, easing = AppMotion.egri), expandFrom = Alignment.Top) +
                    fadeIn(tween(AppMotion.PANEL)),
            exit = shrinkVertically(tween(AppMotion.PANEL, easing = AppMotion.egri), shrinkTowards = Alignment.Top) +
                    fadeOut(tween(AppMotion.PANEL))
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    YENILEME_SURELERI.forEach { ay ->
                        SecimCipi(
                            metin = pluralStringResource(R.plurals.yenile_yil, ay / 12, ay / 12),
                            secili = form.yenileAy == ay,
                            kucuk = true,
                            onTikla = { onFormDegis(form.copy(yenileAy = ay, ozelAyHatali = false)) }
                        )
                    }
                    SecimCipi(
                        metin = stringResource(R.string.aralik_ozel),
                        secili = form.yenileAy == null,
                        kucuk = true,
                        onTikla = { onFormDegis(form.copy(yenileAy = null)) }
                    )
                }
                // Ozel: sureyi ay olarak kullanici yaziyor (orn. 6 ay, 3 yil = 36).
                if (form.yenileAy == null) {
                    OutlinedTextField(
                        value = form.ozelAyYazi,
                        onValueChange = { yazi ->
                            onFormDegis(form.copy(ozelAyYazi = yazi.filter(Char::isDigit).take(3), ozelAyHatali = false))
                        },
                        label = { Text(stringResource(R.string.kac_ayda_bir)) },
                        isError = form.ozelAyHatali,
                        supportingText = { if (form.ozelAyHatali) Text(stringResource(R.string.ozel_ay_hatasi)) },
                        keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                        singleLine = true,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            }
        }
    }
}

// Kucuk buyuk harfli bolum etiketi + altinda icerik.
@Composable
private fun Bolum(etiket: String, icerik: @Composable () -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(
            text = etiket,
            style = TextStyle(fontFamily = Inter, fontSize = 10.sp, fontWeight = FontWeight.ExtraBold, letterSpacing = 0.7.sp),
            color = MetinSolgun
        )
        icerik()
    }
}

// Secim cipi: secili olan murekkep dolgulu. Hatirlatma blogundaki aralik
// cipleriyle ayni dil.
@Composable
private fun SecimCipi(metin: String, secili: Boolean, onTikla: () -> Unit, kucuk: Boolean = false) {
    Text(
        text = metin,
        style = TextStyle(
            fontFamily = Inter,
            fontSize = if (kucuk) 10.5.sp else 11.sp,
            fontWeight = if (secili) FontWeight.ExtraBold else FontWeight.Bold
        ),
        color = if (secili) MurekkepUstu else MetinIkincil,
        modifier = Modifier
            .clip(AppShape.cip)
            .background(if (secili) Murekkep else KartZemin)
            .border(1.4.dp, if (secili) Murekkep else SekmeZemin, AppShape.cip)
            .clickable(onClick = onTikla)
            .padding(horizontal = if (kucuk) 11.dp else 12.dp, vertical = if (kucuk) 6.dp else 7.dp)
    )
}

@Preview(showBackground = true)
@Composable
private fun BelgeIcerikPreview() {
    MotorumTheme {
        BelgeIcerik(
            durum = BelgeUiState(form = BelgeFormu(yenileAcik = true), yukleniyor = false),
            onFormDegis = {}, onKaydet = {}, onSilmeOnayiDegis = {}, onSil = {}, onGeri = {}
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF07090E)
@Composable
private fun BelgeIcerikKaranlikPreview() {
    MotorumTheme(karanlik = true) {
        BelgeIcerik(
            durum = BelgeUiState(
                form = BelgeFormu(tur = BelgeTuru.DIGER, ad = "Ehliyet", yenileAcik = true, yenileAy = null),
                duzenlenenId = "x",
                yukleniyor = false
            ),
            onFormDegis = {}, onKaydet = {}, onSilmeOnayiDegis = {}, onSil = {}, onGeri = {}
        )
    }
}

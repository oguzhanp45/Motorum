package com.oguzhanp.motorum.ui.detay

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
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
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.oguzhanp.motorum.core.constants.AppShape
import com.oguzhanp.motorum.core.constants.AppSpacing
import com.oguzhanp.motorum.model.HatirlatmaDurumu
import com.oguzhanp.motorum.model.Kayit
import com.oguzhanp.motorum.model.TripNoktasi
import com.oguzhanp.motorum.ui.form.AksesuarAlanlari
import com.oguzhanp.motorum.ui.components.bildirimIzniVerildiMi
import com.oguzhanp.motorum.ui.components.rememberBildirimIzni
import com.oguzhanp.motorum.ui.form.BakimAlanlari
import com.oguzhanp.motorum.ui.form.KayitFormu
import com.oguzhanp.motorum.ui.form.RoadTripAlanlari
import com.oguzhanp.motorum.ui.form.YakitAlanlari
import com.oguzhanp.motorum.ui.home.KayitViewModel
import com.oguzhanp.motorum.ui.home.gorunum
import com.oguzhanp.motorum.ui.theme.BakimMetin
import com.oguzhanp.motorum.ui.theme.BakimRenk
import com.oguzhanp.motorum.ui.theme.BakimZemin
import com.oguzhanp.motorum.ui.theme.MotorumTheme
import com.oguzhanp.motorum.ui.theme.Murekkep
import com.oguzhanp.motorum.ui.theme.MurekkepUstu
import com.oguzhanp.motorum.util.dakikaAl
import com.oguzhanp.motorum.util.formatGunAy
import com.oguzhanp.motorum.util.formatSaat
import com.oguzhanp.motorum.util.saatAl
import com.oguzhanp.motorum.util.sonKmOkumasi
import com.oguzhanp.motorum.util.tarihSaatBirlestir
import com.oguzhanp.motorum.ui.components.MotorumIkonlari

@Composable
fun KayitDetaySayfasi(
    kayitViewModel: KayitViewModel,
    navController: NavController,
    kayitId: String,
    detayViewModel: KayitDetayViewModel = hiltViewModel()
) {
    val kayitUiState by kayitViewModel.uiState.collectAsStateWithLifecycle()
    val kayit = kayitUiState.kayitlar.find { it.id == kayitId }

    if (kayit == null) {
        LaunchedEffect(Unit) { navController.popBackStack() }
        return
    }

    LaunchedEffect(kayit.id) { detayViewModel.baslat(kayit) }

    val detay by detayViewModel.uiState.collectAsStateWithLifecycle()
    val baglam = LocalContext.current

    var bildirimIzniVar by remember { mutableStateOf(bildirimIzniVerildiMi(baglam)) }

    // Izin verilsin verilmesin hatirlatma alanini aciyoruz: tarih yine
    // kaydedilecek, izin yoksa formda uyari cikiyor.
    val bildirimIzniIste = rememberBildirimIzni { verildi ->
        bildirimIzniVar = verildi
        (detay.form as? KayitFormu.Bakim)?.let { bakim ->
            detayViewModel.formDegis(bakim.hatirlatmayiAc())
        }
    }

    LaunchedEffect(detay.bitti) {
        if (detay.bitti) navController.popBackStack()
    }

    // Duzenlenen kaydin kendi okumasi haric, en yuksek sayac degeri: kaydin
    // kendisiyle karsilastirip bos yere uyari vermeyelim.
    val sonOkuma = remember(kayitUiState.kayitlar, kayitId) {
        sonKmOkumasi(kayitUiState.kayitlar.filterNot { it.id == kayitId })
    }

    // Zamani gelmis bakimda ustte kehribar serit. Dugmeler kaydi hemen
    // degistiriyor; formdaki degerler eskidigi icin sayfa kapaniyor, sonuc
    // ana sayfadaki cipte gorunuyor.
    val zamaniGelen = (kayit as? Kayit.Bakim)
        ?.takeIf { it.hatirlatmaDurumu() == HatirlatmaDurumu.ZAMANI_GELDI }

    KayitDetayIcerik(
        zamaniGelenHatirlatma = zamaniGelen?.hatirlatmaMillis,
        onYaptirdim = {
            zamaniGelen?.let { kayitViewModel.hatirlatmaYaptirdim(it) }
            navController.popBackStack()
        },
        onErtele = {
            zamaniGelen?.let { kayitViewModel.hatirlatmaErtele(it) }
            navController.popBackStack()
        },
        form = detay.form,
        sonOkuma = sonOkuma,
        silmeOnayiGoster = detay.silmeOnayiGoster,
        calisiyor = detay.calisiyor,
        hata = detay.hata,
        bildirimIzniVar = bildirimIzniVar,
        onHatirlatmaAcilsin = bildirimIzniIste,
        onFormDegis = detayViewModel::formDegis,
        onSilmeOnayiDegis = detayViewModel::silmeOnayiDegis,
        onSilOnayla = { detayViewModel.sil(kayitId) },
        onGeriTikla = { navController.popBackStack() },
        onGuncelleTikla = {
            val kontrol = detay.form.dogrula()
            if (kontrol.gecerli) {
                // Form gecerliyse kayit nesnesi uretiliyor. litre/tutar sadece Yakit'te
                // oldugu icin tip daraltmadan erisilemiyor.
                val guncel: Kayit = when (kontrol) {
                    is KayitFormu.Yakit -> Kayit.Yakit(
                        id = kayitId,
                        tarihMillis = kontrol.tarihMillis,
                        litre = kontrol.litre!!,
                        tutar = kontrol.tutar!!,
                        not = kontrol.not.trim(),
                        km = kontrol.km
                    )

                    is KayitFormu.RoadTrip -> Kayit.RoadTrip(
                        id = kayitId,
                        tutar = kontrol.masraf,
                        not = kontrol.not.trim(),
                        baslangic = TripNoktasi(
                            tarihMillis = tarihSaatBirlestir(
                                kontrol.baslangic.tarihMillis,
                                kontrol.baslangic.saat,
                                kontrol.baslangic.dakika
                            ),
                            km = kontrol.baslangic.km!!,
                            sehir = kontrol.baslangic.sehir.trim()
                        ),
                        // Bitis bolumu bos birakildiysa yolculuk devam ediyor: null yaziliyor.
                        bitis = if (kontrol.bitisVar) TripNoktasi(
                            tarihMillis = tarihSaatBirlestir(
                                kontrol.bitis.tarihMillis,
                                kontrol.bitis.saat,
                                kontrol.bitis.dakika
                            ),
                            km = kontrol.bitis.km!!,
                            sehir = kontrol.bitis.sehir.trim()
                        ) else null,
                        molalar = kontrol.doluMolalar
                    )

                    is KayitFormu.Bakim -> Kayit.Bakim(
                        id = kayitId,
                        tarihMillis = kontrol.tarihMillis,
                        tutar = kontrol.tutar!!,
                        not = kontrol.not.trim(),
                        bakimTuru = kontrol.bakimTuru.trim(),
                        hatirlatmaMillis = kontrol.hatirlatmaMillis,
                        // Hatirlatma degismediyse "yapildi" isareti korunuyor.
                        hatirlatmaYapildiMillis = kontrol.korunanYapildi
                    )

                    is KayitFormu.Aksesuar -> Kayit.Aksesuar(
                        id = kayitId,
                        tarihMillis = kontrol.tarihMillis,
                        tutar = kontrol.tutar!!,
                        not = kontrol.not.trim(),
                        aksesuarAdi = kontrol.aksesuarAdi.trim()
                    )
                }
                detayViewModel.guncelle(guncel)
            } else {
                detayViewModel.formDegis(kontrol)
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KayitDetayIcerik(
    form: KayitFormu,
    sonOkuma: Int?,
    silmeOnayiGoster: Boolean,
    calisiyor: Boolean,
    hata: String?,
    bildirimIzniVar: Boolean,
    onHatirlatmaAcilsin: () -> Unit,
    onFormDegis: (KayitFormu) -> Unit,
    onSilmeOnayiDegis: (Boolean) -> Unit,// Diyalogu acmak ve iptal etmek ayni islem.true/false yapmak.
    onSilOnayla: () -> Unit,
    onGuncelleTikla: () -> Unit,
    onGeriTikla: () -> Unit,
    // Zamani gelmis hatirlatma varsa zamani; yoksa null ve serit cikmiyor.
    zamaniGelenHatirlatma: Long? = null,
    onYaptirdim: () -> Unit = {},
    onErtele: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Kayıt Detayı") },
                navigationIcon = {
                    IconButton(onClick = onGeriTikla) {
                        Icon(MotorumIkonlari.Geri, contentDescription = "Geri")
                    }
                },
                actions = {
                    IconButton(
                        onClick = { onSilmeOnayiDegis(true) },
                        enabled = !calisiyor
                    ) {
                        Icon(MotorumIkonlari.Sil, contentDescription = "Sil")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(AppSpacing.normal),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            if (zamaniGelenHatirlatma != null) {
                ZamaniGeldiSeridi(
                    zaman = zamaniGelenHatirlatma,
                    onYaptirdim = onYaptirdim,
                    onErtele = onErtele
                )
            }

            Text(
                text = "Kategori: ${form.kategori.etiket}",
                style = MaterialTheme.typography.bodyLarge
            )

            // Alan blogu kategoriye gore secilir. Yeni kategori eklendiginde
            // derleyici bu when'in eksik oldugunu gosterir.
            when (form) {
                is KayitFormu.Yakit -> YakitAlanlari(
                    form = form,
                    onDegis = onFormDegis,
                    sonOkuma = sonOkuma,
                    modifier = Modifier.fillMaxWidth()
                )

                is KayitFormu.RoadTrip -> RoadTripAlanlari(
                    form = form,
                    onDegis = onFormDegis,
                    molaGoster = true,
                    bitisGoster = true,
                    sonOkuma = sonOkuma,
                    modifier = Modifier.fillMaxWidth()
                )

                is KayitFormu.Bakim -> BakimAlanlari(
                    form = form,
                    onDegis = onFormDegis,
                    bildirimIzniVar = bildirimIzniVar,
                    onHatirlatmaAcilsin = onHatirlatmaAcilsin,
                    modifier = Modifier.fillMaxWidth()
                )

                is KayitFormu.Aksesuar -> AksesuarAlanlari(
                    form = form,
                    onDegis = onFormDegis,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            OutlinedTextField(
                value = form.not,
                onValueChange = { onFormDegis(form.notDegistir(it)) },
                label = { Text(gorunum(form.kategori).notEtiketi) },
                modifier = Modifier.fillMaxWidth()
            )

            if (hata != null) {
                Text(
                    text = hata,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = onGuncelleTikla,
                enabled = !calisiyor,
                modifier = Modifier.fillMaxWidth()
            ) {
                if (calisiyor) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(20.dp),
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary
                    )
                } else {
                    Text("Güncelle")
                }
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
                TextButton(onClick = onSilOnayla) { Text("Sil") }
            },
            dismissButton = {
                TextButton(onClick = { onSilmeOnayiDegis(false) }) { Text("İptal") }
            }
        )
    }
}

// Kehribar serit: detayi acan kullanici zamani gelen bakimi kacirmasin.
// Paneldeki ilk iki eylem burada da var; "kapat" icin formdaki anahtar yeterli.
@Composable
private fun ZamaniGeldiSeridi(zaman: Long, onYaptirdim: () -> Unit, onErtele: () -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(AppShape.alan)
            .background(BakimZemin)
            .padding(horizontal = 14.dp, vertical = AppSpacing.orta)
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            Icon(
                MotorumIkonlari.Bildirim,
                contentDescription = null,
                tint = BakimRenk,
                modifier = Modifier.size(18.dp)
            )
            Column {
                Text(
                    text = "Zamanı geldi",
                    style = MaterialTheme.typography.labelLarge,
                    fontWeight = FontWeight.Bold,
                    color = BakimMetin
                )
                Text(
                    text = "${formatGunAy(zaman)}, ${formatSaat(saatAl(zaman), dakikaAl(zaman))}",
                    style = MaterialTheme.typography.bodySmall,
                    color = BakimMetin
                )
            }
        }
        Spacer(Modifier.height(10.dp))
        Row(horizontalArrangement = Arrangement.spacedBy(AppSpacing.kucuk)) {
            Button(
                onClick = onYaptirdim,
                shape = AppShape.alan,
                colors = ButtonDefaults.buttonColors(containerColor = Murekkep, contentColor = MurekkepUstu)
            ) {
                Icon(MotorumIkonlari.Onay, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(Modifier.size(6.dp))
                Text("Yaptırdım")
            }
            TextButton(onClick = onErtele) {
                Text("1 hafta ertele", color = BakimMetin, fontWeight = FontWeight.SemiBold)
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun KayitDetayIcerikPreview() {
    MotorumTheme {
        KayitDetayIcerik(
            form = KayitFormu.Yakit(),
            sonOkuma = null,
            silmeOnayiGoster = false,
            calisiyor = false,
            hata = null,
            bildirimIzniVar = true,
            onHatirlatmaAcilsin = {},
            onFormDegis = {},
            onSilmeOnayiDegis = {},
            onSilOnayla = {},
            onGuncelleTikla = {},
            onGeriTikla = {}
        )
    }
}

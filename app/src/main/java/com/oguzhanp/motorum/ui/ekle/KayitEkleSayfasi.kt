package com.oguzhanp.motorum.ui.ekle

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardCapitalization
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.sp
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.NavController
import com.oguzhanp.motorum.R
import com.oguzhanp.motorum.core.constants.AppShape
import com.oguzhanp.motorum.core.constants.AppSpacing
import com.oguzhanp.motorum.ui.components.MurekkepDugme
import com.oguzhanp.motorum.ui.components.yukselerekGir
import com.oguzhanp.motorum.ui.ekle.components.SonKayitSeridi
import com.oguzhanp.motorum.model.Kayit
import com.oguzhanp.motorum.model.TripNoktasi
import com.oguzhanp.motorum.ui.ekle.components.KategoriDropdown
import com.oguzhanp.motorum.ui.form.AksesuarAlanlari
import com.oguzhanp.motorum.ui.components.bildirimIzniVerildiMi
import com.oguzhanp.motorum.ui.components.rememberBildirimIzni
import com.oguzhanp.motorum.ui.form.BakimAlanlari
import com.oguzhanp.motorum.ui.form.KayitFormu
import com.oguzhanp.motorum.ui.form.RoadTripAlanlari
import com.oguzhanp.motorum.ui.form.YakitAlanlari
import com.oguzhanp.motorum.ui.form.bosForm
import com.oguzhanp.motorum.ui.home.gorunum
import com.oguzhanp.motorum.ui.theme.MotorumTheme
import com.oguzhanp.motorum.ui.theme.MurekkepUstu
import com.oguzhanp.motorum.ui.home.KayitViewModel
import com.oguzhanp.motorum.util.sonKmOkumasi
import com.oguzhanp.motorum.util.tarihSaatBirlestir
import com.oguzhanp.motorum.ui.components.MotorumIkonlari

@Composable
fun KayitEkleSayfasi(
    navController: NavController,
    // Ana sayfanin listesi burada sadece bir sey icin gerekiyor: en son sayac
    // okumasini bilip formda uyari gosterebilmek.
    kayitViewModel: KayitViewModel,
    ekleViewModel: KayitEkleViewModel = hiltViewModel()
) {
    val uiState by ekleViewModel.uiState.collectAsStateWithLifecycle()
    val kayitUiState by kayitViewModel.uiState.collectAsStateWithLifecycle()
    val sonOkuma = remember(kayitUiState.kayitlar) { sonKmOkumasi(kayitUiState.kayitlar) }
    val baglam = LocalContext.current

    var bildirimIzniVar by remember { mutableStateOf(bildirimIzniVerildiMi(baglam)) }

    // Sablon cipi formu kendisi dolduruyor; bu kapi sadece izni istiyor,
    // sonuc gelince formun ustune yazmiyor.
    val sadeceIzinIste = rememberBildirimIzni { verildi -> bildirimIzniVar = verildi }

    // Izin verilsin verilmesin hatirlatma alanini aciyoruz: tarih yine
    // kaydedilecek, izin yoksa formda uyari cikiyor.
    val bildirimIzniIste = rememberBildirimIzni { verildi ->
        bildirimIzniVar = verildi
        (uiState.form as? KayitFormu.Bakim)?.let { bakim ->
            ekleViewModel.guncelle(uiState.copy(form = bakim.hatirlatmayiAc()))
        }
    }

    LaunchedEffect(uiState.basarili) {
        if (uiState.basarili) navController.popBackStack()
    }

    // "Tekrarla"dan gelindiyse liste hazir olunca form bir kez dolduruluyor.
    LaunchedEffect(kayitUiState.kayitlar) { ekleViewModel.kopyaUygula(kayitUiState.kayitlar) }

    // Secili kategorinin en son kaydi: "son kayittan doldur" seridi icin.
    val sonKayit = remember(kayitUiState.kayitlar, uiState.form.kategori) {
        kayitUiState.kayitlar
            .filter { it.kategori == uiState.form.kategori }
            .maxByOrNull { it.tarihMillis }
    }
    // Serit bir kategoride kullanilinca o kategoride bir daha cikmiyor.
    var doldurulanKategori by rememberSaveable { mutableStateOf<String?>(null) }
    val seritOzeti = sonKayit
        ?.takeIf { !ekleViewModel.kopyadan && doldurulanKategori != uiState.form.kategori.name }
        ?.let { kayitOzeti(it) }

    KayitEkleIcerik(
        form = uiState.form,
        kaydediliyor = uiState.kaydediliyor,
        hata = uiState.hata,
        sonOkuma = sonOkuma,
        bildirimIzniVar = bildirimIzniVar,
        onHatirlatmaAcilsin = bildirimIzniIste,
        onHatirlatmaIzniIste = sadeceIzinIste,
        seritOzeti = seritOzeti,
        onSeritTikla = {
            sonKayit?.let { kayit ->
                doldurulanKategori = kayit.kategori.name
                ekleViewModel.guncelle(uiState.copy(form = kayittanForm(kayit)))
            }
        },
        onFormDegis = { yeniForm -> ekleViewModel.guncelle(uiState.copy(form = yeniForm)) },
        onGeriTikla = { navController.popBackStack() },
        onKaydetTikla = {
            val kontrol = uiState.form.dogrula()
            if (kontrol.gecerli) {
                // Form gecerliyse kayit nesnesi uretiliyor. litre/tutar sadece Yakit'te
                // oldugu icin tip daraltmadan erisilemiyor.
                val kayit: Kayit = when (kontrol) {
                    is KayitFormu.Yakit -> Kayit.Yakit(
                        tarihMillis = kontrol.tarihMillis,
                        litre = kontrol.litre!!,
                        tutar = kontrol.tutar!!,
                        not = kontrol.not.trim(),
                        // Bos birakildiysa null gidiyor: "girilmedi" ile "sifir km"
                        // ayri seyler.
                        km = kontrol.km
                    )

                    is KayitFormu.RoadTrip -> Kayit.RoadTrip(
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
                        molalar = kontrol.doluMolalar
                    )

                    is KayitFormu.Bakim -> Kayit.Bakim(
                        tarihMillis = kontrol.tarihMillis,
                        tutar = kontrol.tutar!!,
                        not = kontrol.not.trim(),
                        bakimTuru = kontrol.bakimTuru.trim(),
                        hatirlatmaMillis = kontrol.hatirlatmaMillis
                    )

                    is KayitFormu.Aksesuar -> Kayit.Aksesuar(
                        tarihMillis = kontrol.tarihMillis,
                        tutar = kontrol.tutar!!,
                        not = kontrol.not.trim(),
                        aksesuarAdi = kontrol.aksesuarAdi.trim()
                    )
                }
                ekleViewModel.kaydet(kayit)
            } else {
                ekleViewModel.guncelle(uiState.copy(form = kontrol))
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun KayitEkleIcerik(
    form: KayitFormu,
    sonOkuma: Int?,
    bildirimIzniVar: Boolean,
    onHatirlatmaAcilsin: () -> Unit,
    onHatirlatmaIzniIste: () -> Unit = {},
    // null = serit yok.
    seritOzeti: String? = null,
    onSeritTikla: () -> Unit = {},
    kaydediliyor: Boolean,
    hata: String?,
    onFormDegis: (KayitFormu) -> Unit,
    onKaydetTikla: () -> Unit,
    onGeriTikla: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(stringResource(R.string.kayit_ekle)) },
                navigationIcon = {
                    IconButton(onClick = onGeriTikla) {
                        Icon(MotorumIkonlari.Geri, contentDescription = stringResource(R.string.geri))
                    }
                }
            )
        }
    ) { innerPadding ->
        // Kaydet dugmesi tasarimda sayfanin dibinde. Form kisa olunca alta
        // yapissin, uzun olunca (road trip, hatirlatma acik) kayarak gelsin
        // diye sutun en az ekran boyu kadar uzun tutuluyor.
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
        ) {
            val ekranBoyu = maxHeight
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = ekranBoyu)
                    .padding(AppSpacing.normal),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                KategoriDropdown(
                    secili = form.kategori,
                    // Kategori degisince o kategorinin bos formu kurulur; onceki alanlar sifirlanir.
                    onSecim = { onFormDegis(bosForm(it)) },
                    modifier = Modifier
                        .fillMaxWidth()
                        .yukselerekGir(0)
                )

                if (seritOzeti != null) {
                    SonKayitSeridi(
                        ozet = seritOzeti,
                        onTikla = onSeritTikla,
                        modifier = Modifier.yukselerekGir(1)
                    )
                }

                // Alan blogu kategoriye gore secilir. Yeni kategori eklendiginde
                // derleyici bu when'in eksik oldugunu gosterir. Disaridaki Column
                // kategori degisse de ayni kaliyor: giris animasyonu tekrar oynamiyor.
                Column(modifier = Modifier.yukselerekGir(2)) {
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
                            sonOkuma = sonOkuma,
                            modifier = Modifier.fillMaxWidth()
                        )

                        is KayitFormu.Bakim -> BakimAlanlari(
                            form = form,
                            onDegis = onFormDegis,
                            bildirimIzniVar = bildirimIzniVar,
                            onHatirlatmaAcilsin = onHatirlatmaAcilsin,
                            onHatirlatmaIzniIste = onHatirlatmaIzniIste,
                            modifier = Modifier.fillMaxWidth()
                        )

                        is KayitFormu.Aksesuar -> AksesuarAlanlari(
                            form = form,
                            onDegis = onFormDegis,
                            modifier = Modifier.fillMaxWidth()
                        )
                    }
                }

                OutlinedTextField(
                    value = form.not,
                    onValueChange = { onFormDegis(form.notDegistir(it)) },
                    label = { Text(stringResource(gorunum(form.kategori).notEtiketi)) },
                    // Not serbest metin: klavye cumle duzeninde acilsin ve
                    // Enter satir atlasin (varsayilanda "bitti" tusu cikiyordu).
                    keyboardOptions = KeyboardOptions(
                        capitalization = KeyboardCapitalization.Sentences,
                        keyboardType = KeyboardType.Text
                    ),
                    // Tasarimda not alani iki satir yuksekliginde.
                    minLines = 2,
                    modifier = Modifier
                        .fillMaxWidth()
                        .yukselerekGir(3)
                )

                if (hata != null) {
                    Text(
                        text = hata,
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.error
                    )
                }

                // Kalan bosluk dugmeyi dibe itiyor.
                Spacer(Modifier.weight(1f))

                // Tasarimdaki hap: murekkep dolgulu, tam genislik, hafif golgeli.
                // Basilinca kuculup koyulasiyor (MurekkepDugme).
                MurekkepDugme(
                    onClick = onKaydetTikla,
                    enabled = !kaydediliyor,
                    shape = AppShape.cip,
                    elevation = ButtonDefaults.buttonElevation(defaultElevation = 4.dp, pressedElevation = 1.dp),
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(52.dp)
                        .yukselerekGir(4)
                ) {
                    if (kaydediliyor) {
                        CircularProgressIndicator(
                            modifier = Modifier.size(20.dp),
                            strokeWidth = 2.dp,
                            color = MurekkepUstu
                        )
                    } else {
                        Text(stringResource(R.string.kaydet), fontSize = 14.5.sp, fontWeight = FontWeight.Bold)
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun KayitEkleIcerikPreview() {
    MotorumTheme {
        KayitEkleIcerik(
            form = KayitFormu.Yakit(),
            sonOkuma = null,
            bildirimIzniVar = true,
            onHatirlatmaAcilsin = {},
            seritOzeti = "Shell V-Power · 12,40 L · 580,00 ₺",
            kaydediliyor = false,
            hata = null,
            onFormDegis = {},
            onKaydetTikla = {},
            onGeriTikla = {}
        )
    }
}

package com.oguzhanp.motorum.feature.ayarlar

import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oguzhanp.motorum.R
import com.oguzhanp.motorum.core.izin.bildirimIzniVerildiMi
import com.oguzhanp.motorum.core.izin.konumIzniVerildiMi
import com.oguzhanp.motorum.core.izin.rememberBildirimIzni
import com.oguzhanp.motorum.core.izin.rememberKonumIzni
import com.oguzhanp.motorum.core.navigation.AnaBolgeKabugu
import com.oguzhanp.motorum.core.navigation.Routes
import com.oguzhanp.motorum.core.tasarim.AppElevation
import com.oguzhanp.motorum.core.tasarim.AppMotion
import com.oguzhanp.motorum.core.tasarim.AppShape
import com.oguzhanp.motorum.core.tasarim.AppSpacing
import com.oguzhanp.motorum.core.tasarim.BakimRenk
import com.oguzhanp.motorum.core.tasarim.BakimZemin
import com.oguzhanp.motorum.core.tasarim.CizgiSolgun
import com.oguzhanp.motorum.core.tasarim.DurumYesilMetin
import com.oguzhanp.motorum.core.tasarim.DurumYesilZemin
import com.oguzhanp.motorum.core.tasarim.HataKirmizi
import com.oguzhanp.motorum.core.tasarim.HataMetin
import com.oguzhanp.motorum.core.tasarim.HataSolgun
import com.oguzhanp.motorum.core.tasarim.HataZemin
import com.oguzhanp.motorum.core.tasarim.Inter
import com.oguzhanp.motorum.core.tasarim.KartZemin
import com.oguzhanp.motorum.core.tasarim.Kenar
import com.oguzhanp.motorum.core.tasarim.MetinAna
import com.oguzhanp.motorum.core.tasarim.MetinEtiket
import com.oguzhanp.motorum.core.tasarim.MetinIkincil
import com.oguzhanp.motorum.core.tasarim.MetinSolgun
import com.oguzhanp.motorum.core.tasarim.MotorumIkonlari
import com.oguzhanp.motorum.core.tasarim.MotorumTheme
import com.oguzhanp.motorum.core.tasarim.Murekkep
import com.oguzhanp.motorum.core.tasarim.RoadTripRenk
import com.oguzhanp.motorum.core.tasarim.RoadTripZemin
import com.oguzhanp.motorum.core.util.DilAyari
import com.oguzhanp.motorum.core.util.bildirimAyarlariniAc
import com.oguzhanp.motorum.core.util.dakikaAl
import com.oguzhanp.motorum.core.util.formatSaat
import com.oguzhanp.motorum.core.util.formatTarih
import com.oguzhanp.motorum.core.util.saatAl
import com.oguzhanp.motorum.core.util.uygulamaAyarlariniAc
import com.oguzhanp.motorum.model.DilSecimi
import com.oguzhanp.motorum.model.TemaSecimi

// Izin satirinin ekranda gosterdigi hali. Uc durum var cunku "verilmedi"nin
// ikisi farkli davraniyor: hic sorulmadiysa dokununca izin isteniyor,
// reddedildiyse sistem bir daha sormuyor ve telefon ayarlarina gidiyoruz.
enum class IzinHali { VERILDI, SORULMADI, REDDEDILDI }

@Composable
fun AyarlarSayfasi(
    onSekmeTikla: (String) -> Unit,
    onCikisYapildi: () -> Unit,
    onBelgelerTikla: () -> Unit = {},
    viewModel: AyarlarViewModel = hiltViewModel()
) {
    val durum by viewModel.durum.collectAsStateWithLifecycle()
    val baglam = LocalContext.current

    // Izinler ViewModel'de degil burada: kontrolu ve istegi Activity'ye bagli.
    var bildirimVar by remember { mutableStateOf(bildirimIzniVerildiMi(baglam)) }
    var konumVar by remember { mutableStateOf(konumIzniVerildiMi(baglam)) }
    var bildirimReddedildi by rememberSaveable { mutableStateOf(false) }
    var konumReddedildi by rememberSaveable { mutableStateOf(false) }

    // Kullanici telefon ayarlarindan donunce rozet guncel olsun.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        bildirimVar = bildirimIzniVerildiMi(baglam)
        konumVar = konumIzniVerildiMi(baglam)
    }

    val bildirimIste = rememberBildirimIzni { verildi ->
        bildirimVar = verildi
        if (!verildi) bildirimReddedildi = true
    }
    val konumIste = rememberKonumIzni { verildi ->
        konumVar = verildi
        if (!verildi) konumReddedildi = true
    }

    // Sistemin "farkli kaydet" penceresi. Donen adres null ise vazgecildi.
    val kaydetPenceresi = rememberLauncherForActivityResult(
        ActivityResultContracts.CreateDocument("text/csv")
    ) { hedef -> viewModel.dosyaSecildi(hedef) }

    LaunchedEffect(durum.kaydedilecekDosyaAdi) {
        val ad = durum.kaydedilecekDosyaAdi ?: return@LaunchedEffect
        viewModel.kaydetmePenceresiAcildi()
        kaydetPenceresi.launch(ad)
    }

    val snackbarDurumu = remember { SnackbarHostState() }
    // Mesajin metni burada seciliyor: dil secimi ekranin tarafinda.
    val mesajMetni = durum.mesaj?.let { mesajMetni(it) }
    LaunchedEffect(durum.mesaj) {
        val metin = mesajMetni ?: return@LaunchedEffect
        viewModel.mesajGosterildi()
        snackbarDurumu.showSnackbar(metin)
    }

    // Surum Gradle'daki versionName'den: elle yazilsaydi guncellemede unutulurdu.
    val surum = remember {
        runCatching {
            baglam.packageManager.getPackageInfo(baglam.packageName, 0).versionName
        }.getOrNull().orEmpty()
    }

    // Dil secimi DataStore'da degil, Android'in kendisinde duruyor; ekran
    // kuruldugunda oradan okunuyor. Secim degisince Android ekrani zaten
    // yeniden kuruyor ve deger tekrar okunuyor.
    var dil by remember { mutableStateOf(DilAyari.oku()) }

    AyarlarIcerik(
        durum = durum,
        dil = dil,
        bildirimHali = izinHali(bildirimVar, bildirimReddedildi),
        konumHali = izinHali(konumVar, konumReddedildi),
        surum = surum,
        snackbarDurumu = snackbarDurumu,
        onSekmeTikla = onSekmeTikla,
        // Izin verildiyse de ayarlara gidiyoruz: kapatmak isteyen oradan kapatir.
        onBildirimTikla = {
            if (bildirimVar || bildirimReddedildi) bildirimAyarlariniAc(baglam) else bildirimIste()
        },
        onKonumTikla = {
            if (konumVar || konumReddedildi) uygulamaAyarlariniAc(baglam) else konumIste()
        },
        onTemaSec = viewModel::temaSec,
        onDilSec = { secim ->
            dil = secim
            DilAyari.yaz(secim)
        },
        onOnbellekTemizle = viewModel::havaOnbelleginiTemizle,
        onDisaAktar = viewModel::disaAktarimaBasla,
        onBelgelerTikla = onBelgelerTikla,
        onCikisTikla = {
            viewModel.cikisYap()
            onCikisYapildi()
        }
    )
}

private fun izinHali(verildi: Boolean, reddedildi: Boolean): IzinHali = when {
    verildi -> IzinHali.VERILDI
    reddedildi -> IzinHali.REDDEDILDI
    else -> IzinHali.SORULMADI
}

@Composable
fun AyarlarIcerik(
    durum: AyarlarUiState,
    dil: DilSecimi = DilSecimi.SISTEM,
    bildirimHali: IzinHali,
    konumHali: IzinHali,
    surum: String,
    snackbarDurumu: SnackbarHostState,
    onSekmeTikla: (String) -> Unit,
    onBildirimTikla: () -> Unit,
    onKonumTikla: () -> Unit,
    onTemaSec: (TemaSecimi) -> Unit,
    onDilSec: (DilSecimi) -> Unit = {},
    onOnbellekTemizle: () -> Unit,
    onDisaAktar: () -> Unit,
    onCikisTikla: () -> Unit,
    onBelgelerTikla: () -> Unit = {}
) {
    // Onay diyalogu ekranin yerel durumu: ViewModel'e tasimaya deger bir bilgi
    // degil, ekran kapaninca kaybolmasi zaten dogru.
    var onayGoster by rememberSaveable { mutableStateOf(false) }

    AnaBolgeKabugu(
        baslik = stringResource(R.string.sekme_ayarlar),
        seciliRota = Routes.AYARLAR,
        onSekmeTikla = onSekmeTikla,
        snackbarAlani = { SnackbarHost(snackbarDurumu) }
    ) { icPadding ->
        // Surum yazisi tasarimda sayfanin dibinde. Icerik kisa olunca alta
        // yapissin, uzun olunca (kucuk ekran, buyuk yazi) kayarak gelsin diye
        // sutun en az ekran boyu kadar uzun tutuluyor.
        BoxWithConstraints(
            modifier = Modifier
                .fillMaxSize()
                .padding(icPadding)
        ) {
            val ekranBoyu = maxHeight
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .verticalScroll(rememberScrollState())
                    .heightIn(min = ekranBoyu)
                    .padding(start = AppSpacing.normal, end = AppSpacing.normal, top = 6.dp),
                verticalArrangement = Arrangement.spacedBy(18.dp)
            ) {
                Bolum(stringResource(R.string.bolum_bildirimler)) {
                    IzinSatiri(
                        ikon = MotorumIkonlari.Bildirim,
                        ikonRengi = BakimRenk,
                        ikonZemini = BakimZemin,
                        baslik = stringResource(R.string.bakim_hatirlatmalari),
                        aciklama = stringResource(R.string.bildirim_izni_gerekiyor),
                        hal = bildirimHali,
                        onTikla = onBildirimTikla
                    )
                    Ayirici()
                    IzinSatiri(
                        ikon = MotorumIkonlari.Konum,
                        ikonRengi = Murekkep,
                        ikonZemini = CizgiSolgun,
                        baslik = stringResource(R.string.konum_izni),
                        aciklama = stringResource(R.string.konum_izni_gerekiyor),
                        hal = konumHali,
                        onTikla = onKonumTikla
                    )
                }

                Bolum(stringResource(R.string.bolum_gorunum)) {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                        verticalArrangement = Arrangement.spacedBy(11.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IkonKutusu(MotorumIkonlari.Ay, Murekkep, CizgiSolgun)
                            Text(stringResource(R.string.tema), style = BASLIK_STILI, color = MetinAna)
                        }
                        TemaSecici(secili = durum.tema, onSec = onTemaSec)
                    }
                    Ayirici()
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                        verticalArrangement = Arrangement.spacedBy(11.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IkonKutusu(MotorumIkonlari.Dunya, Murekkep, CizgiSolgun)
                            Text(stringResource(R.string.dil), style = BASLIK_STILI, color = MetinAna)
                        }
                        DilSecici(secili = dil, onSec = onDilSec)
                    }
                }

                // Belgeler asil olarak Istatistikler'de duruyor; buradan da
                // ulasilabilsin, cunku kullanici "sigorta tarihim nerede" diye
                // once ayarlara bakiyor.
                Bolum(stringResource(R.string.bolum_motor)) {
                    Satir(
                        ikon = MotorumIkonlari.Klasor,
                        ikonRengi = Murekkep,
                        ikonZemini = CizgiSolgun,
                        baslik = stringResource(R.string.belgeler),
                        aciklama = stringResource(R.string.belgeler_aciklama),
                        onTikla = onBelgelerTikla
                    ) {
                        Cevron(Kenar)
                    }
                }

                Bolum(stringResource(R.string.bolum_veriler)) {
                    Satir(
                        ikon = MotorumIkonlari.Sil,
                        ikonRengi = RoadTripRenk,
                        ikonZemini = RoadTripZemin,
                        baslik = stringResource(R.string.hava_onbellegini_temizle),
                        aciklama = durum.havaSonGuncelleme
                            ?.let { stringResource(R.string.son_guncelleme, sonGuncellemeYazisi(it)) }
                            ?: stringResource(R.string.onbellek_bos),
                        // Bos onbellegi temizlemenin anlami yok.
                        onTikla = if (durum.havaSonGuncelleme != null) onOnbellekTemizle else null
                    )
                    Ayirici()
                    Satir(
                        ikon = MotorumIkonlari.DisaAktar,
                        ikonRengi = Murekkep,
                        ikonZemini = CizgiSolgun,
                        baslik = stringResource(R.string.kayitlari_disa_aktar),
                        aciklama = stringResource(R.string.disa_aktar_aciklama),
                        onTikla = if (durum.disaAktarimHazirlaniyor) null else onDisaAktar
                    ) {
                        if (durum.disaAktarimHazirlaniyor) {
                            CircularProgressIndicator(
                                modifier = Modifier.size(16.dp),
                                strokeWidth = 2.dp,
                                color = MetinIkincil
                            )
                        } else {
                            Cevron(Kenar)
                        }
                    }
                }

                Bolum(stringResource(R.string.bolum_hesap)) {
                    Satir(
                        ikon = MotorumIkonlari.Kisi,
                        ikonRengi = MetinIkincil,
                        ikonZemini = CizgiSolgun,
                        baslik = durum.eposta,
                        baslikStili = EPOSTA_STILI,
                        baslikRengi = MetinEtiket
                    )
                    Ayirici()
                    Satir(
                        ikon = MotorumIkonlari.Cikis,
                        ikonRengi = HataKirmizi,
                        ikonZemini = HataZemin,
                        baslik = stringResource(R.string.cikis_yap),
                        baslikStili = BASLIK_STILI.copy(fontWeight = FontWeight.Bold),
                        baslikRengi = HataKirmizi,
                        onTikla = { onayGoster = true }
                    ) {
                        Cevron(HataSolgun)
                    }
                }

                // Kalan bosluk surumu dibe itiyor.
                Spacer(Modifier.weight(1f))
                Text(
                    // Uygulama adi cevrilmiyor ama tek yerden geliyor.
                    text = "${stringResource(R.string.app_name)} $surum".trim(),
                    style = MaterialTheme.typography.bodySmall.copy(
                        fontSize = 11.sp,
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = Kenar,
                    textAlign = TextAlign.Center,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(bottom = 14.dp)
                )
            }
        }
    }

    if (onayGoster) {
        AlertDialog(
            onDismissRequest = { onayGoster = false },
            title = { Text(stringResource(R.string.cikis_yap)) },
            text = { Text(stringResource(R.string.cikis_onayi)) },
            confirmButton = {
                TextButton(onClick = {
                    onayGoster = false
                    onCikisTikla()
                }) {
                    Text(stringResource(R.string.cikis_yap), color = MaterialTheme.colorScheme.error)
                }
            },
            dismissButton = {
                TextButton(onClick = { onayGoster = false }) { Text(stringResource(R.string.iptal)) }
            }
        )
    }
}

// ------------------------------------------------------------ PARCALAR

private val BASLIK_STILI = TextStyle(fontFamily = Inter, fontSize = 14.sp, fontWeight = FontWeight.SemiBold, lineHeight = 18.sp)
private val ACIKLAMA_STILI = TextStyle(fontFamily = Inter, fontSize = 11.sp, fontWeight = FontWeight.Medium, lineHeight = 14.sp)
private val EPOSTA_STILI = TextStyle(fontFamily = Inter, fontSize = 13.sp, fontWeight = FontWeight.Medium, lineHeight = 18.sp)

// Kucuk buyuk harfli etiket + altinda tek kart. Ayarlarin her grubu bu kalip.
@Composable
private fun Bolum(etiket: String, icerik: @Composable ColumnScope.() -> Unit) {
    Column(verticalArrangement = Arrangement.spacedBy(AppSpacing.kucuk)) {
        Text(
            text = etiket,
            style = MaterialTheme.typography.labelSmall.copy(
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold,
                letterSpacing = 0.8.sp
            ),
            color = MetinSolgun,
            modifier = Modifier.padding(start = AppSpacing.cokKucuk)
        )
        Card(
            shape = AppShape.kart,
            colors = CardDefaults.cardColors(containerColor = KartZemin),
            elevation = CardDefaults.cardElevation(defaultElevation = AppElevation.kart),
            modifier = Modifier.fillMaxWidth()
        ) {
            icerik()
        }
    }
}

// Ikonun oturdugu yumusak koseli kare. Renk satirin konusunu soyluyor:
// kehribar bakim, turkuaz veri, kirmizi cikis; notr olanlar gri.
@Composable
private fun IkonKutusu(ikon: ImageVector, renk: Color, zemin: Color) {
    Box(
        modifier = Modifier
            .size(34.dp)
            .clip(RoundedCornerShape(11.dp))
            .background(zemin),
        contentAlignment = Alignment.Center
    ) {
        Icon(ikon, contentDescription = null, tint = renk, modifier = Modifier.size(17.dp))
    }
}

// Ayarlar satirinin iskeleti: ikon, baslik (+ aciklama), sagda istege bagli ek.
// onTikla null ise satir dokunulmaz ve dalga efekti cikmaz.
@Composable
private fun Satir(
    ikon: ImageVector,
    ikonRengi: Color,
    ikonZemini: Color,
    baslik: String,
    modifier: Modifier = Modifier,
    aciklama: String? = null,
    baslikStili: TextStyle = BASLIK_STILI,
    baslikRengi: Color = MetinAna,
    onTikla: (() -> Unit)? = null,
    sag: @Composable () -> Unit = {}
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .then(if (onTikla != null) Modifier.clickable(onClick = onTikla) else Modifier)
            .padding(horizontal = 14.dp, vertical = 13.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        IkonKutusu(ikon, ikonRengi, ikonZemini)
        Column(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(2.dp)
        ) {
            Text(
                text = baslik,
                style = baslikStili,
                color = baslikRengi,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
            if (aciklama != null) {
                Text(text = aciklama, style = ACIKLAMA_STILI, color = MetinSolgun)
            }
        }
        sag()
    }
}

@Composable
private fun IzinSatiri(
    ikon: ImageVector,
    ikonRengi: Color,
    ikonZemini: Color,
    baslik: String,
    aciklama: String,
    hal: IzinHali,
    onTikla: () -> Unit
) {
    Satir(
        ikon = ikon,
        ikonRengi = ikonRengi,
        ikonZemini = ikonZemini,
        baslik = baslik,
        // Reddedildiyse neden ayarlara gittigimizi onceden soyluyoruz.
        aciklama = if (hal == IzinHali.REDDEDILDI) {
            stringResource(R.string.izin_telefon_ayarlarindan)
        } else {
            aciklama
        },
        onTikla = onTikla
    ) {
        if (hal == IzinHali.VERILDI) {
            Rozet(stringResource(R.string.izin_verildi), DurumYesilMetin, DurumYesilZemin)
        } else {
            Rozet(stringResource(R.string.izin_verilmedi), HataMetin, HataZemin)
        }
        Cevron(Kenar)
    }
}

@Composable
private fun Rozet(metin: String, renk: Color, zemin: Color) {
    Text(
        text = metin,
        style = TextStyle(fontFamily = Inter, fontSize = 10.sp, fontWeight = FontWeight.Bold, lineHeight = 13.sp),
        color = renk,
        modifier = Modifier
            .clip(AppShape.cip)
            .background(zemin)
            .padding(horizontal = AppSpacing.kucuk, vertical = 3.dp)
    )
}

@Composable
private fun Cevron(renk: Color) {
    Icon(MotorumIkonlari.Cevron, contentDescription = null, tint = renk, modifier = Modifier.size(16.dp))
}

// Satirlar arasi ince cizgi. Soldan ikon kutusunun genisligi kadar iceride
// basliyor: cizgi ikonun altindan degil metnin altindan geciyor.
@Composable
private fun Ayirici() {
    Box(
        modifier = Modifier
            .padding(start = 60.dp)
            .fillMaxWidth()
            .height(1.dp)
            .background(CizgiSolgun)
    )
}

// Uc parcali segment. Secili parca kart renginde ve hafif golgeli: seridin
// icinden "kalkmis" bir dugme gibi duruyor. Renk gecisi uygulamanin egrisiyle.
@Composable
private fun TemaSecici(secili: TemaSecimi, onSec: (TemaSecimi) -> Unit) {
    SecimSeridi(
        secenekler = TemaSecimi.entries,
        secili = secili,
        etiket = { stringResource(it.etiket) },
        onSec = onSec
    )
}

// Dil adlari cevrilmiyor: her dil listede kendi adiyla duruyor. Sadece
// "Sistem" cevriliyor, cunku o bir dil adi degil.
@Composable
private fun DilSecici(secili: DilSecimi, onSec: (DilSecimi) -> Unit) {
    SecimSeridi(
        secenekler = DilSecimi.entries,
        secili = secili,
        etiket = { secim ->
            when (secim) {
                DilSecimi.SISTEM -> stringResource(R.string.secim_sistem)
                DilSecimi.TURKCE -> "Türkçe"
                DilSecimi.INGILIZCE -> "English"
            }
        },
        onSec = onSec
    )
}

@Composable
private fun <T> SecimSeridi(
    secenekler: List<T>,
    secili: T,
    etiket: @Composable (T) -> String,
    onSec: (T) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CizgiSolgun)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.cokKucuk)
    ) {
        secenekler.forEach { secim ->
            val seciliMi = secim == secili
            val zemin by animateColorAsState(
                targetValue = if (seciliMi) KartZemin else CizgiSolgun,
                animationSpec = tween(AppMotion.PANEL, easing = AppMotion.egri),
                label = "segmentZemini"
            )
            Text(
                text = etiket(secim),
                style = TextStyle(
                    fontFamily = Inter,
                    fontSize = 12.sp,
                    fontWeight = if (seciliMi) FontWeight.Bold else FontWeight.SemiBold,
                    lineHeight = 16.sp
                ),
                color = if (seciliMi) MetinAna else MetinIkincil,
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .weight(1f)
                    .then(
                        if (seciliMi) Modifier.shadow(1.dp, RoundedCornerShape(8.dp)) else Modifier
                    )
                    .clip(RoundedCornerShape(8.dp))
                    .background(zemin)
                    .clickable { onSec(secim) }
                    .padding(vertical = 7.dp)
            )
        }
    }
}

// ViewModel hangi mesaj oldugunu soyluyor, metni burada seciyoruz.
@Composable
private fun mesajMetni(mesaj: AyarlarMesaji): String = when (mesaj) {
    AyarlarMesaji.OnbellekTemizlendi -> stringResource(R.string.onbellek_temizlendi)
    AyarlarMesaji.MotorYok -> stringResource(R.string.once_motor_ekle)
    AyarlarMesaji.KayitYok -> stringResource(R.string.disa_aktarilacak_kayit_yok)
    is AyarlarMesaji.Aktarildi -> stringResource(R.string.kayit_disa_aktarildi, mesaj.adet)
    is AyarlarMesaji.Hata -> mesaj.metin
}

// Bugun alindiysa sadece saat ("14:30"), degilse tarihiyle birlikte.
private fun sonGuncellemeYazisi(millis: Long): String {
    val saat = formatSaat(saatAl(millis), dakikaAl(millis))
    val bugun = formatTarih(System.currentTimeMillis())
    val gun = formatTarih(millis)
    return if (gun == bugun) saat else "$gun $saat"
}

@Preview(showBackground = true, heightDp = 844)
@Composable
private fun AyarlarIcerikPreview() {
    MotorumTheme {
        AyarlarIcerik(
            durum = AyarlarUiState(
                tema = TemaSecimi.ACIK,
                eposta = "ornek@motorum.app",
                havaSonGuncelleme = System.currentTimeMillis()
            ),
            bildirimHali = IzinHali.VERILDI,
            konumHali = IzinHali.SORULMADI,
            surum = "1.0",
            snackbarDurumu = remember { SnackbarHostState() },
            onSekmeTikla = {},
            onBildirimTikla = {},
            onKonumTikla = {},
            onTemaSec = {},
            onOnbellekTemizle = {},
            onDisaAktar = {},
            onCikisTikla = {}
        )
    }
}

@Preview(showBackground = true, heightDp = 844)
@Composable
private fun AyarlarIcerikKaranlikPreview() {
    MotorumTheme(karanlik = true) {
        AyarlarIcerik(
            durum = AyarlarUiState(tema = TemaSecimi.KARANLIK, eposta = "ornek@motorum.app"),
            bildirimHali = IzinHali.VERILDI,
            konumHali = IzinHali.REDDEDILDI,
            surum = "1.0",
            snackbarDurumu = remember { SnackbarHostState() },
            onSekmeTikla = {},
            onBildirimTikla = {},
            onKonumTikla = {},
            onTemaSec = {},
            onOnbellekTemizle = {},
            onDisaAktar = {},
            onCikisTikla = {}
        )
    }
}

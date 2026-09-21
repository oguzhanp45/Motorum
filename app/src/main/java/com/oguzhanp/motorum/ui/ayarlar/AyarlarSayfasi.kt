package com.oguzhanp.motorum.ui.ayarlar

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
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
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
import com.oguzhanp.motorum.core.constants.AppElevation
import com.oguzhanp.motorum.core.constants.AppMotion
import com.oguzhanp.motorum.core.constants.AppShape
import com.oguzhanp.motorum.core.constants.AppSpacing
import com.oguzhanp.motorum.model.TemaSecimi
import com.oguzhanp.motorum.ui.components.MotorumIkonlari
import com.oguzhanp.motorum.ui.components.bildirimIzniVerildiMi
import com.oguzhanp.motorum.ui.components.konumIzniVerildiMi
import com.oguzhanp.motorum.ui.components.rememberBildirimIzni
import com.oguzhanp.motorum.ui.components.rememberKonumIzni
import com.oguzhanp.motorum.ui.navigation.AnaBolgeKabugu
import com.oguzhanp.motorum.ui.navigation.Routes
import com.oguzhanp.motorum.ui.theme.BakimRenk
import com.oguzhanp.motorum.ui.theme.BakimZemin
import com.oguzhanp.motorum.ui.theme.CizgiSolgun
import com.oguzhanp.motorum.ui.theme.DurumYesilMetin
import com.oguzhanp.motorum.ui.theme.DurumYesilZemin
import com.oguzhanp.motorum.ui.theme.HataKirmizi
import com.oguzhanp.motorum.ui.theme.Inter
import com.oguzhanp.motorum.ui.theme.HataMetin
import com.oguzhanp.motorum.ui.theme.HataSolgun
import com.oguzhanp.motorum.ui.theme.HataZemin
import com.oguzhanp.motorum.ui.theme.KartZemin
import com.oguzhanp.motorum.ui.theme.Kenar
import com.oguzhanp.motorum.ui.theme.MetinAna
import com.oguzhanp.motorum.ui.theme.MetinEtiket
import com.oguzhanp.motorum.ui.theme.MetinIkincil
import com.oguzhanp.motorum.ui.theme.MetinSolgun
import com.oguzhanp.motorum.ui.theme.MotorumTheme
import com.oguzhanp.motorum.ui.theme.Murekkep
import com.oguzhanp.motorum.ui.theme.RoadTripRenk
import com.oguzhanp.motorum.ui.theme.RoadTripZemin
import com.oguzhanp.motorum.util.bildirimAyarlariniAc
import com.oguzhanp.motorum.util.dakikaAl
import com.oguzhanp.motorum.util.formatSaat
import com.oguzhanp.motorum.util.formatTarih
import com.oguzhanp.motorum.util.saatAl
import com.oguzhanp.motorum.util.uygulamaAyarlariniAc

// Izin satirinin ekranda gosterdigi hali. Uc durum var cunku "verilmedi"nin
// ikisi farkli davraniyor: hic sorulmadiysa dokununca izin isteniyor,
// reddedildiyse sistem bir daha sormuyor ve telefon ayarlarina gidiyoruz.
enum class IzinHali { VERILDI, SORULMADI, REDDEDILDI }

@Composable
fun AyarlarSayfasi(
    onSekmeTikla: (String) -> Unit,
    onCikisYapildi: () -> Unit,
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
    LaunchedEffect(durum.mesaj) {
        val mesaj = durum.mesaj ?: return@LaunchedEffect
        viewModel.mesajGosterildi()
        snackbarDurumu.showSnackbar(mesaj)
    }

    // Surum Gradle'daki versionName'den: elle yazilsaydi guncellemede unutulurdu.
    val surum = remember {
        runCatching {
            baglam.packageManager.getPackageInfo(baglam.packageName, 0).versionName
        }.getOrNull().orEmpty()
    }

    AyarlarIcerik(
        durum = durum,
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
        onOnbellekTemizle = viewModel::havaOnbelleginiTemizle,
        onDisaAktar = viewModel::disaAktarimaBasla,
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
    bildirimHali: IzinHali,
    konumHali: IzinHali,
    surum: String,
    snackbarDurumu: SnackbarHostState,
    onSekmeTikla: (String) -> Unit,
    onBildirimTikla: () -> Unit,
    onKonumTikla: () -> Unit,
    onTemaSec: (TemaSecimi) -> Unit,
    onOnbellekTemizle: () -> Unit,
    onDisaAktar: () -> Unit,
    onCikisTikla: () -> Unit
) {
    // Onay diyalogu ekranin yerel durumu: ViewModel'e tasimaya deger bir bilgi
    // degil, ekran kapaninca kaybolmasi zaten dogru.
    var onayGoster by rememberSaveable { mutableStateOf(false) }

    AnaBolgeKabugu(
        baslik = "Ayarlar",
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
                Bolum("BİLDİRİMLER") {
                    IzinSatiri(
                        ikon = MotorumIkonlari.Bildirim,
                        ikonRengi = BakimRenk,
                        ikonZemini = BakimZemin,
                        baslik = "Bakım hatırlatmaları",
                        aciklama = "Bildirim izni gerekiyor",
                        hal = bildirimHali,
                        onTikla = onBildirimTikla
                    )
                    Ayirici()
                    IzinSatiri(
                        ikon = MotorumIkonlari.Konum,
                        ikonRengi = Murekkep,
                        ikonZemini = CizgiSolgun,
                        baslik = "Konum izni",
                        aciklama = "Hava durumu için gerekiyor",
                        hal = konumHali,
                        onTikla = onKonumTikla
                    )
                }

                Bolum("GÖRÜNÜM") {
                    Column(
                        modifier = Modifier.padding(horizontal = 14.dp, vertical = 13.dp),
                        verticalArrangement = Arrangement.spacedBy(11.dp)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            horizontalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            IkonKutusu(MotorumIkonlari.Ay, Murekkep, CizgiSolgun)
                            Text("Tema", style = BASLIK_STILI, color = MetinAna)
                        }
                        TemaSecici(secili = durum.tema, onSec = onTemaSec)
                    }
                    Ayirici()
                    // Tasarimdaki gibi soluk ve dokunulmaz: yerini simdiden
                    // gosteriyor, ceviri gelince acilacak.
                    Satir(
                        ikon = MotorumIkonlari.Dunya,
                        ikonRengi = MetinIkincil,
                        ikonZemini = CizgiSolgun,
                        baslik = "Dil",
                        modifier = Modifier.alpha(0.5f)
                    ) {
                        Rozet("Yakında", MetinIkincil, CizgiSolgun)
                    }
                }

                Bolum("VERİLER") {
                    Satir(
                        ikon = MotorumIkonlari.Sil,
                        ikonRengi = RoadTripRenk,
                        ikonZemini = RoadTripZemin,
                        baslik = "Hava durumu önbelleğini temizle",
                        aciklama = durum.havaSonGuncelleme
                            ?.let { "Son güncelleme: ${sonGuncellemeYazisi(it)}" }
                            ?: "Önbellek boş",
                        // Bos onbellegi temizlemenin anlami yok.
                        onTikla = if (durum.havaSonGuncelleme != null) onOnbellekTemizle else null
                    )
                    Ayirici()
                    Satir(
                        ikon = MotorumIkonlari.DisaAktar,
                        ikonRengi = Murekkep,
                        ikonZemini = CizgiSolgun,
                        baslik = "Kayıtları dışa aktar",
                        aciklama = "Seçili motorun kayıtları, CSV dosyası",
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

                Bolum("HESAP") {
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
                        baslik = "Çıkış yap",
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
                    text = "Motorum $surum".trim(),
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
        aciklama = if (hal == IzinHali.REDDEDILDI) "Telefon ayarlarından açabilirsin" else aciklama,
        onTikla = onTikla
    ) {
        if (hal == IzinHali.VERILDI) {
            Rozet("İzin verildi", DurumYesilMetin, DurumYesilZemin)
        } else {
            Rozet("Verilmedi", HataMetin, HataZemin)
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
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(CizgiSolgun)
            .padding(3.dp),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.cokKucuk)
    ) {
        TemaSecimi.entries.forEach { secim ->
            val seciliMi = secim == secili
            val zemin by animateColorAsState(
                targetValue = if (seciliMi) KartZemin else CizgiSolgun,
                animationSpec = tween(AppMotion.PANEL, easing = AppMotion.egri),
                label = "temaZemini"
            )
            Text(
                text = secim.etiket,
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

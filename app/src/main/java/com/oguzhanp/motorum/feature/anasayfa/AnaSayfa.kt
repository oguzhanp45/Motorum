package com.oguzhanp.motorum.feature.anasayfa

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.expandVertically
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarDuration
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.SnackbarResult
import androidx.compose.material3.Text
import androidx.compose.material3.pulltorefresh.PullToRefreshBox
import androidx.compose.material3.pulltorefresh.rememberPullToRefreshState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.derivedStateOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.graphicsLayer
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.compose.LifecycleEventEffect
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import com.oguzhanp.motorum.R
import com.oguzhanp.motorum.core.izin.konumIzniVerildiMi
import com.oguzhanp.motorum.core.izin.rememberKonumIzni
import com.oguzhanp.motorum.core.navigation.AnaBolgeKabugu
import com.oguzhanp.motorum.core.navigation.Routes
import com.oguzhanp.motorum.core.tasarim.AppMotion
import com.oguzhanp.motorum.core.tasarim.AppSpacing
import com.oguzhanp.motorum.core.tasarim.BosDurum
import com.oguzhanp.motorum.core.tasarim.BosGorsel
import com.oguzhanp.motorum.core.tasarim.DUGME_BASILI
import com.oguzhanp.motorum.core.tasarim.MetinIkincil
import com.oguzhanp.motorum.core.tasarim.MotorcuCekmeGostergesi
import com.oguzhanp.motorum.core.tasarim.MotorcuYukleniyor
import com.oguzhanp.motorum.core.tasarim.MotorumIkonlari
import com.oguzhanp.motorum.core.tasarim.MotorumTheme
import com.oguzhanp.motorum.core.tasarim.basiliMurekkep
import com.oguzhanp.motorum.core.tasarim.basilincaKucul
import com.oguzhanp.motorum.data.ortak.INTERNET_YOK
import com.oguzhanp.motorum.feature.hava.HavaDurumuHali
import com.oguzhanp.motorum.feature.hava.HavaDurumuKarti
import com.oguzhanp.motorum.feature.hava.HavaDurumuViewModel
import com.oguzhanp.motorum.feature.motorlarim.MotorCipi
import com.oguzhanp.motorum.feature.motorlarim.MotorSeciciUiState
import com.oguzhanp.motorum.feature.motorlarim.MotorSeciciViewModel
import com.oguzhanp.motorum.feature.motorlarim.MotorSecimPaneli
import com.oguzhanp.motorum.model.HavaDurumu
import com.oguzhanp.motorum.model.Kategori
import com.oguzhanp.motorum.model.Kayit


@Composable
fun AnaSayfa(
    viewModel: KayitViewModel,
    navController: NavController,
    onSekmeTikla: (String) -> Unit,
    seciciViewModel: MotorSeciciViewModel = hiltViewModel(),
    havaViewModel: HavaDurumuViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsStateWithLifecycle()
    val secici by seciciViewModel.uiState.collectAsStateWithLifecycle()
    val havaHali by havaViewModel.hal.collectAsStateWithLifecycle()
    val snackbarDurumu = remember { SnackbarHostState() }

    // Filtre ekranin kendi meselesi, ViewModel'e tasimadik: kayitlarin hepsi
    // zaten elimizde, suzme cizim aninda yapiliyor. rememberSaveable ekran
    // donunce secimi koruyor.
    var seciliKategori by rememberSaveable { mutableStateOf<Kategori?>(null) }
    val baglam = LocalContext.current

    // Izni karttaki buton istiyor, acilista kendiliginden sormuyoruz.
    val konumIzniIste = rememberKonumIzni { verildi ->
        if (verildi) havaViewModel.yukle() else havaViewModel.izinYok(istendiMi = true)
    }

    LaunchedEffect(Unit) {
        viewModel.yukle()
        seciciViewModel.yukle()
        // Izin her acilista yeniden kontrol ediliyor: "Yalnizca bu sefer"
        // izni iki acilis arasinda sona ermis olabilir.
        if (konumIzniVerildiMi(baglam)) havaViewModel.yukle()
        else havaViewModel.izinYok(istendiMi = false)
    }

    // Ayarlardan donunce izni yeniden kontrol ediyoruz: kullanici oraya
    // "Ayarlara git" ile gitti ve izni acmis olabilir. LaunchedEffect(Unit)
    // bunu yakalayamazdi, o sadece ekran ilk kuruldugunda calisiyor.
    LifecycleEventEffect(Lifecycle.Event.ON_RESUME) {
        if (havaHali is HavaDurumuHali.IzinYok && konumIzniVerildiMi(baglam)) {
            havaViewModel.yukle()
        }
    }

    // Panelden motor secilince kayitlari yeniden cekiyoruz. Secim tamamlanmadan
    // cekmemek icin yine bayrak kullaniliyor.
    LaunchedEffect(secici.secimTamam) {
        if (secici.secimTamam) {
            // temizle = true: eldeki liste onceki motorun, ekranda kalmasin.
            viewModel.yukle(temizle = true)
            seciciViewModel.secimTuketildi()
        }
    }

    // Snackbar metinleri onceden okunuyor: LaunchedEffect'in icinde
    // stringResource cagrilamiyor (orasi composable degil).
    val silindiMesaji = stringResource(R.string.kayit_silindi)
    val geriAlEtiketi = stringResource(R.string.geri_al)

    // showSnackbar askiya alinan bir fonksiyon: snackbar kapanana kadar
    // burada bekliyor ve nasil kapandigini donduruyor.
    LaunchedEffect(uiState.geriAlinabilir) {
        if (uiState.geriAlinabilir == null) return@LaunchedEffect
        val sonuc = snackbarDurumu.showSnackbar(
            message = silindiMesaji,
            actionLabel = geriAlEtiketi,
            duration = SnackbarDuration.Short
        )
        if (sonuc == SnackbarResult.ActionPerformed) viewModel.geriAl()
        else viewModel.geriAlmaTuketildi()
    }

    AnaSayfaIcerik(
        uiState = uiState,
        secici = secici,
        seciliKategori = seciliKategori,
        onKategoriSec = { seciliKategori = it },
        havaHali = havaHali,
        snackbarDurumu = snackbarDurumu,
        onSekmeTikla = onSekmeTikla,
        onEkleTikla = { navController.navigate(Routes.KAYIT_EKLE) },
        onKayitTikla = { id -> navController.navigate("kayit_detay/$id") },
        onIstatistikTikla = { navController.navigate(Routes.ISTATISTIK) },
        onKayitKaydirarakSil = { id -> viewModel.sil(id) },
        // Yenileme yollarinin ikisi de ekrandaki her seyi tazeliyor. Cip
        // atlandiginda, internet geri gelse bile "Baglanti yok" yazmaya
        // devam ediyordu.
        onAsagiCek = {
            viewModel.yenile()
            seciciViewModel.yukle()
            // Kullanici bilerek yeniledi: onbellegi atlayip taze hava aliyoruz.
            havaViewModel.yukle(zorla = true)
        },
        onTekrarDeneTikla = {
            viewModel.yukle()
            seciciViewModel.yukle()
        },
        onIzinIste = konumIzniIste,
        onHavaTekrarDene = { havaViewModel.yukle(zorla = true) },
        onCipTikla = seciciViewModel::panelAc,
        onMotorSec = seciciViewModel::motoruSec,
        onMotorEkleTikla = {
            // Once paneli kapat, sonra git: geri donuldugunde panel acik kalmasin.
            seciciViewModel.panelKapat()
            navController.navigate(Routes.motorEkleRotasi(secilsin = true))
        },
        onPanelKapat = seciciViewModel::panelKapat,
        onHatirlatmaTikla = viewModel::paneliAc
    )

    // Hatirlatma paneli. Kayit listede degilse (silinmis, baska motor
    // yuklenirken) acilmiyor; liste gelince kendiliginden aciliyor.
    val panelKaydi = uiState.panelKayitId
        ?.let { id -> uiState.kayitlar.firstOrNull { it.id == id } } as? Kayit.Bakim
    if (panelKaydi != null) {
        HatirlatmaPaneli(
            kayit = panelKaydi,
            onYaptirdim = { viewModel.hatirlatmaYaptirdim(panelKaydi) },
            onErtele = { viewModel.hatirlatmaErtele(panelKaydi) },
            onKapat = { viewModel.hatirlatmayiKapat(panelKaydi) },
            onPaneliKapat = viewModel::paneliKapat
        )
    }

    // Bildirimden gelinip motor degistiyse ust bardaki motor cipi de tazelensin.
    LaunchedEffect(uiState.panelKayitId) {
        if (uiState.panelKayitId != null) seciciViewModel.yukle()
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AnaSayfaIcerik(
    uiState: KayitUiState,
    secici: MotorSeciciUiState,
    seciliKategori: Kategori?,
    onKategoriSec: (Kategori?) -> Unit,
    havaHali: HavaDurumuHali,
    snackbarDurumu: SnackbarHostState,
    onSekmeTikla: (String) -> Unit,
    onEkleTikla: () -> Unit,
    onKayitTikla: (String) -> Unit,
    onIstatistikTikla: () -> Unit,
    onKayitKaydirarakSil: (String) -> Unit,
    onAsagiCek: () -> Unit,
    onTekrarDeneTikla: () -> Unit,
    onIzinIste: () -> Unit,
    onHavaTekrarDene: () -> Unit,
    onCipTikla: () -> Unit,
    onMotorSec: (String) -> Unit,
    onMotorEkleTikla: () -> Unit,
    onPanelKapat: () -> Unit,
    onHatirlatmaTikla: (String) -> Unit = {}
) {
    val cekmeDurumu = rememberPullToRefreshState()

    AnaBolgeKabugu(
        baslik = stringResource(R.string.app_name),
        seciliRota = Routes.ANA_SAYFA,
        onSekmeTikla = onSekmeTikla,
        ustBarAksiyonlari = {
            MotorCipi(durum = secici.cipDurumu, onTikla = onCipTikla)
        },
        snackbarAlani = { SnackbarHost(snackbarDurumu) },
        kayanButon = {
            // FAB varsayilan olarak primaryContainer kullaniyor, primary degil.
            // Tasarimdaki dolu murekkep icin renkleri burada aciktan veriyoruz.
            // Basilinca diger murekkep dugmeler gibi kuculup koyulasiyor.
            val fabEtkilesim = remember { MutableInteractionSource() }
            FloatingActionButton(
                onClick = onEkleTikla,
                interactionSource = fabEtkilesim,
                containerColor = basiliMurekkep(fabEtkilesim),
                contentColor = MaterialTheme.colorScheme.onPrimary,
                modifier = Modifier.basilincaKucul(fabEtkilesim, DUGME_BASILI)
            ) {
                Icon(MotorumIkonlari.Ekle, contentDescription = stringResource(R.string.kayit_ekle))
            }
        }
    ) { icPadding ->
        // icPadding UYGULANMAK ZORUNDA: yoksa icerik ust barin altinda ve
        // alt barin arkasinda kalir.
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(icPadding)
                .padding(horizontal = AppSpacing.normal)
        ) {
            when {
                // Daire sadece gosterecek bir sey yokken cikiyor. Elde veri
                // varken listeyi daireyle degistirmek, sekmeden her donuste
                // listeyi sifirdan kurup kaydirma konumunu sifirliyordu.
                uiState.yukleniyor && uiState.kayitlar.isEmpty() -> MotorcuYukleniyor(
                    mesaj = stringResource(R.string.kayitlar_geliyor),
                    modifier = Modifier.align(Alignment.Center)
                )

                // Baglanti yokken aciklama kullaniciyi rahatlatiyor: kayitlar
                // kaybolmadi. Baska bir hatada (oturum vb.) boyle bir soz
                // veremiyoruz, sadece hatanin kendisi yaziyor.
                uiState.hata != null -> BosDurum(
                    gorsel = BosGorsel.BAGLANTI_YOK,
                    baslik = uiState.hata,
                    aciklama = if (uiState.hata == INTERNET_YOK) {
                        stringResource(R.string.cevrimdisi_aciklama)
                    } else {
                        null
                    },
                    eylem = stringResource(R.string.tekrar_dene),
                    eylemIkonu = MotorumIkonlari.Yenile,
                    onEylem = onTekrarDeneTikla,
                    anaEylem = false,
                    modifier = Modifier.align(Alignment.Center)
                )

                // Motoru olmayan kullaniciya hata gostermek yanlis olurdu:
                // ortada bir sorun yok, sadece yapmasi gereken bir adim var.
                //
                // Dugme once Motorlarim sekmesine gonderiyordu; kullanici orada
                // bir kez daha "Motor Ekle"ye basmak zorundaydi. Artik dogrudan
                // ekleme formu aciliyor ve eklenen motor otomatik seciliyor.
                uiState.motorYok -> BosDurum(
                    gorsel = BosGorsel.MOTOR_YOK,
                    baslik = stringResource(R.string.motor_yok_baslik),
                    aciklama = stringResource(R.string.motor_yok_aciklama),
                    eylem = stringResource(R.string.motor_ekle),
                    eylemIkonu = MotorumIkonlari.Ekle,
                    onEylem = onMotorEkleTikla,
                    modifier = Modifier.align(Alignment.Center)
                )

                // Asagi cekme hareketi kaydirilabilir cocuktan geliyor, bu yuzden
                // sadece liste durumunu sariyor. Hata ve motor yok ekranlarinin
                // kendi butonlari var, orada cekmek bir sey cozmuyor.
                else -> PullToRefreshBox(
                    isRefreshing = uiState.yenileniyor,
                    onRefresh = onAsagiCek,
                    // Durumu disaridan veriyoruz cunku gostergenin de ayni
                    // cekme oranini okumasi gerekiyor.
                    state = cekmeDurumu,
                    indicator = {
                        MotorcuCekmeGostergesi(
                            ilerleme = cekmeDurumu.distanceFraction,
                            yenileniyor = uiState.yenileniyor,
                            modifier = Modifier.align(Alignment.TopCenter)
                        )
                    },
                    modifier = Modifier.fillMaxSize()
                ) {
                    Liste(
                        uiState = uiState,
                        havaHali = havaHali,
                        seciliKategori = seciliKategori,
                        onKategoriSec = onKategoriSec,
                        onKayitTikla = onKayitTikla,
                        onEkleTikla = onEkleTikla,
                        onIstatistikTikla = onIstatistikTikla,
                        onKayitKaydirarakSil = onKayitKaydirarakSil,
                        onHatirlatmaTikla = onHatirlatmaTikla,
                        onIzinIste = onIzinIste,
                        onHavaTekrarDene = onHavaTekrarDene
                    )
                }
            }
        }
    }

    if (secici.panelAcik) {
        MotorSecimPaneli(
            motorlar = secici.motorlar,
            seciliMotorId = secici.seciliMotor?.id,
            onMotorSec = onMotorSec,
            onMotorEkleTikla = onMotorEkleTikla,
            onKapat = onPanelKapat
        )
    }
}

@Composable
private fun Liste(
    uiState: KayitUiState,
    havaHali: HavaDurumuHali,
    seciliKategori: Kategori?,
    onKategoriSec: (Kategori?) -> Unit,
    onKayitTikla: (String) -> Unit,
    onEkleTikla: () -> Unit,
    onIstatistikTikla: () -> Unit,
    onKayitKaydirarakSil: (String) -> Unit,
    onHatirlatmaTikla: (String) -> Unit,
    onIzinIste: () -> Unit,
    onHavaTekrarDene: () -> Unit
) {
    val listeDurumu = rememberLazyListState()
    val yogunluk = LocalDensity.current

    // Basligin kapladigi yuksekligi olcup listeye bildiriyoruz: baslik listenin
    // icinde degil, uzerinde ciziliyor. Olcmezsek kayitlar onun altinda kalir.
    var baslikYuksekligi by remember { mutableIntStateOf(0) }

    // Basligin tepeden uzakligi. Hava karti gorunurken onun altinda duruyor,
    // kart yukari kayip cikinca sifira oturup orada kaliyor.
    //
    // derivedStateOf sart: kaydirma her karede degisiyor, onu dogrudan
    // okusaydik ust bolum saniyede altmis kez yeniden cizilirdi.
    val araBosluk = with(yogunluk) { AppSpacing.orta.roundToPx() }
    val ustBosluk by remember {
        derivedStateOf {
            val hava = listeDurumu.layoutInfo.visibleItemsInfo.firstOrNull { it.index == 0 }
            // hava = null: kart tamamen yukarida kalmis demek.
            if (hava == null) 0 else maxOf(hava.offset + hava.size + araBosluk, 0)
        }
    }

    // Baslik tepeye oturdugu an ozet kart tek satira iniyor.
    val kucult by remember { derivedStateOf { ustBosluk == 0 } }

    // Filtre ve siralama listede yapiliyor: depo ekleme sirasini koruyor,
    // ekran nasil gostermek istedigine kendisi karar veriyor. remember ile
    // her cizimde degil, liste ya da filtre degisince hesaplaniyor.
    val kayitlar = remember(uiState.kayitlar, seciliKategori) {
        uiState.kayitlar
            .filter { seciliKategori == null || it.kategori == seciliKategori }
            .sortedByDescending { it.tarihMillis }
    }

    // Filtre degisince liste basa doner. Olmazsa kaydirma konumu oldugu yerde
    // kaliyordu: dar bir filtreden genisine gecince ekran listenin ortasinda
    // aciliyor, kayitlar ust uste ekleniyormus gibi gorunuyordu.
    LaunchedEffect(seciliKategori) {
        listeDurumu.scrollToItem(0)
    }

    Box(modifier = Modifier.fillMaxSize()) {

        LazyColumn(
            state = listeDurumu,
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.orta)
        ) {
            // Hava karti listeyle birlikte kayip gidiyor: tasarimi bozulmuyor
            // ama ekranin ustunu surekli isgal de etmiyor.
            item {
                HavaDurumuKarti(
                    hal = havaHali,
                    onIzinIste = onIzinIste,
                    onTekrarDene = onHavaTekrarDene,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            // Baslik listenin uzerinde durdugu icin listede yerini bos
            // birakiyoruz; yoksa ilk kayitlar basligin altinda gizli kalirdi.
            item {
                Spacer(Modifier.height(with(yogunluk) { baslikYuksekligi.toDp() }))
            }

            // Iki ayri bos hal: motorda hic kayit yok (ilk adimi atmaya cagir)
            // ya da secili filtrede kayit yok (sadece bilgi ver, cip degistirir).
            if (kayitlar.isEmpty()) {
                item {
                    if (seciliKategori == null) {
                        BosDurum(
                            gorsel = BosGorsel.KAYIT_YOK,
                            baslik = stringResource(R.string.kayit_yok_baslik),
                            aciklama = stringResource(R.string.kayit_yok_aciklama),
                            eylem = stringResource(R.string.ilk_kaydi_ekle),
                            eylemIkonu = MotorumIkonlari.Ekle,
                            onEylem = onEkleTikla,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(top = AppSpacing.genis)
                        )
                    } else {
                        Text(
                            text = stringResource(R.string.filtrede_kayit_yok),
                            style = MaterialTheme.typography.bodyMedium,
                            color = MetinIkincil,
                            modifier = Modifier.padding(top = AppSpacing.kucuk)
                        )
                    }
                }
            }

            items(kayitlar, key = { it.id }) { kayit ->
                KayitSatiri(
                    kayit = kayit,
                    onTikla = { onKayitTikla(kayit.id) },
                    onKaydirarakSil = { onKayitKaydirarakSil(kayit.id) },
                    onHatirlatmaTikla = { onHatirlatmaTikla(kayit.id) },
                    //KayitSatiri — Modifier.clickable { onTikla() }.
                    // Satır hangi kaydın olduğunu bilir ama nereye gidileceğini bilmez;
                    //onTikla = { onKayitTikla(kayit.id) } ile id'yi yukarı taşır.
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }

        // BASLIK: listenin uzerinde ciziliyor, kaydirmaya gore yer degistiriyor.
        // graphicsLayer kullanildigi icin sadece cizim kayiyor, duzen yeniden
        // hesaplanmiyor; her karede calisan bir sey icin dogru olan bu.
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .onSizeChanged { baslikYuksekligi = it.height }
                .graphicsLayer { translationY = ustBosluk.toFloat() }
                // Zemin rengi sart: altindan kayan kayitlar basligin icinden
                // gorunurdu.
                .background(MaterialTheme.colorScheme.background)
                .padding(bottom = AppSpacing.kucuk),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.orta)
        ) {
            // Kart ile serit birbirinin yerini aliyor. Ayni sure ve ayni egri
            // kullanildigi icin iki ayri animasyon degil, tek bir kuculme gibi
            // okunuyor.
            AnimatedVisibility(
                visible = !kucult,
                enter = expandVertically(tween(AppMotion.SAYFA, easing = AppMotion.egri)) + fadeIn(),
                exit = shrinkVertically(tween(AppMotion.SAYFA, easing = AppMotion.egri)) + fadeOut()
            ) {
                ToplamCard(
                    toplamTutar = uiState.toplamTutar,
                    toplamLitre = uiState.toplamLitre,
                    gidilenYol = uiState.gidilenYol,
                    onTikla = onIstatistikTikla,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            AnimatedVisibility(
                visible = kucult,
                enter = expandVertically(tween(AppMotion.SAYFA, easing = AppMotion.egri)) + fadeIn(),
                exit = shrinkVertically(tween(AppMotion.SAYFA, easing = AppMotion.egri)) + fadeOut()
            ) {
                OzetSerit(
                    toplamTutar = uiState.toplamTutar,
                    toplamLitre = uiState.toplamLitre,
                    gidilenYol = uiState.gidilenYol,
                    onTikla = onIstatistikTikla,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            FiltreCipleri(
                secili = seciliKategori,
                onSec = onKategoriSec,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun AnaSayfaIcerikPreview() {
    MotorumTheme {
        AnaSayfaIcerik(
            uiState = KayitUiState(),
            secici = MotorSeciciUiState(yukleniyor = false),
            seciliKategori = null,
            onKategoriSec = {},
            havaHali = HavaDurumuHali.Hazir(
                HavaDurumu(
                    sehir = "Akhisar",
                    sicaklik = 23.9,
                    aciklama = "parçalı bulutlu",
                    kod = 803,
                    ruzgarHizi = 2.0,
                    gorusMesafesi = 10000
                )
            ),
            snackbarDurumu = remember { SnackbarHostState() },
            onSekmeTikla = {},
            onEkleTikla = {},
            onKayitTikla = {},
            onIstatistikTikla = {},
            onKayitKaydirarakSil = {},
            onAsagiCek = {},
            onIzinIste = {},
            onHavaTekrarDene = {},
            onTekrarDeneTikla = {},
            onCipTikla = {},
            onMotorSec = {},
            onMotorEkleTikla = {},
            onPanelKapat = {}
        )
    }
}

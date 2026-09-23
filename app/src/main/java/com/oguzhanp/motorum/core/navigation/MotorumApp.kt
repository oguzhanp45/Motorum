package com.oguzhanp.motorum.core.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.NavType
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import androidx.navigation.navigation
import com.oguzhanp.motorum.data.hatirlatma.HatirlatmaIstegi
import com.oguzhanp.motorum.feature.ayarlar.AyarlarSayfasi
import com.oguzhanp.motorum.feature.belge.BelgeSayfasi
import com.oguzhanp.motorum.feature.belge.BelgelerSayfasi
import com.oguzhanp.motorum.feature.kayit.detay.KayitDetaySayfasi
import com.oguzhanp.motorum.feature.kayit.ekle.KayitEkleSayfasi
import com.oguzhanp.motorum.feature.anasayfa.AnaSayfa
import com.oguzhanp.motorum.feature.anasayfa.KayitViewModel
import com.oguzhanp.motorum.feature.istatistik.IstatistikSayfasi
import com.oguzhanp.motorum.feature.kimlik.GirisSayfasi
import com.oguzhanp.motorum.feature.kimlik.GirisViewModel
import com.oguzhanp.motorum.feature.kimlik.UyeOlSayfasi
import com.oguzhanp.motorum.feature.kimlik.UyeOlViewModel
import com.oguzhanp.motorum.feature.motorlarim.MotorDetaySayfasi
import com.oguzhanp.motorum.feature.motorlarim.MotorlarimSayfasi
import com.oguzhanp.motorum.feature.onboarding.OnboardingSayfasi
import com.oguzhanp.motorum.feature.onboarding.OnboardingViewModel

// Uygulamanin kokü: NavHost burada.
@Composable
fun MotorumApp(
    baslangicRotasi: String,
    // Bildirimin govdesine dokunulduysa dolu gelir. Islenince tuketiliyor.
    hatirlatmaIstegi: HatirlatmaIstegi? = null,
    onHatirlatmaIstegiIslendi: () -> Unit = {}
) {
    // Gecmisi (back stack) tutan nesne
    val navController = rememberNavController()

    // DIKKAT: hiltViewModel() NavHost'un DISINDA cagriliyor.
    // Iceride cagirsaydin her ekran ayri bir ViewModel alirdi,
    // ekleme sayfasi kendi listesine yazar, ana sayfa bos kalirdi.
    val viewModel: KayitViewModel = hiltViewModel()
    // MainActivity'deki ile ayni ornek: hiltViewModel() NavHost disinda cagrildigi
    // icin sahibi Activity oluyor.
    val onboardingViewModel: OnboardingViewModel = hiltViewModel()
    val girisViewModel: GirisViewModel = hiltViewModel()
    val uyeOlViewModel: UyeOlViewModel = hiltViewModel()

    // Sekme ve alt baglanti gecisleri: gidilecek rotayi yigindan cikarip yeniden koyuyor.
    // Boylece yigin ikiyi gecmiyor ve giris ekraninda geri tusu uygulamadan cikiyor.
    fun kimlikGecisi(hedef: String) {
        navController.navigate(hedef) {
            popUpTo(hedef) { inclusive = true }
            launchSingleTop = true
        }
    }

    // Google'in bottom bar icin onerdigi kalip. saveState/restoreState ikilisi
    // her sekmenin kendi gecmisini ve kaydirma konumunu sakliyor; popUpTo ise
    // yigini sisirmiyor, geri tusu her sekmeden ana sayfaya donuyor.
    fun sekmeyeGec(hedef: String) {
        navController.navigate(hedef) {
            popUpTo(Routes.ANA_SAYFA) { saveState = true }
            launchSingleTop = true
            restoreState = true
        }
    }

    fun anaBolgeyeGec(silinecek: String) {
        navController.navigate(Routes.ANA_BOLGE) {
            popUpTo(silinecek) { inclusive = true }
        }
    }

    fun girisEDon() {
        navController.navigate(Routes.GIRIS) {
            popUpTo(Routes.ANA_BOLGE) { inclusive = true }
        }
    }

    NavHost(
        navController = navController,
        startDestination = baslangicRotasi,
        // Gecisler tek yerde (Gecisler.kt): her ekran turune gore alttan,
        // sagdan ya da yerinde. Ekran eklerken buraya dokunmak gerekmiyor,
        // sadece rotayi Gecisler.kt'deki listeye eklemek yetiyor.
        enterTransition = { ekranGirisi() },
        exitTransition = { ekranCikisi() },
        popEnterTransition = { geriGirisi() },
        popExitTransition = { geriCikisi() },
        // Parmakla geri kaydirma (tahminli geri) ayri iki kural kullaniyor ve
        // Navigation'in varsayilani sayfayi %70'e kucultuyor. Verilmezse geri
        // hareketinde sayfa once kuculup sonra gidiyordu; ayni kurallari
        // buraya da veriyoruz. (Int: kaydirmanin hangi kenardan basladigi.)
        predictivePopEnterTransition = { geriGirisi() },
        predictivePopExitTransition = { geriCikisi() }
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingSayfasi(
                onOnboardingBitti = {
                    onboardingViewModel.tamamla()
                    // inclusive = true: onboarding gecmisten tamamen silinir.
                    // Yoksa uye ol ekraninda geri tusuna basinca tanitima geri donulur.
                    // Tanitimi yeni bitiren kisinin hesabi yok: uye ol ekranina gidiyor.
                    navController.navigate(Routes.UYE_OL) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.GIRIS) {
            GirisSayfasi(
                viewModel = girisViewModel,
                onUyeOlaGit = { kimlikGecisi(Routes.UYE_OL) },
                onGirisBasarili = { anaBolgeyeGec(Routes.GIRIS) }
            )
        }
        composable(Routes.UYE_OL) {
            UyeOlSayfasi(
                viewModel = uyeOlViewModel,
                onGirisEGit = { kimlikGecisi(Routes.GIRIS) },
                onUyeOlBasarili = { anaBolgeyeGec(Routes.UYE_OL) }
            )
        }

        // Uc sekme kendi grafinda. Alt bar sadece bu grafin icinde ciziliyor.
        navigation(route = Routes.ANA_BOLGE, startDestination = Routes.ANA_SAYFA) {
            composable(Routes.ANA_SAYFA) {
                AnaSayfa(
                    viewModel = viewModel,
                    navController = navController,
                    onSekmeTikla = ::sekmeyeGec
                )
            }
            composable(Routes.MOTORLARIM) {
                MotorlarimSayfasi(
                    onSekmeTikla = ::sekmeyeGec,
                    onMotorEkleTikla = { navController.navigate(Routes.motorEkleRotasi()) },
                    onMotorDuzenleTikla = { id -> navController.navigate("motor_detay/$id") }
                )
            }
            composable(Routes.AYARLAR) {
                AyarlarSayfasi(
                    onSekmeTikla = ::sekmeyeGec,
                    onCikisYapildi = ::girisEDon,
                    onBelgelerTikla = { navController.navigate(Routes.BELGELER) }
                )
            }
        }

        composable(
            route = Routes.MOTOR_EKLE,
            // Istege bagli parametre icin varsayilan sart: rota "?sec=" olmadan
            // cagrildiginda Navigation bu degeri kullaniyor.
            arguments = listOf(
                navArgument("sec") { type = NavType.BoolType; defaultValue = false }
            )
        ) { backStackEntry ->
            // motorId null: sayfa "yeni motor" kipinde aciliyor.
            MotorDetaySayfasi(
                navController = navController,
                motorId = null,
                secilsin = backStackEntry.arguments?.getBoolean("sec") == true
            )
        }
        composable(Routes.MOTOR_DETAY) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: return@composable
            MotorDetaySayfasi(navController = navController, motorId = id)
        }
        composable(
            route = Routes.KAYIT_EKLE_KALIBI,
            arguments = listOf(navArgument("kopya") { type = NavType.StringType; defaultValue = "" })
        ) {
            // ekleViewModel verilmiyor: ekran onu kendisi uretiyor (hiltViewModel() varsayilani)
            KayitEkleSayfasi(
                navController = navController,
                kayitViewModel = viewModel
            )
        }
        composable(Routes.KAYIT_DETAY) { backStackEntry ->
            val id = backStackEntry.arguments?.getString("id") ?: return@composable
            KayitDetaySayfasi(
                kayitViewModel = viewModel,
                navController = navController,
                kayitId = id
            )
        }
        composable(
            route = Routes.BELGE,
            arguments = listOf(
                navArgument("id") { type = NavType.StringType; defaultValue = "" },
                navArgument("tur") { type = NavType.StringType; defaultValue = "" }
            )
        ) {
            // Parametreleri sayfanin ViewModel'i kendisi okuyor (SavedStateHandle).
            BelgeSayfasi(navController = navController)
        }
        composable(Routes.BELGELER) {
            BelgelerSayfasi(navController = navController)
        }
        composable(Routes.ISTATISTIK) {
            // Ayni viewModel veriliyor: istatistik sayfasinin kendi deposu yok,
            // ana sayfanin cektigi listeden hesapliyor.
            IstatistikSayfasi(
                kayitViewModel = viewModel,
                navController = navController
            )
        }
    }

    // Bildirimden gelindi: ana sayfaya don ve o kaydin panelini ac. NavHost
    // kurulduktan sonra calisiyor, grafik hazir. Oturum kontrolunu MainActivity
    // yapti; oturum yoksa buraya hic gelmiyor.
    LaunchedEffect(hatirlatmaIstegi) {
        val istek = hatirlatmaIstegi ?: return@LaunchedEffect
        navController.navigate(Routes.ANA_SAYFA) {
            // Ustte acik bir sayfa varsa (detay, istatistik) kapaniyor.
            popUpTo(Routes.ANA_SAYFA)
            launchSingleTop = true
        }
        if (istek.belgeler) {
            // Belge bildirimi: once dogru motor, sonra Belgeler sayfasi. Sira
            // onemli; sayfa acilir acilmaz secili motorun belgelerini cekiyor.
            viewModel.motoraGec(istek.motorId) {
                navController.navigate(Routes.BELGELER) { launchSingleTop = true }
            }
        } else {
            viewModel.hatirlatmaPaneliniAc(istek.kayitId, istek.motorId)
        }
        onHatirlatmaIstegiIslendi()
    }
}

package com.oguzhanp.motorum

import androidx.compose.runtime.Composable
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.oguzhanp.motorum.data.KimlikDeposu
import com.oguzhanp.motorum.ui.detay.KayitDetaySayfasi
import com.oguzhanp.motorum.ui.ekle.KayitEkleSayfasi
import com.oguzhanp.motorum.ui.home.AnaSayfa
import com.oguzhanp.motorum.ui.home.KayitViewModel
import com.oguzhanp.motorum.ui.kimlik.GirisSayfasi
import com.oguzhanp.motorum.ui.kimlik.GirisViewModel
import com.oguzhanp.motorum.ui.kimlik.UyeOlSayfasi
import com.oguzhanp.motorum.ui.kimlik.UyeOlViewModel
import com.oguzhanp.motorum.ui.navigation.Routes
import com.oguzhanp.motorum.ui.onboarding.OnboardingSayfasi
import com.oguzhanp.motorum.ui.onboarding.OnboardingViewModel

// Uygulamanin kokü: NavHost burada.
@Composable
fun MotorumApp(
    baslangicRotasi: String,
    // Compose'a dogrudan enjeksiyon yok: depoyu MainActivity aliyor, buraya veriyor.
    kimlikDeposu: KimlikDeposu
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

    fun anaSayfayaGec(silinecek: String) {
        navController.navigate(Routes.ANA_SAYFA) {
            popUpTo(silinecek) { inclusive = true }
        }
    }

    NavHost(
        navController = navController,
        startDestination = baslangicRotasi
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
                onGirisBasarili = { anaSayfayaGec(Routes.GIRIS) }
            )
        }
        composable(Routes.UYE_OL) {
            UyeOlSayfasi(
                viewModel = uyeOlViewModel,
                onGirisEGit = { kimlikGecisi(Routes.GIRIS) },
                onUyeOlBasarili = { anaSayfayaGec(Routes.UYE_OL) }
            )
        }
        composable(Routes.ANA_SAYFA) {
            AnaSayfa(
                viewModel = viewModel,
                navController = navController,
                onCikisTikla = {
                    kimlikDeposu.cikisYap()
                    navController.navigate(Routes.GIRIS) {
                        popUpTo(Routes.ANA_SAYFA) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.KAYIT_EKLE) {
            // ekleViewModel verilmiyor: ekran onu kendisi uretiyor (hiltViewModel() varsayilani)
            KayitEkleSayfasi(
                navController = navController
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
    }
}

package com.oguzhanp.motorum

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.oguzhanp.motorum.ui.detay.KayitDetaySayfasi
import com.oguzhanp.motorum.ui.ekle.KayitEkleSayfasi
import com.oguzhanp.motorum.ui.home.AnaSayfa
import com.oguzhanp.motorum.ui.home.KayitViewModel
import com.oguzhanp.motorum.ui.navigation.Routes
import com.oguzhanp.motorum.ui.onboarding.OnboardingSayfasi
import com.oguzhanp.motorum.ui.onboarding.OnboardingViewModel

// Uygulamanin kokü: NavHost burada.
@Composable
fun MotorumApp(baslangicRotasi: String) {
    // Gecmisi (back stack) tutan nesne
    val navController = rememberNavController()

    // DIKKAT: viewModel() NavHost'un DISINDA cagriliyor.
    // Iceride cagirsaydin her ekran ayri bir ViewModel alirdi,
    // ekleme sayfasi kendi listesine yazar, ana sayfa bos kalirdi.
    val viewModel: KayitViewModel = viewModel()
    // MainActivity'deki ile ayni ornek: viewModel() NavHost disinda cagrildigi
    // icin sahibi Activity oluyor.
    val onboardingViewModel: OnboardingViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = baslangicRotasi
    ) {
        composable(Routes.ONBOARDING) {
            OnboardingSayfasi(
                onOnboardingBitti = {
                    onboardingViewModel.tamamla()
                    // inclusive = true: onboarding gecmisten tamamen silinir.
                    // Yoksa ana sayfada geri tusuna basinca tanitima geri donulur.
                    navController.navigate(Routes.ANA_SAYFA) {
                        popUpTo(Routes.ONBOARDING) { inclusive = true }
                    }
                }
            )
        }
        composable(Routes.ANA_SAYFA) {
            AnaSayfa(
                viewModel = viewModel,
                navController = navController
            )
        }
        composable(Routes.KAYIT_EKLE) {
            // ekleViewModel verilmiyor: ekran onu kendisi uretiyor (viewModel() varsayilani)
            KayitEkleSayfasi(
                kayitViewModel = viewModel,
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

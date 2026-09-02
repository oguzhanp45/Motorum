package com.oguzhanp.motorum

import androidx.compose.material3.Scaffold
import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.oguzhanp.motorum.ui.ekle.KayitEkleSayfasi
import com.oguzhanp.motorum.ui.home.AnaSayfa
import com.oguzhanp.motorum.ui.home.KayitViewModel
import com.oguzhanp.motorum.ui.navigation.Routes

// Uygulamanin kokü: NavHost burada.
@Composable
fun MotorumApp() {
    // Gecmisi (back stack) tutan nesne
    val navController = rememberNavController()

    // DIKKAT: viewModel() NavHost'un DISINDA cagriliyor.
    // Iceride cagirsaydin her ekran ayri bir ViewModel alirdi,
    // ekleme sayfasi kendi listesine yazar, ana sayfa bos kalirdi.
    val viewModel: KayitViewModel = viewModel()

    NavHost(
        navController = navController,
        startDestination = Routes.ANA_SAYFA
    ) {
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
    }
}

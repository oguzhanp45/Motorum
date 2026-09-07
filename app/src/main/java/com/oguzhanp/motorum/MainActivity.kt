package com.oguzhanp.motorum

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oguzhanp.motorum.data.KimlikDeposu
import com.oguzhanp.motorum.ui.navigation.Routes
import com.oguzhanp.motorum.ui.onboarding.OnboardingViewModel
import com.oguzhanp.motorum.ui.theme.MotorumTheme

class MainActivity : ComponentActivity() {

    private val onboardingViewModel: OnboardingViewModel by viewModels()
    private val kimlikDeposu = KimlikDeposu()

    override fun onCreate(savedInstanceState: Bundle?) {
        // super.onCreate'ten ONCE cagrilmali, yoksa splash devreye girmez.
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Bayrak okunana kadar splash ekranda kalir. Yapay gecikme yok:
        // suresi diskten okuma ne kadar suruyorsa o kadar.
        // Boylece kullanici bir an yanlis ekrani gormuyor.
        splashScreen.setKeepOnScreenCondition {
            onboardingViewModel.onboardingBitti.value == null
        }

        setContent {
            MotorumTheme {
                val bitti by onboardingViewModel.onboardingBitti.collectAsStateWithLifecycle()

                if (bitti != null) {
                    // Uc kosul, sirayla: tanitim bitti mi, oturum acik mi.
                    MotorumApp(
                        baslangicRotasi = when {
                            bitti != true -> Routes.ONBOARDING
                            kimlikDeposu.oturumAcik -> Routes.ANA_SAYFA
                            else -> Routes.GIRIS
                        }
                    )
                }
            }
        }
    }
}



@Preview(showBackground = true)
@Composable
fun AnaSayfaPreview() {
    MotorumTheme {

    }
}

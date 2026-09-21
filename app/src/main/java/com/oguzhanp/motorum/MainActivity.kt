package com.oguzhanp.motorum

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oguzhanp.motorum.data.HatirlatmaIstegi
import com.oguzhanp.motorum.data.KimlikDeposu
import com.oguzhanp.motorum.data.hatirlatmaIstegi
import com.oguzhanp.motorum.model.TemaSecimi
import com.oguzhanp.motorum.ui.navigation.Routes
import com.oguzhanp.motorum.ui.onboarding.OnboardingViewModel
import com.oguzhanp.motorum.ui.theme.MotorumTheme
import com.oguzhanp.motorum.ui.theme.TemaViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@AndroidEntryPoint
class MainActivity : ComponentActivity() {

    private val onboardingViewModel: OnboardingViewModel by viewModels()
    private val temaViewModel: TemaViewModel by viewModels()

    // Bildirimin govdesinden gelen istek. Ekran onu isleyince bosaltiyor.
    private val hatirlatmaIstegi = MutableStateFlow<HatirlatmaIstegi?>(null)

    // Yapiciya veremiyoruz: Activity'yi sistem uretiyor. Hilt bu alani
    // onCreate'ten once dolduruyor, o yuzden lateinit.
    @Inject
    lateinit var kimlikDeposu: KimlikDeposu

    override fun onCreate(savedInstanceState: Bundle?) {
        // super.onCreate'ten ONCE cagrilmali, yoksa splash devreye girmez.
        val splashScreen = installSplashScreen()
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        // Ekran donup yeniden kurulursa ayni istegi ikinci kez islemiyoruz.
        if (savedInstanceState == null) bildirimIsteginiAl(intent)

        // Bayrak okunana kadar splash ekranda kalir. Yapay gecikme yok:
        // suresi diskten okuma ne kadar suruyorsa o kadar.
        // Boylece kullanici bir an yanlis ekrani gormuyor.
        // Tema tercihi de ayni diskten, ayni anda okunuyor; ikisi gelene kadar.
        splashScreen.setKeepOnScreenCondition {
            onboardingViewModel.onboardingBitti.value == null ||
                    temaViewModel.secim.value == null
        }

        setContent {
            val secim by temaViewModel.secim.collectAsStateWithLifecycle()
            val karanlik = when (secim) {
                TemaSecimi.ACIK -> false
                TemaSecimi.KARANLIK -> true
                else -> isSystemInDarkTheme()
            }

            // Durum cubugu ikonlari varsayilan olarak TELEFONUN temasina bakiyor.
            // Telefon acikken uygulamada karanlik secilirse koyu zeminde koyu
            // ikonlar kalirdi; bu yuzden tema her degistiginde yeniden ayarlaniyor.
            DisposableEffect(karanlik) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { karanlik },
                    navigationBarStyle = SystemBarStyle.auto(ACIK_PERDE, KOYU_PERDE) { karanlik }
                )
                onDispose {}
            }

            MotorumTheme(karanlik = karanlik) {
                val bitti by onboardingViewModel.onboardingBitti.collectAsStateWithLifecycle()
                val bekleyenIstek by hatirlatmaIstegi.collectAsStateWithLifecycle()

                if (bitti != null) {
                    // Uc kosul, sirayla: tanitim bitti mi, oturum acik mi.
                    MotorumApp(
                        baslangicRotasi = when {
                            bitti != true -> Routes.ONBOARDING
                            // Ic grafa gidiliyor; NavHost onun baslangic
                            // noktasini (ana sayfa) kendisi seciyor.
                            kimlikDeposu.oturumAcik -> Routes.ANA_BOLGE
                            else -> Routes.GIRIS
                        },
                        hatirlatmaIstegi = bekleyenIstek,
                        onHatirlatmaIstegiIslendi = { hatirlatmaIstegi.value = null }
                    )
                }
            }
        }
    }

    // Uygulama zaten acikken bildirime basildi: Activity yeniden kurulmuyor
    // (SINGLE_TOP), yeni niyet buraya geliyor.
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        bildirimIsteginiAl(intent)
    }

    // Govdeye dokununca bildirim kendiliginden kapaniyor (setAutoCancel).
    private fun bildirimIsteginiAl(intent: Intent) {
        val istek = intent.hatirlatmaIstegi() ?: return
        if (kimlikDeposu.oturumAcik) hatirlatmaIstegi.value = istek
    }
}



// enableEdgeToEdge'in kendi varsayilan perde renkleri: 3 tusla gezinen
// telefonlarda alt cubugun arkasi. Hareketle gezinmede hic gorunmuyor.
private val ACIK_PERDE = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
private val KOYU_PERDE = Color.argb(0x80, 0x1b, 0x1b, 0x1b)

@Preview(showBackground = true)
@Composable
fun AnaSayfaPreview() {
    MotorumTheme {

    }
}

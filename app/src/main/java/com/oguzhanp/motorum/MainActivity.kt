package com.oguzhanp.motorum

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.oguzhanp.motorum.core.navigation.MotorumApp
import com.oguzhanp.motorum.core.navigation.Routes
import com.oguzhanp.motorum.core.tasarim.MotorumTheme
import com.oguzhanp.motorum.core.tasarim.TemaViewModel
import com.oguzhanp.motorum.data.hatirlatma.HatirlatmaIstegi
import com.oguzhanp.motorum.data.hatirlatma.hatirlatmaIstegi
import com.oguzhanp.motorum.data.kimlik.KimlikDeposu
import com.oguzhanp.motorum.feature.acilis.AcilisEkrani
import com.oguzhanp.motorum.feature.onboarding.OnboardingViewModel
import com.oguzhanp.motorum.model.TemaSecimi
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {

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
                // rememberSaveable: ekran donunce acilis tekrar oynamasin.
                var acilisSuruyor by rememberSaveable { mutableStateOf(true) }

                // Uygulama acilis perdesinin ALTINDA hemen kuruluyor: perde
                // kalktiginda ilk ekran hazir, beklenmiyor.
                Box(Modifier.fillMaxSize()) {
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

                    AnimatedVisibility(
                        visible = acilisSuruyor,
                        enter = EnterTransition.None,
                        exit = fadeOut(tween(ACILIS_CIKIS_MS))
                    ) {
                        AcilisEkrani(
                            hazir = bitti != null && secim != null,
                            onBitti = { acilisSuruyor = false }
                        )
                    }
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
// Acilis perdesinin kalkma suresi.
private const val ACILIS_CIKIS_MS = 300

private val ACIK_PERDE = Color.argb(0xe6, 0xFF, 0xFF, 0xFF)
private val KOYU_PERDE = Color.argb(0x80, 0x1b, 0x1b, 0x1b)

@Preview(showBackground = true)
@Composable
fun AnaSayfaPreview() {
    MotorumTheme {

    }
}

package com.oguzhanp.motorum.core.tasarim

import android.app.Activity
import android.content.Context
import android.content.ContextWrapper
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.platform.LocalInspectionMode
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import com.oguzhanp.motorum.core.tasarim.karanlikTema

// Ustu iki temada da koyu olan ekranlar (acilis, giris) icin: durum cubugu
// ikonlari ekran gorunurken beyaz, cikinca eski haline donuyor.
//
// Ayni anda birden fazla ekran isteyebiliyor: acilis perdesinin altinda giris
// ekrani da kurulu duruyor. Her biri "eski deger"i kendisi saklasaydi, ilk
// kapanan ikonlari koyuya cevirirdi, oysa digeri hala ekranda. Bu yuzden
// sayiyoruz: ilk gelen eski degeri sakliyor, son giden geri koyuyor.
@Composable
fun AcikDurumCubugu() {
    if (LocalInspectionMode.current) return
    val gorunum = LocalView.current
    // Tema degisince MainActivity cubugu yeniden ayarliyor; biz de ardindan
    // tekrar beyaza ceviriyoruz.
    DisposableEffect(gorunum, karanlikTema) {
        val denetci = gorunum.context.aktivite()?.window
            ?.let { WindowCompat.getInsetsController(it, gorunum) }
        if (denetci != null) {
            if (istekSayisi == 0) oncekiDeger = denetci.isAppearanceLightStatusBars
            istekSayisi++
            denetci.isAppearanceLightStatusBars = false
        }
        onDispose {
            if (denetci != null) {
                istekSayisi--
                if (istekSayisi == 0) denetci.isAppearanceLightStatusBars = oncekiDeger
            }
        }
    }
}

// Uygulamada tek pencere var; sayac da tek.
private var istekSayisi = 0
private var oncekiDeger = true

// Compose'un context'i dogrudan Activity olmayabilir (sarmalanmis olabilir).
private tailrec fun Context.aktivite(): Activity? = when (this) {
    is Activity -> this
    is ContextWrapper -> baseContext.aktivite()
    else -> null
}

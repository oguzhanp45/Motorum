package com.oguzhanp.motorum.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.net.toUri
import com.oguzhanp.motorum.model.Kayit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

// Iki alarmi birbirinden ayiran adres. Kayit kimligi buraya giriyor.
private const val HATIRLATMA_ADRESI = "motorum://hatirlatma/"

@Singleton
class HatirlatmaZamanlayici @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    private val alarmYoneticisi = context.getSystemService(AlarmManager::class.java)

    // setAndAllowWhileIdle: kesin olmayan alarm, izin istemiyor. Telefon uyku
    // modundayken de yaklasik zamaninda caliyor. setWindow kullansaydik uyku
    // modunda bir sonraki bakim penceresine ertelenirdi, bu da belirli bir gune
    // kurulan hatirlatmayi saatlerce kaydirabilirdi.
    fun kur(kayitId: String, baslik: String, zamanMillis: Long) {
        val yonetici = alarmYoneticisi ?: return

        // Gecmis bir zamana alarm kurmak anlamsiz: sistem onu hemen calardi.
        if (zamanMillis <= System.currentTimeMillis()) return

        val niyet = bekleyenNiyet(
            kayitId = kayitId,
            baslik = baslik,
            bayrak = PendingIntent.FLAG_UPDATE_CURRENT
        ) ?: return

        yonetici.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, zamanMillis, niyet)
    }

    // Kaydin son halini alarm tarafina yansitiyor. Once varsa iptal, sonra
    // hatirlatma varsa yeniden kur. Tek yol izliyoruz cunku guncellemede
    // hatirlatma degismis, eklenmis ya da tamamen kaldirilmis olabilir;
    // uc durumu ayri ayri kovalamak yerine hepsini bu sira kapsiyor.
    fun esitle(kayit: Kayit) {
        iptal(kayit.id)

        val bakim = kayit as? Kayit.Bakim ?: return
        val zaman = bakim.hatirlatmaMillis ?: return
        kur(kayitId = kayit.id, baslik = bakim.bakimTuru, zamanMillis = zaman)
    }

    fun iptal(kayitId: String) {
        val yonetici = alarmYoneticisi ?: return

        // FLAG_NO_CREATE: yoksa yenisini uretme, null don. Kurulu bir alarm
        // yoksa iptal edecek bir sey de yok.
        val niyet = bekleyenNiyet(
            kayitId = kayitId,
            baslik = "",
            bayrak = PendingIntent.FLAG_NO_CREATE
        ) ?: return

        yonetici.cancel(niyet)
        niyet.cancel()
    }

    // Alarmlari birbirinden ayiran sey Intent'in data alani; extras DEGIL.
    // PendingIntent esitligi Intent.filterEquals ile olculuyor ve o fonksiyon
    // extras'a hic bakmiyor. Kayit kimligini sadece extras'a koysaydik butun
    // hatirlatmalar "ayni alarm" sayilir, ikincisi birincisinin uzerine yazardi.
    private fun bekleyenNiyet(kayitId: String, baslik: String, bayrak: Int): PendingIntent? {
        val niyet = Intent(context, HatirlatmaAlicisi::class.java).apply {
            data = (HATIRLATMA_ADRESI + kayitId).toUri()
            putExtra(EK_KAYIT_ID, kayitId)
            putExtra(EK_BASLIK, baslik)
        }

        // FLAG_IMMUTABLE Android 12'den beri zorunlu: baskasi bu niyeti
        // degistiremesin. Bizim koydugumuz extras'i etkilemiyor.
        return PendingIntent.getBroadcast(
            context,
            0,
            niyet,
            bayrak or PendingIntent.FLAG_IMMUTABLE
        )
    }
}

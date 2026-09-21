package com.oguzhanp.motorum.data

import android.Manifest
import android.annotation.SuppressLint
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.content.ContextCompat
import androidx.core.net.toUri
import com.oguzhanp.motorum.MainActivity
import com.oguzhanp.motorum.R
import dagger.hilt.EntryPoint
import dagger.hilt.InstallIn
import dagger.hilt.android.EntryPointAccessors
import dagger.hilt.components.SingletonComponent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import kotlinx.serialization.json.Json
import java.util.Calendar

// Kanal kimligi ve ek alan adlari tek yerde: kanali MotorumUygulamasi kuruyor,
// alarmi HatirlatmaZamanlayici koyuyor, bildirimi burasi gosteriyor.
internal const val BILDIRIM_KANALI = "bakim_hatirlatma"
internal const val EK_KAYIT_ID = "kayit_id"
internal const val EK_HATIRLATMA = "hatirlatma"
internal const val EK_MOTOR_ID = "motor_id"

// Niyetin ne istedigi. Alarm caldi / "1 hafta ertele" / "Yaptirdim" /
// bildirimin govdesine dokunuldu (uygulamada paneli ac).
internal const val EYLEM_CAL = "com.oguzhanp.motorum.HATIRLATMA_CAL"
internal const val EYLEM_ERTELE = "com.oguzhanp.motorum.HATIRLATMA_ERTELE"
internal const val EYLEM_YAPTIRDIM = "com.oguzhanp.motorum.HATIRLATMA_YAPTIRDIM"
internal const val EYLEM_PANEL = "com.oguzhanp.motorum.HATIRLATMA_PANEL"

// Her kaydin kendi bildirimi: iki hatirlatma birbirinin uzerine yazmasin.
internal fun bildirimKimligi(kayitId: String): Int = kayitId.hashCode()

private val json = Json { ignoreUnknownKeys = true }

// Bildirimin govdesine dokunulunca uygulamanin acmasi gereken hatirlatma.
data class HatirlatmaIstegi(val kayitId: String, val motorId: String)

// MainActivity kullaniyor: niyet bildirimden geldiyse istegi cikariyor.
fun Intent.hatirlatmaIstegi(): HatirlatmaIstegi? {
    if (action != EYLEM_PANEL) return null
    val kayitId = getStringExtra(EK_KAYIT_ID) ?: return null
    val motorId = getStringExtra(EK_MOTOR_ID) ?: return null
    return HatirlatmaIstegi(kayitId, motorId)
}

// Alici Android'in urettigi bir sinif; Hilt'in yapici enjeksiyonu burada
// calismiyor. Ihtiyaci olan nesneleri uygulamanin Hilt kabindan bu kapiyla aliyor.
@EntryPoint
@InstallIn(SingletonComponent::class)
interface HatirlatmaGirisi {
    fun zamanlayici(): HatirlatmaZamanlayici
    fun depo(): HatirlatmaDeposu
}

// Alarm caldiginda ve bildirimdeki iki dugmeye ("Yaptirdim", "1 hafta ertele")
// basilinca Android bu sinifi uyandiriyor. Uygulama kapali olsa da calisiyor.
class HatirlatmaAlicisi : BroadcastReceiver() {

    override fun onReceive(context: Context, intent: Intent) {
        val giris = EntryPointAccessors.fromApplication(context, HatirlatmaGirisi::class.java)

        // Depo diskten okuyor, erteleme buluta yaziyor: ikisi de zaman alan isler.
        // goAsync alicinin omrunu is bitene kadar uzatiyor (en fazla ~10 sn).
        val bekleyen = goAsync()
        CoroutineScope(SupervisorJob() + Dispatchers.IO).launch {
            try {
                when (intent.action) {
                    // Iki dugme de uygulamayi acmiyor: bildirim kapaniyor, is arkada bitiyor.
                    EYLEM_ERTELE, EYLEM_YAPTIRDIM -> {
                        val kayit = intent.getStringExtra(EK_HATIRLATMA)
                            ?.let { runCatching { json.decodeFromString(HatirlatmaKaydi.serializer(), it) }.getOrNull() }
                            ?: return@launch
                        NotificationManagerCompat.from(context).cancel(bildirimKimligi(kayit.kayitId))
                        if (intent.action == EYLEM_ERTELE) giris.zamanlayici().ertele(kayit)
                        else giris.zamanlayici().yapildiIsaretle(kayit)
                    }

                    else -> {
                        val kayitId = intent.getStringExtra(EK_KAYIT_ID) ?: return@launch
                        val kayit = giris.depo().bul(kayitId) ?: return@launch
                        // Caldi: telefondaki listeden cikiyor. Ertelenirse yeniden giriyor.
                        giris.depo().sil(kayitId)
                        bildirimGoster(context, kayit)
                    }
                }
            } finally {
                bekleyen.finish()
            }
        }
    }
}

// notify() Android 13'ten beri POST_NOTIFICATIONS istiyor. Surum ve izin
// kontrolu var ama lint bileske kosulu goremiyor, o yuzden bastiriliyor.
@SuppressLint("MissingPermission")
private fun bildirimGoster(context: Context, kayit: HatirlatmaKaydi) {
    val izinVar = Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU ||
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
    if (!izinVar) return

    val baslik = kayit.bakimTuru.ifBlank { "Bakım" } + " zamanı"
    // "Honda CB500F · 12 Mart'taki bakımda bunu kurmuştun."
    val metin = "${kayit.motorAdi} · ${tarihliBakim(kayit.bakimTarihi)} bunu kurmuştun."
    val kayitJson = json.encodeToString(HatirlatmaKaydi.serializer(), kayit)

    val bildirim = NotificationCompat.Builder(context, BILDIRIM_KANALI)
        .setSmallIcon(R.drawable.ic_hatirlatma)
        .setContentTitle(baslik)
        .setContentText(metin)
        // Uzun motor adinda metin kesilmesin: acilinca tamami gorunuyor.
        .setStyle(NotificationCompat.BigTextStyle().bigText(metin))
        .setPriority(NotificationCompat.PRIORITY_DEFAULT)
        .setAutoCancel(true)
        .setContentIntent(paneliAcanNiyet(context, kayit))
        .addAction(0, "Yaptırdım", yaptirdimNiyeti(context, kayit.kayitId, kayitJson))
        .addAction(0, "1 hafta ertele", erteleNiyeti(context, kayit.kayitId, kayitJson))
        .build()

    NotificationManagerCompat.from(context).notify(bildirimKimligi(kayit.kayitId), bildirim)
}

// Bildirimin govdesine dokununca uygulama aciliyor ve o kaydin hatirlatma
// paneli geliyor. SINGLE_TOP + CLEAR_TOP: uygulama aciksa yeniden
// kurulmuyor, niyet onNewIntent'e dusuyor.
private fun paneliAcanNiyet(context: Context, kayit: HatirlatmaKaydi): PendingIntent =
    PendingIntent.getActivity(
        context,
        0,
        Intent(context, MainActivity::class.java)
            .setAction(EYLEM_PANEL)
            .setData((HATIRLATMA_ADRESI + kayit.kayitId).toUri())
            .putExtra(EK_KAYIT_ID, kayit.kayitId)
            .putExtra(EK_MOTOR_ID, kayit.motorId)
            .addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

// Iki dugme de ayni aliciya gidiyor; ne istendigini eylem soyluyor. Eylemler
// farkli oldugu icin ikisi ayri PendingIntent sayiliyor.
private fun yaptirdimNiyeti(context: Context, kayitId: String, kayitJson: String): PendingIntent =
    dugmeNiyeti(context, EYLEM_YAPTIRDIM, kayitId, kayitJson)

private fun erteleNiyeti(context: Context, kayitId: String, kayitJson: String): PendingIntent =
    dugmeNiyeti(context, EYLEM_ERTELE, kayitId, kayitJson)

private fun dugmeNiyeti(context: Context, eylem: String, kayitId: String, kayitJson: String): PendingIntent =
    PendingIntent.getBroadcast(
        context,
        0,
        Intent(context, HatirlatmaAlicisi::class.java)
            .setAction(eylem)
            .setData((HATIRLATMA_ADRESI + kayitId).toUri())
            .putExtra(EK_HATIRLATMA, kayitJson),
        PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
    )

// "12 Mart'taki bakımda". Turkcede ek ayin son sesine gore degisiyor
// (Mart'taki, Haziran'daki, Eylul'deki); ay adlari sabit oldugu icin tablo.
// Baska yildaysa yil sayisina ek bulmak yerine "tarihindeki" diyoruz.
private fun tarihliBakim(millis: Long): String {
    val takvim = Calendar.getInstance().apply { timeInMillis = millis }
    val gun = takvim.get(Calendar.DAY_OF_MONTH)
    val (ay, ek) = AYLAR[takvim.get(Calendar.MONTH)]
    val buYil = Calendar.getInstance().get(Calendar.YEAR)
    return if (takvim.get(Calendar.YEAR) == buYil) {
        "$gun $ay'$ek bakımda"
    } else {
        "$gun $ay ${takvim.get(Calendar.YEAR)} tarihindeki bakımda"
    }
}

private val AYLAR = listOf(
    "Ocak" to "taki", "Şubat" to "taki", "Mart" to "taki", "Nisan" to "daki",
    "Mayıs" to "taki", "Haziran" to "daki", "Temmuz" to "daki", "Ağustos" to "taki",
    "Eylül" to "deki", "Ekim" to "deki", "Kasım" to "daki", "Aralık" to "taki"
)

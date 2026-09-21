package com.oguzhanp.motorum.data

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import com.oguzhanp.motorum.model.Kayit
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton

// Iki alarmi birbirinden ayiran adres. Kayit kimligi buraya giriyor.
internal const val HATIRLATMA_ADRESI = "motorum://hatirlatma/"

private const val BIR_HAFTA_MS = 7L * 24 * 60 * 60 * 1000

// Telefon kapaliyken zamani gecen hatirlatma, acilista hemen degil birkac
// saniye sonra caliyor: sistem acilisi bitsin, bildirim kaybolmasin.
private const val GECIKEN_ICIN_BEKLEME_MS = 10_000L

// Alarmlarin tek sahibi. Uc kaynaktan besleniyor:
// - Kayit eklenip duzenlenince (esitle / iptal),
// - Telefon acilinca (yenidenKur): Android alarmlari yeniden baslatmada siliyor,
// - Uygulama acilip kayitlar gelince (buluttanEsitle): yeniden yukleme ya da
//   yeni telefonda buluttaki hatirlatmalar geri kuruluyor.
@Singleton
class HatirlatmaZamanlayici @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val depo: HatirlatmaDeposu,
    private val motorDeposu: MotorDeposu,
    private val kayitDeposu: KayitDeposu
) {

    private val alarmYoneticisi = context.getSystemService(AlarmManager::class.java)

    // Kaydin son halini alarm tarafina yansitiyor. Once varsa iptal, sonra
    // hatirlatma varsa yeniden kur. Tek yol izliyoruz cunku guncellemede
    // hatirlatma degismis, eklenmis ya da tamamen kaldirilmis olabilir.
    // Kayit her zaman secili motora yaziliyor; motor adi da oradan.
    suspend fun esitle(kayit: Kayit) {
        iptal(kayit.id)

        val bakim = kayit as? Kayit.Bakim ?: return
        val zaman = bakim.hatirlatmaMillis ?: return
        if (zaman <= System.currentTimeMillis() || bakim.hatirlatmaYapildiMillis != null) return
        val motor = motorDeposu.seciliMotor() ?: return
        kur(bakim.hatirlatmaKaydi(zaman, motor.id, motor.adi))
    }

    suspend fun iptal(kayitId: String) {
        alarmiKaldir(kayitId)
        depo.sil(kayitId)
    }

    // "1 hafta ertele". Once telefondaki alarm, sonra bulut: internet yoksa
    // da ertelenmis olsun, bulut bir sonraki acilista yetissin.
    suspend fun ertele(kayit: HatirlatmaKaydi) {
        val yeni = kayit.copy(zaman = System.currentTimeMillis() + BIR_HAFTA_MS, buluttaGuncellenecek = true)
        kur(yeni)
        if (kayitDeposu.hatirlatmayiGuncelle(yeni.motorId, yeni.kayitId, yeni.zaman) == null) {
            depo.yaz(yeni.copy(buluttaGuncellenecek = false))
        }
    }

    // Bildirimdeki "Yaptirdim": uygulama acilmadan kayit isaretleniyor.
    // Internet yoksa telefonda bekliyor, bir sonraki acilista buluta yaziliyor.
    suspend fun yapildiIsaretle(kayit: HatirlatmaKaydi) {
        alarmiKaldir(kayit.kayitId)
        val hata = kayitDeposu.hatirlatmaYapildiYaz(kayit.motorId, kayit.kayitId, System.currentTimeMillis())
        if (hata == null) depo.sil(kayit.kayitId) else depo.yaz(kayit.copy(yapildiBekliyor = true))
    }

    // Telefon yeniden baslayinca (ya da uygulama guncellenince) cagriliyor.
    // Kapaliyken zamani gecenler kaybolmuyor: birkac saniye icinde caliyor.
    suspend fun yenidenKur() {
        val simdi = System.currentTimeMillis()
        depo.hepsi().filterNot { it.yapildiBekliyor }.forEach { kayit ->
            alarmKur(kayit.kayitId, maxOf(kayit.zaman, simdi + GECIKEN_ICIN_BEKLEME_MS))
        }
    }

    // Secili motorun kayitlari geldikten sonra. Bulut asil kaynak; telefondaki
    // liste ona uyduruluyor. Yeniden yukleme ya da yeni telefonda hatirlatmalar
    // boylece geri geliyor, baska cihazda kapatilanlar da burada kapaniyor.
    suspend fun buluttanEsitle(kayitlar: List<Kayit>) {
        val motorId = motorDeposu.seciliMotorId() ?: return
        val simdi = System.currentTimeMillis()
        val bakimlar = kayitlar.filterIsInstance<Kayit.Bakim>().associateBy { it.id }
        val yereldekiler = depo.hepsi().filter { it.motorId == motorId }

        yereldekiler.forEach { yerel ->
            val bulutta = bakimlar[yerel.kayitId]
            when {
                // Kayit silinmis: hatirlatmasi da gitsin.
                bulutta == null -> iptal(yerel.kayitId)

                // Internetsiz "Yaptirdim" denmisti: simdi buluta yaz.
                yerel.yapildiBekliyor -> {
                    if (kayitDeposu.hatirlatmaYapildiYaz(motorId, yerel.kayitId, simdi) == null) {
                        depo.sil(yerel.kayitId)
                    }
                }

                // Baska yerden (uygulama ici panel, baska cihaz) yapildi denmis.
                bulutta.hatirlatmaYapildiMillis != null -> iptal(yerel.kayitId)

                // Internetsiz ertelenmisti: simdi buluta yaz.
                yerel.buluttaGuncellenecek -> {
                    if (kayitDeposu.hatirlatmayiGuncelle(motorId, yerel.kayitId, yerel.zaman) == null) {
                        depo.yaz(yerel.copy(buluttaGuncellenecek = false))
                    }
                }

                // Hatirlatma baska yerde kapatilmis ya da tarihi degismis.
                // Degistiyse asagida yeni tarihle yeniden kuruluyor. Ayni ise
                // dokunmuyoruz: zamani gecmis ama henuz calmamis olsa bile calsin.
                bulutta.hatirlatmaMillis != yerel.zaman -> iptal(yerel.kayitId)
            }
        }

        // Bulutta olup telefonda olmayan gelecek hatirlatmalar. Motor adi icin
        // sunucuya ancak gerekirse gidiyoruz.
        val kalan = depo.hepsi().map { it.kayitId }.toSet()
        val eksikler = bakimlar.values.filter { bakim ->
            (bakim.hatirlatmaMillis ?: 0L) > simdi &&
                    bakim.hatirlatmaYapildiMillis == null &&
                    bakim.id !in kalan
        }
        if (eksikler.isEmpty()) return
        val motorAdi = motorDeposu.seciliMotor()?.adi ?: return
        eksikler.forEach { bakim ->
            kur(bakim.hatirlatmaKaydi(bakim.hatirlatmaMillis!!, motorId, motorAdi))
        }
    }

    private suspend fun kur(kayit: HatirlatmaKaydi) {
        depo.yaz(kayit)
        alarmKur(kayit.kayitId, kayit.zaman)
    }

    // setAndAllowWhileIdle: kesin olmayan alarm, izin istemiyor. Telefon uyku
    // modundayken de yaklasik zamaninda caliyor. setWindow kullansaydik uyku
    // modunda bir sonraki bakim penceresine ertelenirdi.
    private fun alarmKur(kayitId: String, zamanMillis: Long) {
        val yonetici = alarmYoneticisi ?: return
        val niyet = bekleyenNiyet(kayitId, PendingIntent.FLAG_UPDATE_CURRENT) ?: return
        yonetici.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, zamanMillis, niyet)
    }

    private fun alarmiKaldir(kayitId: String) {
        // Ekranda duran bildirim de gitsin: kaydi silinen bakim icin "Yaptirdim"
        // dugmesi kalmasin.
        NotificationManagerCompat.from(context).cancel(bildirimKimligi(kayitId))

        val yonetici = alarmYoneticisi ?: return
        // FLAG_NO_CREATE: yoksa yenisini uretme, null don. Kurulu alarm yoksa
        // iptal edecek bir sey de yok.
        val niyet = bekleyenNiyet(kayitId, PendingIntent.FLAG_NO_CREATE) ?: return
        yonetici.cancel(niyet)
        niyet.cancel()
    }

    // Alarmlari birbirinden ayiran sey Intent'in data alani; extras DEGIL.
    // PendingIntent esitligi Intent.filterEquals ile olculuyor ve o fonksiyon
    // extras'a hic bakmiyor. Bildirimin icerigi niyette degil, depoda duruyor:
    // alarm caldiginda en guncel hali okunuyor.
    private fun bekleyenNiyet(kayitId: String, bayrak: Int): PendingIntent? {
        val niyet = Intent(context, HatirlatmaAlicisi::class.java).apply {
            action = EYLEM_CAL
            data = (HATIRLATMA_ADRESI + kayitId).toUri()
            putExtra(EK_KAYIT_ID, kayitId)
        }
        return PendingIntent.getBroadcast(context, 0, niyet, bayrak or PendingIntent.FLAG_IMMUTABLE)
    }
}

private fun Kayit.Bakim.hatirlatmaKaydi(zaman: Long, motorId: String, motorAdi: String) =
    HatirlatmaKaydi(
        kayitId = id,
        motorId = motorId,
        motorAdi = motorAdi,
        bakimTuru = bakimTuru,
        bakimTarihi = tarihMillis,
        zaman = zaman
    )

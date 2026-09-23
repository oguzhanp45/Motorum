package com.oguzhanp.motorum.data.motor

import android.content.Context
import android.graphics.Bitmap
import android.graphics.ImageDecoder
import android.net.Uri
import android.util.Base64
import androidx.core.content.FileProvider
import androidx.core.graphics.scale
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID
import javax.inject.Inject
import javax.inject.Singleton

// Tek yerden degistirilecek olculer. Tam boy telefon ekranini kaplayacak
// kadar, onizleme ise kart ve panel karelerine yetecek kadar.
private const val TAM_BOY_KENAR = 1280
private const val ONIZLEME_KENAR = 256
private const val ONIZLEME_KALITESI = 70

// Gurultulu bir fotograf %85'te beklenenden buyuk cikabilir. Sirayla deneyip
// sinira sigani aliyoruz; hicbiri sigmazsa vazgeciyoruz.
private val TAM_BOY_KALITELERI = listOf(85, 70, 55)

// Firestore belge siniri 1 MiB. Fotograf disindaki alanlara da yer birakmak
// icin kendimize bundan cok daha dar bir tavan koyuyoruz.
private const val EN_BUYUK_BAYT = 600 * 1024

// AndroidManifest'teki authorities ile ayni olmak zorunda.
private const val SAGLAYICI_EKI = ".fileprovider"
private const val GECICI_KLASOR = "fotograf"

data class HazirGorsel(val tamBoy: String, val onizleme: String)

@Singleton
class GorselHazirlayici @Inject constructor(
    @param:ApplicationContext private val context: Context
) {

    // Uri'den iki base64 metin uretiyor. Basarisizlikta null: elimizde
    // kullaniciya anlatilacak bir ayrinti yok, "fotograf okunamadi" yeter.
    suspend fun hazirla(uri: Uri): HazirGorsel? = withContext(Dispatchers.IO) {
        try {
            val tamBoy = coz(uri, TAM_BOY_KENAR)
            val onizleme = kucult(tamBoy, ONIZLEME_KENAR)

            val tamBoyMetin = metneCevir(tamBoy, TAM_BOY_KALITELERI)
                ?: return@withContext null
            val onizlemeMetin = metneCevir(onizleme, listOf(ONIZLEME_KALITESI))
                ?: return@withContext null

            HazirGorsel(tamBoy = tamBoyMetin, onizleme = onizlemeMetin)
        } catch (hata: Exception) {
            null
        }
    }

    // Kameranin fotografi yazacagi bos dosyayi hazirliyor. Onbellek altinda
    // duruyor: sistem gerektiginde kendisi de temizleyebilir.
    fun kameraHedefi(): Uri? = try {
        val klasor = File(context.cacheDir, GECICI_KLASOR).apply { mkdirs() }
        val dosya = File(klasor, "${UUID.randomUUID()}.jpg")
        FileProvider.getUriForFile(context, context.packageName + SAGLAYICI_EKI, dosya)
    } catch (hata: Exception) {
        null
    }

    // Kameradan cikan ham dosya birkac MB. Kucultup base64'e cevirdikten sonra
    // isimiz bitiyor, onbellekte oylece durmasin.
    fun geciciSil(uri: Uri) {
        try {
            context.contentResolver.delete(uri, null, null)
        } catch (hata: Exception) {
            // Silinemezse yapacak bir sey yok, onbellegi sistem zaten temizliyor.
        }
    }

    // ImageDecoder gorseli zaten kucultulmus olarak aciyor: 12 MP fotografi
    // once bellege alip sonra kucultmuyoruz. EXIF donmesini de kendisi
    // duzeltiyor, yan yatmis kamera fotograflari dik geliyor.
    private fun coz(uri: Uri, hedefKenar: Int): Bitmap {
        val kaynak = ImageDecoder.createSource(context.contentResolver, uri)
        return ImageDecoder.decodeBitmap(kaynak) { kodCozucu, bilgi, _ ->
            // Donanim bitmap'i GPU bellegindedir ve compress edilemez.
            kodCozucu.allocator = ImageDecoder.ALLOCATOR_SOFTWARE

            val genislik = bilgi.size.width
            val yukseklik = bilgi.size.height
            val uzunKenar = maxOf(genislik, yukseklik)

            // Zaten kucuk olani buyutmuyoruz: yoktan cozunurluk uretilmiyor,
            // sadece dosya sismis olurdu.
            if (uzunKenar > hedefKenar) {
                val oran = hedefKenar.toFloat() / uzunKenar
                kodCozucu.setTargetSize(
                    (genislik * oran).toInt().coerceAtLeast(1),
                    (yukseklik * oran).toInt().coerceAtLeast(1)
                )
            }
        }
    }

    // Onizleme icin gorseli ikinci kez cozmek yerine tam boyu kucultuyoruz:
    // dosyayi bir kez okumus oluyoruz.
    private fun kucult(kaynak: Bitmap, hedefKenar: Int): Bitmap {
        val uzunKenar = maxOf(kaynak.width, kaynak.height)
        if (uzunKenar <= hedefKenar) return kaynak

        val oran = hedefKenar.toFloat() / uzunKenar
        return kaynak.scale(
            (kaynak.width * oran).toInt().coerceAtLeast(1),
            (kaynak.height * oran).toInt().coerceAtLeast(1)
        )
    }

    // NO_WRAP sart: varsayilan Base64 satir sonu ekliyor ve metni bozuyor.
    private fun metneCevir(bitmap: Bitmap, kaliteler: List<Int>): String? {
        kaliteler.forEach { kalite ->
            val cikti = ByteArrayOutputStream()
            bitmap.compress(Bitmap.CompressFormat.JPEG, kalite, cikti)
            val metin = Base64.encodeToString(cikti.toByteArray(), Base64.NO_WRAP)
            if (metin.length <= EN_BUYUK_BAYT) return metin
        }
        return null
    }
}

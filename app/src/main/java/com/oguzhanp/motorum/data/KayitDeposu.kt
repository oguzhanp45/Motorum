package com.oguzhanp.motorum.data

import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.CollectionReference
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source
import com.oguzhanp.motorum.model.Kayit
import kotlinx.coroutines.tasks.await
import javax.inject.Inject
import javax.inject.Singleton

data class KayitSonucu(
    val kayitlar: List<Kayit> = emptyList(),
    val hata: String? = null,
    // "Motoru yok" bir hata degil, anlatilacak bir durum: ekran bunu gorunce
    // hata mesaji yerine "once bir motor ekle" diyor.
    val motorYok: Boolean = false
)

// @Singleton: uygulamada tek ornek. Onceden her ViewModel kendi deposunu
// uretiyordu, yani onbellek de uc kez ayri tutuluyordu.
@Singleton
class KayitDeposu @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    private val agDurumu: AgDurumu,
    private val motorDeposu: MotorDeposu
) {

    // users/{uid}/motorlar/{motorId}/kayitlar
    // Motor kimligini artik bu depo cozmuyor, MotorDeposu'na soruyor: her deponun
    // tek bir konusu olsun diye. uid her cagrida auth'tan taze okunuyor.
    private fun kayitlarKoleksiyonu(uid: String, motorId: String): CollectionReference =
        firestore
            .collection(KOLEKSIYON_KULLANICILAR).document(uid)
            .collection(KOLEKSIYON_MOTORLAR).document(motorId)
            .collection(KOLEKSIYON_KAYITLAR)

    // Source.SERVER sart: varsayilan get() sunucuya ulasamayinca onbellege dusuyor
    // ve bunu HATA olarak degil, gecerli bir sonuc olarak donduruyor. Onbellek bos
    // oldugu icin de "basarili ama bos liste" gibi gorunuyordu. SERVER derken
    // "onbellegi istemiyorum" demis oluyoruz: ulasilamazsa hata firlatiyor.
    suspend fun kayitlariGetir(): KayitSonucu {
        // Okumadan once de ag kontrolu. Source.SERVER cevrimdisiyken hatayi
        // ana is parcaciginda doguruyor; asagidaki try/catch onu goremiyor ve
        // uygulama cokuyor. Tek korunma yolu oraya hic gitmemek.
        if (!agDurumu.internetVar()) return KayitSonucu(hata = INTERNET_YOK)

        return try {
            val uid = auth.currentUser?.uid ?: return KayitSonucu(hata = OTURUM_YOK)
            val motorId = motorDeposu.seciliMotorId() ?: return KayitSonucu(motorYok = true)
            val anlik = kayitlarKoleksiyonu(uid, motorId).get(Source.SERVER).await()
            KayitSonucu(
                // Cevrilemeyen belge eleniyor: tek bozuk kayit yuzunden liste comesin.
                kayitlar = anlik.documents.mapNotNull { belge ->
                    belge.toObject(KayitBelgesi::class.java)?.kayidaCevir()
                }
            )
        } catch (hata: Exception) {
            KayitSonucu(hata = hataMesaji(hata))
        }
    }

    // Yazmadan once ag kontrolu sart: Firestore cevrimdisiyken hata FIRLATMIYOR,
    // yazmayi kuyruga alip susuyor. await orada askida kalirdi ve try/catch'in
    // yakalayacagi bir sey olmazdi. Sessizligi cevaba ceviren sey bu kontrol.
    suspend fun kaydet(kayit: Kayit): String? {
        if (!agDurumu.internetVar()) return INTERNET_YOK
        return try {
            val uid = auth.currentUser?.uid ?: return OTURUM_YOK
            val motorId = motorDeposu.seciliMotorId() ?: return "Önce bir motor eklemelisin"
            // set ayni id'ye yazinca belgeyi bastan yaziyor: ekleme ve duzenleme ayni fonksiyon.
            kayitlarKoleksiyonu(uid, motorId).document(kayit.id).set(kayit.belgeyeCevir()).await()
            null
        } catch (hata: Exception) {
            hataMesaji(hata)
        }
    }

    suspend fun sil(id: String): String? {
        if (!agDurumu.internetVar()) return INTERNET_YOK
        return try {
            val uid = auth.currentUser?.uid ?: return OTURUM_YOK
            val motorId = motorDeposu.seciliMotorId() ?: return OTURUM_YOK
            kayitlarKoleksiyonu(uid, motorId).document(id).delete().await()
            null
        } catch (hata: Exception) {
            hataMesaji(hata)
        }
    }
}

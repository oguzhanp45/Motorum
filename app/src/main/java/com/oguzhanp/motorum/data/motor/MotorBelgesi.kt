package com.oguzhanp.motorum.data.motor

import com.google.firebase.firestore.DocumentId
import com.oguzhanp.motorum.model.Motor

// Motor duz bir data class, Firestore onu dogrudan da cevirebilirdi. Yine de DTO
// yaziyoruz: @DocumentId bir Firestore ek aciklamasi ve model/ katmani Firestore'u
// tanimamali. Kurali tek bir yerde bozmak, sonradan "burada niye farkli" sorusunu
// doguruyor.
data class MotorBelgesi(
    @DocumentId val id: String = "",
    val marka: String = "",
    val model: String = "",
    val plaka: String = "",
    // Eski motor belgelerinde bu alan yok. Varsayilan deger sayesinde Firestore
    // onlari da cevirebiliyor, bos onizleme "fotograf yok" demek oluyor.
    val onizleme: String = "",
    val olusturmaMillis: Long = 0L
)

fun Motor.belgeyeCevir(): MotorBelgesi = MotorBelgesi(
    marka = marka,
    model = model,
    plaka = plaka,
    onizleme = onizleme,
    olusturmaMillis = olusturmaMillis
)

fun MotorBelgesi.motoraCevir(): Motor = Motor(
    id = id,
    marka = marka,
    model = model,
    plaka = plaka,
    onizleme = onizleme,
    olusturmaMillis = olusturmaMillis
)

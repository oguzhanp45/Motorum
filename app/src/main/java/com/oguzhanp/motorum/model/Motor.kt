package com.oguzhanp.motorum.model

import java.util.UUID

// Marka ve model modelde zorunlu degil: mevcut otomatik motorun ikisi de bos ve
// onu "bilgisiz motor" olarak gostermeye devam edecegiz. Zorunluluk formda.
data class Motor(
    val id: String = UUID.randomUUID().toString(),
    val marka: String = "",
    val model: String = "",
    val plaka: String = "",
    val olusturmaMillis: Long = System.currentTimeMillis()
) {

    val bilgisiz: Boolean get() = marka.isBlank() && model.isBlank()

    val adi: String get() = if (bilgisiz) "Bilgisiz motor" else "$marka $model".trim()
}

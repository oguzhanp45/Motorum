package com.oguzhanp.motorum.core.tasarim

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp

// Kose yaricaplari. Uc kademe var ve her birinin bir anlami var: buyuk kose
// "bu bir yuzey", orta kose "bu bir alan", tam yuvarlak "bu bir etiket".
// Ayni ekranda dort farkli yaricap gorunce goz duzensizlik olarak okuyor.
object AppShape {
    // Kartlar, sayfa uzerindeki yuzeyler.
    val kart = RoundedCornerShape(16.dp)

    // Hava seridi: karttan bir tik kucuk kose. Serit alcak ve genis; ayni
    // 16'lik kose onu gereginden yuvarlak, "hap" gibi gosteriyordu.
    val serit = RoundedCornerShape(14.dp)

    // Bos durum ekranlarindaki buyuk eylem dugmeleri. Alanlardan bir tik
    // yuvarlak: dugme sayfada tek basina duruyor, yumusak kose onu davetkar
    // gosteriyor.
    val buton = RoundedCornerShape(14.dp)

    // Form alanlari, segment seridi gibi ic bilesenler.
    val alan = RoundedCornerShape(12.dp)

    // Segment seridinin icindeki secili parca: disindaki 12'den bir tik kucuk
    // olmali, yoksa dolgunun kosesi seridin kosesinden tasiyor gibi duruyor.
    val alanIci = RoundedCornerShape(9.dp)

    // Cipler ve rozetler: yuksekligi ne olursa olsun tam yuvarlak uc.
    val cip = RoundedCornerShape(999.dp)
}

// Yukseklik (golge) tek kademe: kartlar sayfadan bir tik ayrilsin yeter.
// Material'in varsayilan kademeleri bizim duz tasarimimiza gore agir kaliyor.
object AppElevation {
    val kart = 1.dp
}

package com.oguzhanp.motorum.feature.kimlik

import androidx.compose.animation.animateColorAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.interaction.collectIsFocusedAsState
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.offset
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.ripple
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.geometry.CornerRadius
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.oguzhanp.motorum.R
import com.oguzhanp.motorum.core.tasarim.DUGME_BASILI
import com.oguzhanp.motorum.core.tasarim.MotorumIkonlari
import com.oguzhanp.motorum.core.tasarim.basiliMurekkep
import com.oguzhanp.motorum.core.tasarim.basilincaKucul
import com.oguzhanp.motorum.core.tasarim.yukselerekGir
import com.oguzhanp.motorum.core.tasarim.CizgiSolgun
import com.oguzhanp.motorum.core.tasarim.DurumYesilMetin
import com.oguzhanp.motorum.core.tasarim.HataKirmizi
import com.oguzhanp.motorum.core.tasarim.HataMetin
import com.oguzhanp.motorum.core.tasarim.KartZemin
import com.oguzhanp.motorum.core.tasarim.MetinAna
import com.oguzhanp.motorum.core.tasarim.MetinEtiket
import com.oguzhanp.motorum.core.tasarim.MetinIkincil
import com.oguzhanp.motorum.core.tasarim.MetinSolgun
import com.oguzhanp.motorum.core.tasarim.MotorumTheme
import com.oguzhanp.motorum.core.tasarim.Murekkep
import com.oguzhanp.motorum.core.tasarim.MurekkepUstu
import com.oguzhanp.motorum.core.tasarim.RoadTripMetin
import com.oguzhanp.motorum.core.tasarim.RoadTripRenk
import com.oguzhanp.motorum.core.tasarim.SekmeZemin
import com.oguzhanp.motorum.core.tasarim.Zemin
import com.oguzhanp.motorum.core.tasarim.karanlikTema

private val ALAN_SEKLI = RoundedCornerShape(12.dp)
private val KART_SEKLI = RoundedCornerShape(22.dp)

// Giris ve uye ol ekranlarinin ortak iskeleti (tasarim: Giris, Uye Ol).
// Iki ekran ayni; farklari secili sekme, buton yazisi, "Sifremi unuttum"
// baglantisi (sadece giriste) ve sifre gostergesi (sadece uye olurken).
@Composable
fun KimlikEkrani(
    girisSecili: Boolean,
    form: KimlikFormu,
    yukleniyor: Boolean,
    hata: String?,
    onFormDegis: (KimlikFormu) -> Unit,
    onGonderTikla: () -> Unit,
    onDigerEkranaGec: () -> Unit,
    bilgi: String? = null,
    onSifremiUnuttum: (() -> Unit)? = null
) {
    // Klavye acilinca icerik kaydirilabiliyor; kapaliyken alt baglanti ekranin
    // dibinde duruyor (heightIn + bos Spacer). Kayit Ekle'deki kalip.
    BoxWithConstraints(
        Modifier
            .fillMaxSize()
            .background(Zemin)
            .imePadding()
    ) {
        Column(
            Modifier
                .verticalScroll(rememberScrollState())
                .heightIn(min = maxHeight)
                .navigationBarsPadding()
        ) {
            KimlikBasligi()

            KimlikKarti(
                girisSecili = girisSecili,
                form = form,
                yukleniyor = yukleniyor,
                hata = hata,
                bilgi = bilgi,
                onFormDegis = onFormDegis,
                onGonderTikla = onGonderTikla,
                onSekmeDegis = onDigerEkranaGec,
                onSifremiUnuttum = onSifremiUnuttum,
                // Kart koyu alanin ustune 22 dp biniyor.
                modifier = Modifier
                    .padding(horizontal = 20.dp)
                    .offset(y = (-22).dp)
            )

            Spacer(Modifier.weight(1f))

            KimlikAltBaglantisi(
                soru = stringResource(
                    if (girisSecili) R.string.hesabin_yok_mu else R.string.zaten_hesabin_var_mi
                ),
                baglanti = stringResource(if (girisSecili) R.string.uye_ol else R.string.giris_yap),
                onTikla = onDigerEkranaGec,
                modifier = Modifier.padding(top = 8.dp, bottom = 22.dp)
            )
        }
    }
}

@Composable
private fun KimlikKarti(
    girisSecili: Boolean,
    form: KimlikFormu,
    yukleniyor: Boolean,
    hata: String?,
    bilgi: String?,
    onFormDegis: (KimlikFormu) -> Unit,
    onGonderTikla: () -> Unit,
    onSekmeDegis: () -> Unit,
    onSifremiUnuttum: (() -> Unit)?,
    modifier: Modifier = Modifier
) {
    val karanlik = karanlikTema
    val golge = if (karanlik) Color.Black else Color(0xFF0F172A)
    Column(
        modifier = modifier
            .yukselerekGir(0)
            .fillMaxWidth()
            .shadow(14.dp, KART_SEKLI, ambientColor = golge, spotColor = golge)
            .background(KartZemin, KART_SEKLI)
            .padding(20.dp),
        verticalArrangement = Arrangement.spacedBy(16.dp)
    ) {
        KimlikSekmesi(
            girisSecili = girisSecili,
            onGirisTikla = onSekmeDegis,
            onUyeOlTikla = onSekmeDegis
        )

        Column(Modifier.yukselerekGir(1), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            AlanEtiketi(stringResource(R.string.eposta))
            KimlikAlani(
                deger = form.eposta,
                onDegis = { onFormDegis(form.copy(eposta = it, epostaHatali = false)) },
                ikon = MotorumIkonlari.Eposta,
                ipucu = stringResource(R.string.eposta_ipucu),
                hatali = form.epostaHatali,
                klavye = KeyboardOptions(
                    keyboardType = KeyboardType.Email,
                    imeAction = ImeAction.Next
                )
            )
            if (form.epostaHatali) AltNot(stringResource(R.string.eposta_hatali), HataMetin)
        }

        Column(Modifier.yukselerekGir(2), verticalArrangement = Arrangement.spacedBy(6.dp)) {
            AlanEtiketi(stringResource(R.string.sifre))
            KimlikAlani(
                deger = form.sifre,
                onDegis = { onFormDegis(form.copy(sifre = it, sifreHatali = false)) },
                ikon = MotorumIkonlari.Kilitli,
                ipucu = "••••••",
                hatali = form.sifreHatali,
                sifreGizli = !form.sifreGorunur,
                onGozTikla = { onFormDegis(form.copy(sifreGorunur = !form.sifreGorunur)) },
                klavye = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                // Klavyedeki "Bitti" tusu da formu gonderiyor.
                onBitti = onGonderTikla
            )
            if (girisSecili) {
                if (form.sifreHatali) {
                    AltNot(stringResource(R.string.sifre_kisa, EN_AZ_SIFRE), HataMetin)
                }
                if (onSifremiUnuttum != null) {
                    Text(
                        text = stringResource(R.string.sifremi_unuttum),
                        fontSize = 11.5.sp,
                        fontWeight = FontWeight.Bold,
                        color = Murekkep,
                        modifier = Modifier
                            .align(Alignment.End)
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(enabled = !yukleniyor, onClick = onSifremiUnuttum)
                            .padding(horizontal = 4.dp, vertical = 4.dp)
                    )
                }
            } else {
                SifreGostergesi(uzunluk = form.sifre.length, hatali = form.sifreHatali)
            }
        }

        // Sunucudan gelen hata ya da bilgi (sifre yenileme postasi gitti gibi).
        if (hata != null) AltNot(hata, HataMetin)
        if (bilgi != null) AltNot(bilgi, DurumYesilMetin)

        GonderDugmesi(
            metin = stringResource(if (girisSecili) R.string.giris_yap else R.string.uye_ol),
            yukleniyor = yukleniyor,
            onTikla = onGonderTikla,
            modifier = Modifier.yukselerekGir(3)
        )
    }
}

// Sekmeler: gri serit, secili olan murekkep renginde dolu.
@Composable
private fun KimlikSekmesi(
    girisSecili: Boolean,
    onGirisTikla: () -> Unit,
    onUyeOlTikla: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .background(CizgiSolgun, RoundedCornerShape(12.dp))
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        SekmeParcasi(stringResource(R.string.giris_yap), girisSecili, onGirisTikla, Modifier.weight(1f))
        SekmeParcasi(stringResource(R.string.uye_ol), !girisSecili, onUyeOlTikla, Modifier.weight(1f))
    }
}

@Composable
private fun SekmeParcasi(
    metin: String,
    secili: Boolean,
    onTikla: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sekil = RoundedCornerShape(9.dp)
    Box(
        modifier = modifier
            .then(
                if (secili) Modifier
                    .shadow(3.dp, sekil)
                    .background(Murekkep, sekil)
                else Modifier
            )
            .clip(sekil)
            .clickable(enabled = !secili, role = Role.Tab, onClick = onTikla)
            .padding(vertical = 9.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = metin,
            fontSize = 13.sp,
            fontWeight = if (secili) FontWeight.ExtraBold else FontWeight.SemiBold,
            color = if (secili) MurekkepUstu else MetinIkincil
        )
    }
}

// Tasarimdaki alan: ince cerceve, solda ikon. Odaklaninca cerceve murekkep
// rengine donuyor ve etrafinda 3 dp'lik soluk bir hale beliriyor.
// OutlinedTextField bu haleyi ve 1.5 dp cerceveyi desteklemiyor; o yuzden
// BasicTextField'in "decorationBox"u ile kendimiz ciziyoruz.
@Composable
private fun KimlikAlani(
    deger: String,
    onDegis: (String) -> Unit,
    ikon: ImageVector,
    ipucu: String,
    hatali: Boolean,
    klavye: KeyboardOptions,
    sifreGizli: Boolean = false,
    onGozTikla: (() -> Unit)? = null,
    onBitti: (() -> Unit)? = null
) {
    val etkilesim = remember { MutableInteractionSource() }
    val odakli by etkilesim.collectIsFocusedAsState()
    val karanlik = karanlikTema

    val cerceve by animateColorAsState(
        when {
            hatali -> HataKirmizi
            odakli -> Murekkep
            else -> SekmeZemin
        },
        label = "cerceve"
    )
    val hale = when {
        !odakli -> Color.Transparent
        hatali -> HataKirmizi.copy(alpha = 0.14f)
        karanlik -> Color.Black.copy(alpha = 0.22f)
        else -> Murekkep.copy(alpha = 0.10f)
    }
    val ikonRengi = if (odakli) Murekkep else MetinSolgun
    val sifreNoktali = onGozTikla != null && sifreGizli && deger.isNotEmpty()

    BasicTextField(
        value = deger,
        onValueChange = onDegis,
        singleLine = true,
        interactionSource = etkilesim,
        keyboardOptions = klavye,
        keyboardActions = KeyboardActions(onDone = { onBitti?.invoke() }),
        visualTransformation = if (onGozTikla != null && sifreGizli) PasswordVisualTransformation()
        else VisualTransformation.None,
        cursorBrush = SolidColor(Murekkep),
        textStyle = MaterialTheme.typography.bodyMedium.copy(
            color = MetinAna,
            // Gizli sifre noktalari maketteki gibi iri ve aralikli.
            fontSize = if (sifreNoktali) 16.sp else 14.sp,
            fontWeight = if (sifreNoktali) FontWeight.Bold else FontWeight.Medium,
            letterSpacing = if (sifreNoktali) 3.sp else 0.sp
        ),
        modifier = Modifier.fillMaxWidth(),
        decorationBox = { metinAlani ->
            Row(
                modifier = Modifier
                    // Hale cercevenin disina tasiyor: kutunun arkasina 3 dp buyuk ciziliyor.
                    .drawBehind {
                        val tasma = 3.dp.toPx()
                        drawRoundRect(
                            color = hale,
                            topLeft = Offset(-tasma, -tasma),
                            size = Size(size.width + tasma * 2, size.height + tasma * 2),
                            cornerRadius = CornerRadius(12.dp.toPx() + tasma)
                        )
                    }
                    .background(KartZemin, ALAN_SEKLI)
                    .border(1.5.dp, cerceve, ALAN_SEKLI)
                    .padding(horizontal = 13.dp)
                    .height(48.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(10.dp)
            ) {
                Icon(ikon, contentDescription = null, tint = ikonRengi, modifier = Modifier.size(18.dp))
                Box(Modifier.weight(1f)) {
                    if (deger.isEmpty()) {
                        Text(ipucu, fontSize = 14.sp, fontWeight = FontWeight.Medium, color = MetinSolgun)
                    }
                    metinAlani()
                }
                if (onGozTikla != null) {
                    Icon(
                        imageVector = if (sifreGizli) MotorumIkonlari.Goz else MotorumIkonlari.GozKapali,
                        contentDescription = stringResource(
                            if (sifreGizli) R.string.sifreyi_goster else R.string.sifreyi_gizle
                        ),
                        tint = MetinSolgun,
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .clickable(onClick = onGozTikla)
                            .padding(7.dp)
                            .size(18.dp)
                    )
                }
            }
        }
    )
}

// Uye olurken sifrenin altindaki uc parcali cubuk. Tek kural uzunluk:
// 6'dan kisa bir parca, 6-9 iki parca, 10 ve ustu uc parca. Olcut 6'yi
// gecince turkuaza donup "✓" aliyor; Firebase de en az 6 istiyor.
@Composable
private fun SifreGostergesi(uzunluk: Int, hatali: Boolean) {
    val yeterli = uzunluk >= EN_AZ_SIFRE
    val dolu = when {
        uzunluk == 0 -> 0
        !yeterli -> 1
        uzunluk < GUCLU_SIFRE -> 2
        else -> 3
    }
    val renk = if (yeterli) RoadTripRenk else MetinSolgun
    Row(
        modifier = Modifier.padding(start = 2.dp, end = 2.dp, top = 4.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(8.dp)
    ) {
        Row(Modifier.weight(1f), horizontalArrangement = Arrangement.spacedBy(3.dp)) {
            repeat(3) { i ->
                Box(
                    Modifier
                        .weight(1f)
                        .height(3.dp)
                        .background(if (i < dolu) renk else SekmeZemin, RoundedCornerShape(50))
                )
            }
        }
        Text(
            text = stringResource(
                if (yeterli) R.string.sifre_olcutu_tamam else R.string.sifre_olcutu,
                EN_AZ_SIFRE
            ),
            fontSize = 10.5.sp,
            fontWeight = FontWeight.Bold,
            color = when {
                hatali -> HataMetin
                yeterli -> RoadTripMetin
                else -> MetinSolgun
            }
        )
    }
}

private const val GUCLU_SIFRE = 10

@Composable
private fun GonderDugmesi(
    metin: String,
    yukleniyor: Boolean,
    onTikla: () -> Unit,
    modifier: Modifier = Modifier
) {
    val sekil = RoundedCornerShape(13.dp)
    val golge = if (karanlikTema) Color.Black else Murekkep
    // Basilinca kuculup bir ton koyulasiyor (tasarim: Dokunma).
    val etkilesim = remember { MutableInteractionSource() }
    Row(
        modifier = modifier
            .basilincaKucul(etkilesim, DUGME_BASILI)
            .fillMaxWidth()
            .shadow(8.dp, sekil, ambientColor = golge, spotColor = golge)
            .background(basiliMurekkep(etkilesim), sekil)
            .clip(sekil)
            .clickable(
                interactionSource = etkilesim,
                indication = ripple(),
                enabled = !yukleniyor,
                role = Role.Button,
                onClick = onTikla
            )
            .height(52.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp, Alignment.CenterHorizontally),
        verticalAlignment = Alignment.CenterVertically
    ) {
        if (yukleniyor) {
            CircularProgressIndicator(
                strokeWidth = 2.dp,
                color = MurekkepUstu,
                modifier = Modifier.size(20.dp)
            )
        } else {
            Text(metin, fontSize = 14.5.sp, fontWeight = FontWeight.Bold, color = MurekkepUstu)
            Icon(
                imageVector = MotorumIkonlari.Ileri,
                contentDescription = null,
                tint = MurekkepUstu,
                modifier = Modifier.size(17.dp)
            )
        }
    }
}

@Composable
private fun AlanEtiketi(metin: String) {
    Text(
        text = metin,
        fontSize = 11.sp,
        fontWeight = FontWeight.Bold,
        color = MetinEtiket,
        modifier = Modifier.padding(start = 2.dp)
    )
}

@Composable
private fun AltNot(metin: String, renk: Color) {
    Text(
        text = metin,
        fontSize = 11.5.sp,
        fontWeight = FontWeight.SemiBold,
        color = renk,
        modifier = Modifier.padding(start = 2.dp)
    )
}

@Composable
private fun KimlikAltBaglantisi(
    soru: String,
    baglanti: String,
    onTikla: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Center,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = soru,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.Medium,
            color = MetinIkincil
        )
        Text(
            text = baglanti,
            fontSize = 12.5.sp,
            fontWeight = FontWeight.ExtraBold,
            color = Murekkep,
            textAlign = TextAlign.Center,
            modifier = Modifier
                .clip(RoundedCornerShape(6.dp))
                .clickable(onClick = onTikla)
                .padding(horizontal = 5.dp, vertical = 8.dp)
        )
    }
}

// Hata ve yukleniyor hallerini ayri gormek gerekiyor: ikisi de gunluk
// kullanimda cikan ama elle test etmesi zahmetli durumlar.
@Preview(heightDp = 844, widthDp = 390)
@Composable
private fun KimlikEkraniHataliPreview() {
    MotorumTheme(karanlik = false) {
        KimlikEkrani(
            girisSecili = true,
            form = KimlikFormu(eposta = "bozuk", epostaHatali = true, sifreHatali = true),
            yukleniyor = false,
            hata = "E-posta ya da şifre hatalı",
            onFormDegis = {},
            onGonderTikla = {},
            onDigerEkranaGec = {},
            onSifremiUnuttum = {}
        )
    }
}

@Preview(heightDp = 844, widthDp = 390)
@Composable
private fun KimlikEkraniYukleniyorKaranlikPreview() {
    MotorumTheme(karanlik = true) {
        KimlikEkrani(
            girisSecili = false,
            form = KimlikFormu(eposta = "oguz@motorum.app", sifre = "1234567"),
            yukleniyor = true,
            hata = null,
            onFormDegis = {},
            onGonderTikla = {},
            onDigerEkranaGec = {}
        )
    }
}

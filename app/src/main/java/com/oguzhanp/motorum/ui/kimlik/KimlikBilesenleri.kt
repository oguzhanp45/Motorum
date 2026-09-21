package com.oguzhanp.motorum.ui.kimlik

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.oguzhanp.motorum.ui.theme.BaglantiMavi
import com.oguzhanp.motorum.ui.theme.KartZemin
import com.oguzhanp.motorum.ui.theme.MetinEtiket
import com.oguzhanp.motorum.ui.theme.MetinIkincil
import com.oguzhanp.motorum.ui.theme.MetinSolgun
import com.oguzhanp.motorum.ui.theme.MotorumTheme
import com.oguzhanp.motorum.ui.theme.SekmeZemin
import com.oguzhanp.motorum.ui.components.MotorumIkonlari

private val ALAN_SEKLI = RoundedCornerShape(12.dp)

@Composable
fun KimlikBasligi(modifier: Modifier = Modifier) {
    Column(
        modifier = modifier.fillMaxWidth(),
        verticalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Text(
            text = "Motorum",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )
        Text(
            text = "Motosiklet Masraf & Bakım Takibi",
            style = MaterialTheme.typography.bodyMedium,
            color = MetinIkincil
        )
    }
}

@Composable
fun KimlikSekmesi(
    girisSecili: Boolean,
    onGirisTikla: () -> Unit,
    onUyeOlTikla: () -> Unit,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .background(SekmeZemin)
            .padding(4.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        SekmeParcasi("Giriş Yap", girisSecili, onGirisTikla, Modifier.weight(1f))
        SekmeParcasi("Kayıt Ol", !girisSecili, onUyeOlTikla, Modifier.weight(1f))
    }
}

@Composable
private fun SekmeParcasi(
    metin: String,
    secili: Boolean,
    onTikla: () -> Unit,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .then(
                if (secili) Modifier
                    .shadow(1.dp, RoundedCornerShape(10.dp))
                    .background(KartZemin, RoundedCornerShape(10.dp))
                else Modifier
            )
            .clip(RoundedCornerShape(10.dp))
            .clickable(enabled = !secili, onClick = onTikla)
            .padding(vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = metin,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = if (secili) FontWeight.SemiBold else FontWeight.Normal,
            color = if (secili) MaterialTheme.colorScheme.onSurface else MetinIkincil
        )
    }
}

@Composable
fun KimlikKarti(
    baslik: String,
    butonMetni: String,
    form: KimlikFormu,
    yukleniyor: Boolean,
    hata: String?,
    onFormDegis: (KimlikFormu) -> Unit,
    onGonderTikla: () -> Unit,
    modifier: Modifier = Modifier
) {
    Card(
        modifier = modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = 1.dp)
    ) {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Text(
                    text = baslik,
                    style = MaterialTheme.typography.titleMedium
                )
                Text(
                    text = "Sadece e-posta ve şifrenizle devam edin",
                    style = MaterialTheme.typography.bodySmall,
                    color = MetinIkincil
                )
            }

            AlanEtiketi("E-posta")
            OutlinedTextField(
                value = form.eposta,
                onValueChange = { onFormDegis(form.copy(eposta = it, epostaHatali = false)) },
                modifier = Modifier.fillMaxWidth(),
                shape = ALAN_SEKLI,
                placeholder = { Text("ornek@motorum.com", color = MetinSolgun) },
                leadingIcon = {
                    Icon(MotorumIkonlari.Eposta, contentDescription = null, tint = MetinSolgun)
                },
                isError = form.epostaHatali,
                supportingText = {
                    if (form.epostaHatali) Text("Geçerli bir e-posta adresi girin")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email)
            )

            AlanEtiketi("Şifre")
            OutlinedTextField(
                value = form.sifre,
                onValueChange = { onFormDegis(form.copy(sifre = it, sifreHatali = false)) },
                modifier = Modifier.fillMaxWidth(),
                shape = ALAN_SEKLI,
                placeholder = { Text("••••••••", color = MetinSolgun) },
                leadingIcon = {
                    Icon(MotorumIkonlari.Kilitli, contentDescription = null, tint = MetinSolgun)
                },
                trailingIcon = {
                    IconButton(
                        onClick = { onFormDegis(form.copy(sifreGorunur = !form.sifreGorunur)) }
                    ) {
                        Icon(
                            imageVector = if (form.sifreGorunur) MotorumIkonlari.Goz
                            else MotorumIkonlari.GozKapali,
                            contentDescription = if (form.sifreGorunur) "Şifreyi gizle" else "Şifreyi göster",
                            tint = MetinSolgun
                        )
                    }
                },
                visualTransformation = if (form.sifreGorunur) VisualTransformation.None
                else PasswordVisualTransformation(),
                isError = form.sifreHatali,
                supportingText = {
                    if (form.sifreHatali) Text("Şifre en az $EN_AZ_SIFRE karakter olmalı")
                },
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password)
            )

            if (hata != null) {
                Text(
                    text = hata,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }

            Button(
                onClick = onGonderTikla,
                enabled = !yukleniyor,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = ALAN_SEKLI
            ) {
                if (yukleniyor) {
                    CircularProgressIndicator(
                        strokeWidth = 2.dp,
                        color = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(20.dp)
                    )
                } else {
                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        Text(butonMetni, fontWeight = FontWeight.SemiBold)
                        Icon(
                            imageVector = MotorumIkonlari.Ileri,
                            contentDescription = null,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun AlanEtiketi(metin: String) {
    Text(
        text = metin,
        style = MaterialTheme.typography.labelMedium,
        color = MetinEtiket
    )
}

@Composable
fun KimlikAltBaglantisi(
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
            style = MaterialTheme.typography.bodyMedium,
            color = MetinIkincil
        )
        Text(
            text = baglanti,
            style = MaterialTheme.typography.bodyMedium,
            fontWeight = FontWeight.Bold,
            color = BaglantiMavi,
            modifier = Modifier
                .clickable(onClick = onTikla)
                .padding(start = 6.dp, top = 4.dp, bottom = 4.dp)
        )
    }
}

// Dosyadaki parcalar tek tek degil, giris ekranindaki gercek dizilisleriyle
// gosteriliyor: aralarindaki bosluklar ve hizalama da boyle kontrol ediliyor.
@Preview(showBackground = true)
@Composable
private fun KimlikBilesenleriPreview() {
    MotorumTheme {
        Column(
            modifier = Modifier.padding(20.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            KimlikBasligi()
            KimlikSekmesi(
                girisSecili = true,
                onGirisTikla = {},
                onUyeOlTikla = {}
            )
            KimlikKarti(
                baslik = "Giriş Yap",
                butonMetni = "Giriş Yap",
                form = KimlikFormu(eposta = "ornek@eposta.com", sifre = "123456"),
                yukleniyor = false,
                hata = null,
                onFormDegis = {},
                onGonderTikla = {}
            )
            KimlikAltBaglantisi(
                soru = "Hesabın yok mu?",
                baglanti = "Üye ol",
                onTikla = {}
            )
        }
    }
}

// Hata ve yukleniyor hallerini ayri gormek gerekiyor: ikisi de gunluk
// kullanimda cikan ama elle test etmesi zahmetli durumlar.
@Preview(showBackground = true)
@Composable
private fun KimlikKartiHataliPreview() {
    MotorumTheme {
        KimlikKarti(
            baslik = "Giriş Yap",
            butonMetni = "Giriş Yap",
            form = KimlikFormu(eposta = "bozuk", epostaHatali = true, sifreHatali = true),
            yukleniyor = false,
            hata = "E-posta veya şifre hatalı",
            onFormDegis = {},
            onGonderTikla = {},
            modifier = Modifier.padding(20.dp)
        )
    }
}

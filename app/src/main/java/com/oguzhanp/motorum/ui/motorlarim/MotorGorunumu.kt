package com.oguzhanp.motorum.ui.motorlarim

import androidx.compose.ui.graphics.Color
import com.oguzhanp.motorum.ui.theme.MotorArduvaz
import com.oguzhanp.motorum.ui.theme.MotorArduvazZemin
import com.oguzhanp.motorum.ui.theme.MotorCamgobegi
import com.oguzhanp.motorum.ui.theme.MotorCamgobegiZemin
import com.oguzhanp.motorum.ui.theme.MotorGok
import com.oguzhanp.motorum.ui.theme.MotorGokZemin
import com.oguzhanp.motorum.ui.theme.MotorIndigo
import com.oguzhanp.motorum.ui.theme.MotorIndigoZemin
import com.oguzhanp.motorum.ui.theme.MotorLacivert
import com.oguzhanp.motorum.ui.theme.MotorLacivertZemin

data class MotorGorunumu(val renk: Color, val zemin: Color)

private val MOTOR_PALETI = listOf(
    MotorGorunumu(MotorIndigo, MotorIndigoZemin),
    MotorGorunumu(MotorGok, MotorGokZemin),
    MotorGorunumu(MotorLacivert, MotorLacivertZemin),
    MotorGorunumu(MotorArduvaz, MotorArduvazZemin),
    MotorGorunumu(MotorCamgobegi, MotorCamgobegiZemin)
)

// Renk motorun kimliginden turuyor, listedeki sirasindan degil: sirayla
// dagitsaydik bir motor silinince kalanlarin rengi kayardi.
// String.hashCode() Java'da tanimli ve sabit bir algoritma, yani ayni kimlik
// her cihazda ve her calistirmada ayni rengi veriyor.
// mod (rem degil) negatif hash'te de pozitif sonuc donuyor.
fun motorGorunumu(motorId: String): MotorGorunumu =
    MOTOR_PALETI[motorId.hashCode().mod(MOTOR_PALETI.size)]

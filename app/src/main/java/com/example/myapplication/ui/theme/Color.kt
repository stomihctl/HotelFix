package com.example.myapplication.ui.theme

import androidx.compose.ui.graphics.Color

val HotelNavy = Color(0xFF133B5E)
val HotelNavyTamna = Color(0xFF0D2A44)
val HotelKrem = Color(0xFFFBF5E9)
val HotelCrvena = Color(0xFFE43836)
val HotelSiva = Color(0xFFF2F0EC)

fun bojaStatusa(status: String): androidx.compose.ui.graphics.Color = when (status) {
    "Prijavljeno" -> Color(0xFF757575)
    "Dodeljeno" -> Color(0xFFE08A00)
    "Prihvaćeno" -> HotelNavy
    "U toku" -> Color(0xFFE08A00)
    "Završeno" -> Color(0xFF2E7D32)
    "Zatvoreno" -> Color(0xFF5C6B73)
    else -> Color.Gray
}
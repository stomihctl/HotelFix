package com.example.myapplication.model

import androidx.compose.ui.graphics.Color

data class Kvar(
    val id: String = "",
    val tip: String = "",
    val opis: String = "",
    val soba: String = "",
    val hitno: Boolean = false,
    val status: String = "Prijavljeno",
    val dodeljenMajstoru: String = "",
    val slikaBase64: String = "",
    val napomenaZaMajstora: String = "",
    val poslednjeOdbijanje: String = "",
    val vremePrijave: Long = System.currentTimeMillis()
){
    companion object {
        const val NOSLIKE = "NO_IMAGE"
    }
}

enum class Uloga { GOST, MENADZER, MAJSTOR }

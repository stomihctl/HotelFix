package com.example.myapplication.model

data class Poruka(
    val id: String = "",
    val posiljalac: String = "", // "MENADZER" ili "GOST"
    val tekst: String = "",
    val vreme: Long = System.currentTimeMillis()
)
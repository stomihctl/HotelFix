package com.example.myapplication.model

data class Korisnik(
    val ime: String = "",
    val email: String = "",
    val uloga: String = "", // "MENADZER" ili "MAJSTOR"
    val specijalizacija: String = "" // npr. "Električar", "Vodoinstalater", "Housekeeping / Opšte"
)
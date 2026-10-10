package com.example.myapplication.model

data class Majstor(
    val ime: String,
    val specijalizacija: String
)

val hardkodovaniMajstori = listOf(
    Majstor("Marko Petrović", "Električar"),
    Majstor("Ivan Jovanović", "Vodoinstalater"),
    Majstor("Ana Ilić", "Housekeeping / Opšte")
)
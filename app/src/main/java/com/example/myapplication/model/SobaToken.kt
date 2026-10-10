package com.example.myapplication.model

data class SobaToken(
    val token: String = "",
    val broj: String = "",
    val aktivan: Boolean = true,
    val vremeGenerisanja: Long = System.currentTimeMillis()
)
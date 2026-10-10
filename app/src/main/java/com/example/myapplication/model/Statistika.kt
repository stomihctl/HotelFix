package com.example.myapplication.model

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

data class Statistika(
    val ukupnoKvarova: Int = 0,
    val poTipu: Map<String, Int> = emptyMap(),
    val poMajstoru: Map<String, Int> = emptyMap(),
    val poDanu: List<Pair<String, Int>> = emptyList(),
    val zavrseno: Int = 0,
    val hitnih: Int = 0
)

fun izracunajStatistiku(kvarovi: List<Kvar>): Statistika {
    val format = SimpleDateFormat("dd.MM.", Locale.getDefault())

    val poDanuMapa = kvarovi
        .groupBy { format.format(Date(it.vremePrijave)) }
        .mapValues { it.value.size }

    val poDanuLista = kvarovi
        .sortedBy { it.vremePrijave }
        .map { format.format(Date(it.vremePrijave)) }
        .distinct()
        .map { it to (poDanuMapa[it] ?: 0) }

    return Statistika(
        ukupnoKvarova = kvarovi.size,
        poTipu = kvarovi.groupingBy { it.tip }.eachCount(),
        poMajstoru = kvarovi
            .filter { it.dodeljenMajstoru.isNotBlank() }
            .groupingBy { it.dodeljenMajstoru }
            .eachCount(),
        poDanu = poDanuLista,
        zavrseno = kvarovi.count { it.status == "Završeno" },
        hitnih = kvarovi.count { it.hitno }
    )
}

val bojePaleta = listOf(
    0xFF1565C0, 0xFFF57C00, 0xFF2E7D32, 0xFFD32F2F, 0xFF7B1FA2, 0xFF00838F
)
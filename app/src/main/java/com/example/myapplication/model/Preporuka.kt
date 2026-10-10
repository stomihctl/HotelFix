package com.example.myapplication.model

private val mapaSpecijalizacija = mapOf(
    "Svetlo" to listOf("Elektricar", "Elektro"),
    "Klima" to listOf("Klima", "Grejanje", "Vent"),
    "Grejanje" to listOf("Grejanje", "Klima"),
    "Tuš/Voda" to listOf("Vodoinstalater", "Voda"),
    "Drugo" to emptyList()
)

fun poklapaSeSaTipom(tip: String, specijalizacija: String): Boolean {
    val kljucneReci = mapaSpecijalizacija[tip] ?: return false
    if (kljucneReci.isEmpty()) return false
    return kljucneReci.any { specijalizacija.contains(it, ignoreCase = true) }
}

fun sortirajMajstore(
    tip: String,
    majstori: List<Korisnik>,
    zauzetost: Map<String, Int>
): List<Korisnik> {
    return majstori.sortedWith(
        compareByDescending<Korisnik> { poklapaSeSaTipom(tip, it.specijalizacija) }
            .thenBy { zauzetost[it.ime] ?: 0 }
    )
}
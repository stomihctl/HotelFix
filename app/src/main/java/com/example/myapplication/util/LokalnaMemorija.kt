package com.example.myapplication.util

import android.content.Context

private const val PREF_IME = "hotelfix_prefs"
private const val KLJUC_KVAROVI = "moji_kvarovi"

private const val KLJUC_POSLEDNJI_TOKEN = "poslednji_token"

fun sacuvajPoslednjiToken(context: Context, token: String) {
    val prefs = context.getSharedPreferences(PREF_IME, Context.MODE_PRIVATE)
    prefs.edit().putString(KLJUC_POSLEDNJI_TOKEN, token).apply()
}

fun ucitajPoslednjiToken(context: Context): String? {
    val prefs = context.getSharedPreferences(PREF_IME, Context.MODE_PRIVATE)
    return prefs.getString(KLJUC_POSLEDNJI_TOKEN, null)
}

fun obrisiPoslednjiToken(context: Context) {
    val prefs = context.getSharedPreferences(PREF_IME, Context.MODE_PRIVATE)
    prefs.edit().remove(KLJUC_POSLEDNJI_TOKEN).apply()
}

fun sacuvajMojKvar(context: Context, kvarId: String) {
    val prefs = context.getSharedPreferences(PREF_IME, Context.MODE_PRIVATE)
    val postojeci = prefs.getStringSet(KLJUC_KVAROVI, emptySet())?.toMutableSet() ?: mutableSetOf()
    postojeci.add(kvarId)
    prefs.edit().putStringSet(KLJUC_KVAROVI, postojeci).apply()
}

fun ucitajMojeKvarove(context: Context): List<String> {
    val prefs = context.getSharedPreferences(PREF_IME, Context.MODE_PRIVATE)
    return prefs.getStringSet(KLJUC_KVAROVI, emptySet())?.toList() ?: emptyList()
}
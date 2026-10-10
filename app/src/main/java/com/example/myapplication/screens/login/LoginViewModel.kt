package com.example.myapplication.screens.login

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.Korisnik
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class LoginStanje {
    object Prazno : LoginStanje()
    object UToku : LoginStanje()
    data class Uspeh(val korisnik: Korisnik) : LoginStanje()
    data class Greska(val poruka: String) : LoginStanje()
}

class LoginViewModel : ViewModel() {
    private val auth = FirebaseAuth.getInstance()
    private val firestore = FirebaseFirestore.getInstance()

    var email by mutableStateOf("")
    var lozinka by mutableStateOf("")

    private val _stanje = MutableStateFlow<LoginStanje>(LoginStanje.Prazno)
    val stanje: StateFlow<LoginStanje> = _stanje

    fun prijaviSe() {
        viewModelScope.launch {
            _stanje.value = LoginStanje.UToku
            try {
                val rezultat = auth.signInWithEmailAndPassword(email, lozinka).await()
                val uid = rezultat.user?.uid ?: throw Exception("Neuspešna prijava")

                val dokument = firestore.collection("korisnici").document(uid).get().await()
                val korisnik = dokument.toObject(Korisnik::class.java)
                    ?: throw Exception("Korisnik nije pronađen u bazi")

                _stanje.value = LoginStanje.Uspeh(korisnik)
            } catch (e: Exception) {
                val poruka = when {
                    e.message?.contains("password is invalid", true) == true ||
                            e.message?.contains("no user record", true) == true ->
                        "Pogrešan email ili lozinka."
                    e.message?.contains("nije pronađen") == true ->
                        "Nalog postoji, ali nije povezan sa hotelskim sistemom. Obratite se administratoru."
                    else -> "Greška pri prijavi. Proverite internet konekciju."
                }
                _stanje.value = LoginStanje.Greska(poruka)
            }
        }
    }
}
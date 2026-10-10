package com.example.myapplication.screens.startup

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import com.example.myapplication.model.Korisnik
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

@Composable
fun StartupScreen(
    onGost: () -> Unit,
    onMenadzer: (String) -> Unit,
    onMajstor: (String) -> Unit
) {
    LaunchedEffect(Unit) {
        val trenutniKorisnik = FirebaseAuth.getInstance().currentUser
        if (trenutniKorisnik == null) {
            onGost() // niko nije ulogovan
            return@LaunchedEffect
        }

        try {
            //trazimo ulogu korisnika
            val dokument = FirebaseFirestore.getInstance()
                .collection("korisnici")
                .document(trenutniKorisnik.uid)
                .get()
                .await()
            //pretvaramo je u objekat
            val korisnik = dokument.toObject(Korisnik::class.java)
            when (korisnik?.uloga) {
                "MENADZER" -> onMenadzer(korisnik.ime)
                "MAJSTOR" -> onMajstor(korisnik.ime)
                else -> onGost()
            }
        } catch (e: Exception) {
            onGost() // ako nešto pukne vrati na login
        }
    }

    Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
        CircularProgressIndicator()
    }
}
package com.example.myapplication.screens.menadzer

import android.graphics.Bitmap
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.SobaToken
import com.example.myapplication.model.generisiQrBitmap
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class SobaQrViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    var brojSobe by mutableStateOf("")
    var generisaniQr by mutableStateOf<Bitmap?>(null)
    var trenutniToken by mutableStateOf("")

    private val _postojeciTokeni = MutableStateFlow<List<SobaToken>>(emptyList())
    val postojeciTokeni: StateFlow<List<SobaToken>> = _postojeciTokeni

    init {
        firestore.collection("sobe_tokeni")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                _postojeciTokeni.value = snapshot.toObjects(SobaToken::class.java)
            }
    }

    fun generisiZaSobu() {
        if (brojSobe.isBlank()) return
        val noviToken = UUID.randomUUID().toString()

        viewModelScope.launch {
            val sobaTokenObj = SobaToken(token = noviToken, broj = brojSobe, aktivan = true)
            firestore.collection("sobe_tokeni").document(noviToken).set(sobaTokenObj).await()

            trenutniToken = noviToken
            generisaniQr = generisiQrBitmap(noviToken)
        }
    }

    fun deaktivirajToken(token: String) {
        viewModelScope.launch {
            firestore.collection("sobe_tokeni").document(token)
                .update("aktivan", false).await()
        }
    }
}
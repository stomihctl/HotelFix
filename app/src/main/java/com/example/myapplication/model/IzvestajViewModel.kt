package com.example.myapplication.screens.menadzer

import androidx.lifecycle.ViewModel
import com.example.myapplication.model.Kvar
import com.example.myapplication.model.Statistika
import com.example.myapplication.model.izracunajStatistiku
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class IzvestajViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _statistika = MutableStateFlow(Statistika())
    val statistika: StateFlow<Statistika> = _statistika

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    init {
        firestore.collection("kvarovi")
            .addSnapshotListener { snapshot, error ->
                _isLoading.value = false
                if (error != null || snapshot == null) return@addSnapshotListener
                val kvarovi = snapshot.toObjects(Kvar::class.java)
                _statistika.value = izracunajStatistiku(kvarovi)
            }
    }
}
package com.example.myapplication.screens.gost

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.Kvar
import com.example.myapplication.util.ucitajMojeKvarove
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MojiKvaroviViewModel : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _kvarovi = MutableStateFlow<List<Kvar>>(emptyList())
    val kvarovi: StateFlow<List<Kvar>> = _kvarovi

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    fun ucitaj(context: Context) {
        val idjevi = ucitajMojeKvarove(context)
        if (idjevi.isEmpty()) {
            _isLoading.value = false
            return
        }

        viewModelScope.launch {
            val rezultati = mutableListOf<Kvar>()
            for (id in idjevi) {
                try {
                    val dokument = firestore.collection("kvarovi").document(id).get().await()
                    dokument.toObject(Kvar::class.java)?.let { rezultati.add(it) }
                } catch (e: Exception) {
                    // preskoči ako fali internet ili je dokument obrisan
                }
            }
            _kvarovi.value = rezultati.sortedByDescending { it.vremePrijave }
            _isLoading.value = false
        }
    }
}
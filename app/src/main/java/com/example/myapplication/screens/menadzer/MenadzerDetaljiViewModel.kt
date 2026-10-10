package com.example.myapplication.screens.menadzer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.Korisnik
import com.example.myapplication.model.Kvar
import com.example.myapplication.util.posaljiNotifikaciju
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MenadzerDetaljiViewModel(private val kvarId: String, context: Context) : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val appContext = context.applicationContext

    private val _kvar = MutableStateFlow<Kvar?>(null)
    val kvar: StateFlow<Kvar?> = _kvar

    private val _majstori = MutableStateFlow<List<Korisnik>>(emptyList())
    val majstori: StateFlow<List<Korisnik>> = _majstori

    private val _zauzetost = MutableStateFlow<Map<String, Int>>(emptyMap())
    val zauzetost: StateFlow<Map<String, Int>> = _zauzetost

    private var prviPut = true
    private var poslednjiPoznatiStatus: String? = null

    init {
        firestore.collection("kvarovi").document(kvarId)
            .addSnapshotListener { snapshot, _ ->
                _kvar.value = snapshot?.toObject(Kvar::class.java)
            }

        firestore.collection("korisnici")
            .whereEqualTo("uloga", "MAJSTOR")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                _majstori.value = snapshot.toObjects(Korisnik::class.java)
            }

        firestore.collection("kvarovi")
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener
                val sviKvarovi = snapshot.toObjects(Kvar::class.java)
                _zauzetost.value = sviKvarovi
                    .filter { it.status != "Zatvoreno" && it.dodeljenMajstoru.isNotBlank() }
                    .groupingBy { it.dodeljenMajstoru }
                    .eachCount()
            }
    }

    fun dodeliMajstora(imeMajstora: String) {
        viewModelScope.launch {
            firestore.collection("kvarovi").document(kvarId)
                .update(mapOf("dodeljenMajstoru" to imeMajstora, "status" to "Dodeljeno"))
                .await()
        }
    }

    fun zatvoriKvar() {
        viewModelScope.launch {
            firestore.collection("kvarovi").document(kvarId)
                .update("status", "Zatvoreno")
                .await()
        }
    }

    fun sacuvajNapomenu(tekst: String) {
        viewModelScope.launch {
            firestore.collection("kvarovi").document(kvarId)
                .update("napomenaZaMajstora", tekst)
                .await()
        }
    }
}

class MenadzerDetaljiViewModelFactory(
    private val kvarId: String,
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        MenadzerDetaljiViewModel(kvarId, context) as T
}
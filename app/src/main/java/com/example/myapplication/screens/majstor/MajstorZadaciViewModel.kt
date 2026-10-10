package com.example.myapplication.screens.majstor

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.model.Kvar
import com.example.myapplication.util.posaljiNotifikaciju
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MajstorZadaciViewModel(private val imeMajstora: String, context: Context) : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val appContext = context.applicationContext

    private val _zadaci = MutableStateFlow<List<Kvar>>(emptyList())
    val zadaci: StateFlow<List<Kvar>> = _zadaci

    private var prviPut = true
    private var poznatiIdjevi = setOf<String>()

    init {
        firestore.collection("kvarovi")
            .whereEqualTo("dodeljenMajstoru", imeMajstora)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener

                val novaLista = snapshot.toObjects(Kvar::class.java)
                val noviIdjevi = novaLista.map { it.id }.toSet()

                if (!prviPut) {
                    val dodati = noviIdjevi - poznatiIdjevi
                    if (dodati.isNotEmpty()) {
                        val novKvar = novaLista.firstOrNull { it.id in dodati }
                        novKvar?.let {
                            posaljiNotifikaciju(
                                appContext,
                                "Novi zadatak dodeljen",
                                "${it.tip} — Soba ${it.soba}"
                            )
                        }
                    }
                }

                prviPut = false
                poznatiIdjevi = noviIdjevi
                _zadaci.value = novaLista
            }
    }
}

class MajstorZadaciViewModelFactory(
    private val imeMajstora: String,
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        MajstorZadaciViewModel(imeMajstora, context) as T
}
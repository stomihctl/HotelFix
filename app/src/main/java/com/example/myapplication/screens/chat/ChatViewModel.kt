package com.example.myapplication.screens.chat

import android.content.Context
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.Poruka
import com.example.myapplication.util.posaljiNotifikaciju
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.UUID

class ChatViewModel(
    private val kvarId: String,
    private val mojaUloga: String,
    context: Context
) : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val appContext = context.applicationContext

    var novaPoruka by mutableStateOf("")

    private val _poruke = MutableStateFlow<List<Poruka>>(emptyList())
    val poruke: StateFlow<List<Poruka>> = _poruke

    private var prviPut = true
    private var poznatiIdjevi = setOf<String>()

    init {
        firestore.collection("kvarovi").document(kvarId)
            .collection("poruke")
            .orderBy("vreme", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null || snapshot == null) return@addSnapshotListener //slusa bazu

                val novaLista = snapshot.toObjects(Poruka::class.java) //podaci u obj
                val noviIdjevi = novaLista.map { it.id }.toSet()

                if (!prviPut) {// slanje notif za nove
                    val dodati = noviIdjevi - poznatiIdjevi
                    dodati.forEach { id ->
                        val poruka = novaLista.firstOrNull { it.id == id }
                        if (poruka != null && poruka.posiljalac != mojaUloga) {
                            val ime = if (poruka.posiljalac == "MENADZER") "Menadžer" else "Gost"
                            posaljiNotifikaciju(appContext, "Nova poruka od: $ime", poruka.tekst)
                        }
                    }
                }

                prviPut = false
                poznatiIdjevi = noviIdjevi
                _poruke.value = novaLista
            }
    }

    fun posaljiPoruku(posiljalac: String) {
        if (novaPoruka.isBlank()) return
        val tekst = novaPoruka
        novaPoruka = ""

        viewModelScope.launch {
            val poruka = Poruka(id = UUID.randomUUID().toString(), posiljalac = posiljalac, tekst = tekst)
            firestore.collection("kvarovi").document(kvarId)
                .collection("poruke").document(poruka.id)
                .set(poruka).await()
        }
    }
}

class ChatViewModelFactory(
    private val kvarId: String,
    private val mojaUloga: String,
    private val context: Context
) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        ChatViewModel(kvarId, mojaUloga, context) as T
}
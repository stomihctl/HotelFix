package com.example.myapplication.screens.menadzer

import android.content.Context
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import com.example.myapplication.model.Kvar
import com.example.myapplication.util.posaljiNotifikaciju
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow

class MenadzerListaViewModel(context: Context) : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()
    private val appContext = context.applicationContext

    private val _kvarovi = MutableStateFlow<List<Kvar>>(emptyList())
    val kvarovi: StateFlow<List<Kvar>> = _kvarovi

    private val _isLoading = MutableStateFlow(true)
    val isLoading: StateFlow<Boolean> = _isLoading

    private var prviPut = true
    private var poznatiStatusi = mapOf<String, String>()

    init {
        firestore.collection("kvarovi")
            .orderBy("vremePrijave", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                _isLoading.value = false
                if (error != null || snapshot == null) return@addSnapshotListener

                val novaLista = snapshot.toObjects(Kvar::class.java)
                val noviStatusi = novaLista.associate { it.id to it.status }

                if (!prviPut) {
                    novaLista.forEach { kvar ->
                        val stariStatus = poznatiStatusi[kvar.id]

                        if (stariStatus == null) {
                            // potpuno nov kvar
                            posaljiNotifikaciju(
                                appContext,
                                "Novi kvar prijavljen",
                                "${kvar.tip} — Soba ${kvar.soba}"
                            )
                        } else if (stariStatus != kvar.status) {
                            // promena statusa na postojećem kvaru
                            val poruka = if (kvar.dodeljenMajstoru.isBlank() && kvar.poslednjeOdbijanje.isNotBlank()) {
                                "Majstor je odbio: ${kvar.poslednjeOdbijanje}"
                            } else {
                                "${kvar.tip} — Soba ${kvar.soba}: ${kvar.status}"
                            }
                            posaljiNotifikaciju(appContext, "Status promenjen", poruka)
                        }
                    }
                }

                prviPut = false
                poznatiStatusi = noviStatusi
                _kvarovi.value = novaLista
            }
    }
}

class MenadzerListaViewModelFactory(private val context: Context) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        MenadzerListaViewModel(context) as T
}
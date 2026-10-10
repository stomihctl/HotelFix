package com.example.myapplication.screens.majstor

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.Kvar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class MajstorDetaljiViewModel(private val kvarId: String) : ViewModel() {
    private val firestore = FirebaseFirestore.getInstance()

    private val _kvar = MutableStateFlow<Kvar?>(null)
    val kvar: StateFlow<Kvar?> = _kvar

    init {
        firestore.collection("kvarovi").document(kvarId)
            .addSnapshotListener { snapshot, _ ->
                _kvar.value = snapshot?.toObject(Kvar::class.java)
            }
    }

    fun promeniStatus(noviStatus: String) {
        viewModelScope.launch {
            firestore.collection("kvarovi").document(kvarId)
                .update("status", noviStatus)
                .await()
        }
    }

    fun odbijZadatak(imeMajstora: String, razlog: String) {
        viewModelScope.launch {
            firestore.collection("kvarovi").document(kvarId)
                .update(
                    mapOf(
                        "status" to "Prijavljeno",
                        "dodeljenMajstoru" to "",
                        "poslednjeOdbijanje" to "$imeMajstora: $razlog"
                    )
                )
                .await()
        }
    }
}

class MajstorDetaljiViewModelFactory(private val kvarId: String) : ViewModelProvider.Factory {
    @Suppress("UNCHECKED_CAST")
    override fun <T : ViewModel> create(modelClass: Class<T>): T =
        MajstorDetaljiViewModel(kvarId) as T
}
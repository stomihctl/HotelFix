package com.example.myapplication.screens.majstor

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.screens.menadzer.KvarKartica

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MajstorZadaciScreen(
    imeMajstora: String,
    onKvarKlik: (String) -> Unit,
    onOdjava: () -> Unit,
    viewModel: MajstorZadaciViewModel = viewModel(
        factory = MajstorZadaciViewModelFactory(imeMajstora, androidx.compose.ui.platform.LocalContext.current)
    )
) {
    val zadaci by viewModel.zadaci.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Moji zadaci — $imeMajstora") },
                actions = {
                    IconButton(onClick = onOdjava) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Odjavi se")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            if (zadaci.isEmpty()) {
                Text("Trenutno nemaš dodeljenih zadataka.")
            }

            LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                items(zadaci) { kvar ->
                    KvarKartica(kvar = kvar, onClick = { onKvarKlik(kvar.id) })
                }
            }
        }
    }
}
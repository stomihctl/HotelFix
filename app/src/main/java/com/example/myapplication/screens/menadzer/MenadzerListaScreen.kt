package com.example.myapplication.screens.menadzer

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ExitToApp
import androidx.compose.material.icons.filled.Assessment
import androidx.compose.material.icons.filled.QrCode
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.model.Kvar
import com.example.myapplication.ui.theme.bojaStatusa

private val tabovi = listOf("Aktivni", "Zatvoreni")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenadzerListaScreen(
    onKvarKlik: (String) -> Unit,
    onGenerisiQr: () -> Unit,
    onIzvestaj: () -> Unit,
    onOdjava: () -> Unit,
    viewModel: MenadzerListaViewModel = viewModel(
        factory = MenadzerListaViewModelFactory(androidx.compose.ui.platform.LocalContext.current)
    )
) {
    val kvarovi by viewModel.kvarovi.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()
    var izabraniTab by remember { mutableStateOf(0) }

    val filtrirani = remember(kvarovi, izabraniTab) {
        if (izabraniTab == 0) {
            kvarovi.filter { it.status != "Zatvoreno" }
        } else {
            kvarovi.filter { it.status == "Zatvoreno" }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Svi kvarovi") },
                actions = {
                    IconButton(onClick = onGenerisiQr) {
                        Icon(Icons.Default.QrCode, contentDescription = "Generiši QR kod")
                    }
                    IconButton(onClick = onIzvestaj) {
                        Icon(Icons.Default.Assessment, contentDescription = "Izveštaj")
                    }
                    IconButton(onClick = onOdjava) {
                        Icon(Icons.AutoMirrored.Filled.ExitToApp, contentDescription = "Odjavi se")
                    }
                }
            )
        }
    ) { padding ->
        Column(modifier = Modifier.fillMaxSize().padding(padding)) {
            TabRow(selectedTabIndex = izabraniTab) {
                tabovi.forEachIndexed { index, naziv ->
                    Tab(
                        selected = izabraniTab == index,
                        onClick = { izabraniTab = index },
                        text = { Text(naziv) }
                    )
                }
            }

            Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
                if (isLoading) {
                    CircularProgressIndicator()
                } else if (filtrirani.isEmpty()) {
                    Text(if (izabraniTab == 0) "Nema aktivnih kvarova." else "Nema zatvorenih prijava.")
                }

                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(filtrirani) { kvar ->
                        KvarKartica(kvar = kvar, onClick = { onKvarKlik(kvar.id) })
                    }
                }
            }
        }
    }
}

@Composable
fun KvarKartica(kvar: Kvar, onClick: () -> Unit) {
    Card(onClick = onClick, modifier = Modifier.fillMaxWidth()) {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text("${kvar.tip} — Soba ${kvar.soba}", style = MaterialTheme.typography.titleMedium)
                if (kvar.hitno) {
                    Text("HITNO", color = Color.Red, style = MaterialTheme.typography.labelMedium)
                }
            }
            Spacer(Modifier.height(4.dp))
            Text(
                "Status: ${kvar.status}",
                color = bojaStatusa(kvar.status),
                style = MaterialTheme.typography.bodyMedium
            )
        }
    }
}
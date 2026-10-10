package com.example.myapplication.screens.gost

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.ui.theme.bojaStatusa

@Composable
fun MojiKvaroviScreen(
    onKvarKlik: (String) -> Unit,
    viewModel: MojiKvaroviViewModel = viewModel()
) {
    val context = LocalContext.current
    val kvarovi by viewModel.kvarovi.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.ucitaj(context)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("Moje prijave", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(12.dp))

        if (isLoading) {
            CircularProgressIndicator()
        } else if (kvarovi.isEmpty()) {
            Text("Nemate prijavljenih kvarova sa ovog uređaja.")
        }

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(kvarovi) { kvar ->
                Card(
                    onClick = { onKvarKlik(kvar.id) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text("${kvar.tip} — Soba ${kvar.soba}", style = MaterialTheme.typography.titleMedium)
                        Spacer(Modifier.height(4.dp))
                        Text(
                            "Status: ${kvar.status}",
                            color = bojaStatusa(kvar.status),
                            style = MaterialTheme.typography.bodyMedium
                        )
                    }
                }
            }
        }
    }
}
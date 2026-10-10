package com.example.myapplication.screens.majstor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.model.Base64Slika

private val redosledStatusa = listOf("Dodeljeno", "Prihvaćeno", "U toku", "Završeno")

@Composable
fun MajstorDetaljiScreen(
    kvarId: String,
    imeMajstora: String,
    viewModel: MajstorDetaljiViewModel = viewModel(factory = MajstorDetaljiViewModelFactory(kvarId))
) {
    val kvar by viewModel.kvar.collectAsState()
    val trenutniKvar = kvar
    var dijalogOtvoren by remember { mutableStateOf(false) }
    var razlog by remember { mutableStateOf("") }

    if (trenutniKvar == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val trenutniIndeks = redosledStatusa.indexOf(trenutniKvar.status).coerceAtLeast(0)
    val sledeciStatus = redosledStatusa.getOrNull(trenutniIndeks + 1)
    val mozeDaOdbije = trenutniKvar.status == "Dodeljeno"

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("${trenutniKvar.tip} — Soba ${trenutniKvar.soba}", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text("Opis: ${trenutniKvar.opis}")
        Spacer(Modifier.height(8.dp))
        Text("Hitno: ${if (trenutniKvar.hitno) "Da" else "Ne"}")
        Spacer(Modifier.height(8.dp))
        Text("Trenutni status: ${trenutniKvar.status}")

        Spacer(Modifier.height(12.dp))
        Base64Slika(
            base64 = trenutniKvar.slikaBase64,
            modifier = Modifier.fillMaxWidth().height(200.dp)
        )

        if (trenutniKvar.napomenaZaMajstora.isNotBlank()) {
            Spacer(Modifier.height(12.dp))
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Napomena od menadžera:", style = MaterialTheme.typography.labelMedium)
                    Spacer(Modifier.height(4.dp))
                    Text(trenutniKvar.napomenaZaMajstora)
                }
            }
        }

        Spacer(Modifier.height(24.dp))

        if (sledeciStatus != null) {
            Button(
                onClick = { viewModel.promeniStatus(sledeciStatus) },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Označi kao: $sledeciStatus")
            }
        } else {
            Text("Zadatak je završen.", style = MaterialTheme.typography.titleMedium)
        }

        if (mozeDaOdbije) {
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                onClick = { dijalogOtvoren = true },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Odbij zadatak")
            }
        }
    }

    if (dijalogOtvoren) {
        AlertDialog(
            onDismissRequest = { dijalogOtvoren = false },
            title = { Text("Odbijanje zadatka") },
            text = {
                Column {
                    Text("Unesite razlog odbijanja — vraća se menadžeru na ponovnu dodelu.")
                    Spacer(Modifier.height(8.dp))
                    OutlinedTextField(
                        value = razlog,
                        onValueChange = { razlog = it },
                        placeholder = { Text("npr. Van smene do sutra") },
                        minLines = 2,
                        modifier = Modifier.fillMaxWidth()
                    )
                }
            },
            confirmButton = {
                TextButton(
                    onClick = {
                        if (razlog.isNotBlank()) {
                            viewModel.odbijZadatak(imeMajstora, razlog)
                            dijalogOtvoren = false
                            razlog = ""
                        }
                    },
                    enabled = razlog.isNotBlank()
                ) {
                    Text("Potvrdi odbijanje")
                }
            },
            dismissButton = {
                TextButton(onClick = { dijalogOtvoren = false }) {
                    Text("Otkaži")
                }
            }
        )
    }
}
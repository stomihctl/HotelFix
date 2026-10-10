package com.example.myapplication.screens.menadzer

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.model.Base64Slika
import com.example.myapplication.model.poklapaSeSaTipom
import com.example.myapplication.model.sortirajMajstore

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MenadzerDetaljiScreen(
    kvarId: String,
    onOtvoriChat: () -> Unit,
    viewModel: MenadzerDetaljiViewModel = viewModel(
        factory = MenadzerDetaljiViewModelFactory(kvarId, androidx.compose.ui.platform.LocalContext.current)
    )
) {
    val kvar by viewModel.kvar.collectAsState()
    val majstori by viewModel.majstori.collectAsState()
    val zauzetost by viewModel.zauzetost.collectAsState()
    var dropdownOtvoren by remember { mutableStateOf(false) }

    val trenutniKvar = kvar
    if (trenutniKvar == null) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    val jeZatvoreno = trenutniKvar.status == "Zatvoreno"
    val sortiraniMajstori = remember(majstori, zauzetost, trenutniKvar.tip) {
        sortirajMajstore(trenutniKvar.tip, majstori, zauzetost)
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        Text("${trenutniKvar.tip} — Soba ${trenutniKvar.soba}", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text("Opis: ${trenutniKvar.opis}")
        Spacer(Modifier.height(8.dp))
        Text("Hitno: ${if (trenutniKvar.hitno) "Da" else "Ne"}")
        Spacer(Modifier.height(8.dp))
        Text("Status: ${trenutniKvar.status}")
        Spacer(Modifier.height(8.dp))
        Text("Dodeljen majstor: ${trenutniKvar.dodeljenMajstoru.ifBlank { "Nije dodeljen" }}")

        if (trenutniKvar.poslednjeOdbijanje.isNotBlank() && trenutniKvar.dodeljenMajstoru.isBlank()) {
            Spacer(Modifier.height(8.dp))
            Card(colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.errorContainer)) {
                Column(modifier = Modifier.padding(12.dp)) {
                    Text("Odbijeno:", style = MaterialTheme.typography.labelMedium)
                    Text(trenutniKvar.poslednjeOdbijanje)
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        Base64Slika(
            base64 = trenutniKvar.slikaBase64,
            modifier = Modifier.fillMaxWidth().height(200.dp)
        )

        Spacer(Modifier.height(16.dp))
        var napomena by remember(trenutniKvar.id) { mutableStateOf(trenutniKvar.napomenaZaMajstora) }
        OutlinedTextField(
            value = napomena,
            onValueChange = { napomena = it },
            label = { Text("Napomena za majstora") },
            minLines = 1,
            modifier = Modifier.fillMaxWidth(),
            enabled = !jeZatvoreno
        )
        if (!jeZatvoreno) {
            Spacer(Modifier.height(4.dp))
            TextButton(onClick = { viewModel.sacuvajNapomenu(napomena) }) {
                Text("Sačuvaj napomenu")
            }
        }

        if (!jeZatvoreno) {
            Spacer(Modifier.height(24.dp))
            Text("Dodeli majstora:")

            if (sortiraniMajstori.isEmpty()) {
                Text("Nema registrovanih majstora u sistemu.")
            } else {
                ExposedDropdownMenuBox(
                    expanded = dropdownOtvoren,
                    onExpandedChange = { dropdownOtvoren = it }
                ) {
                    OutlinedTextField(
                        value = trenutniKvar.dodeljenMajstoru.ifBlank { "Izaberi majstora" },
                        onValueChange = {},
                        readOnly = true,
                        modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
                    )
                    ExposedDropdownMenu(
                        expanded = dropdownOtvoren,
                        onDismissRequest = { dropdownOtvoren = false }
                    ) {
                        sortiraniMajstori.forEach { majstor ->
                            val poklapaSe = poklapaSeSaTipom(trenutniKvar.tip, majstor.specijalizacija)
                            val brojZadataka = zauzetost[majstor.ime] ?: 0

                            DropdownMenuItem(
                                text = {
                                    Column {
                                        Row(verticalAlignment = Alignment.CenterVertically) {
                                            Text(majstor.ime, style = MaterialTheme.typography.bodyLarge)
                                            if (poklapaSe) {
                                                Spacer(Modifier.width(6.dp))
                                                Text(
                                                    "★ preporučeno",
                                                    style = MaterialTheme.typography.labelSmall,
                                                    color = MaterialTheme.colorScheme.primary
                                                )
                                            }
                                        }
                                        Text(
                                            "${majstor.specijalizacija.ifBlank { "Bez specijalizacije" }} · $brojZadataka aktivnih zadataka",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                },
                                onClick = {
                                    viewModel.dodeliMajstora(majstor.ime)
                                    dropdownOtvoren = false
                                }
                            )
                        }
                    }
                }
            }

            if (trenutniKvar.status == "Završeno") {
                Spacer(Modifier.height(12.dp))
                Button(
                    onClick = { viewModel.zatvoriKvar() },
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.tertiary)
                ) {
                    Text("Zatvori prijavu (prinudno)")
                }
                Text(
                    "Koristite samo ako gost ne može sam da potvrdi (npr. već odjavljen).",
                    style = MaterialTheme.typography.bodySmall
                )
            }
        } else {
            Spacer(Modifier.height(16.dp))
            Text(
                "Prijava je zatvorena.",
                style = MaterialTheme.typography.titleMedium,
                color = MaterialTheme.colorScheme.tertiary
            )
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = onOtvoriChat,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Otvori chat sa gostom")
        }
    }
}
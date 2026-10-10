package com.example.myapplication.screens.chat

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.compose.ui.platform.LocalContext

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ChatScreen(
    kvarId: String,
    mojaUloga: String,
    zakljucan: Boolean = false,
    onNazad: () -> Unit = {},
    viewModel: ChatViewModel = viewModel(
        factory = ChatViewModelFactory(kvarId, mojaUloga, LocalContext.current)
    )
) {
    val poruke by viewModel.poruke.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Chat o prijavi") },
                navigationIcon = {
                    IconButton(onClick = onNazad) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Nazad")
                    }
                }
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(16.dp)
        ) {
            LazyColumn(
                modifier = Modifier.weight(1f),
                verticalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                items(poruke) { poruka ->
                    val mojaPoruka = poruka.posiljalac == mojaUloga
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        horizontalArrangement = if (mojaPoruka) Arrangement.End else Arrangement.Start
                    ) {
                        Card(
                            colors = CardDefaults.cardColors(
                                containerColor = if (mojaPoruka)
                                    MaterialTheme.colorScheme.primary
                                else
                                    MaterialTheme.colorScheme.surfaceVariant
                            )
                        ) {
                            Column(modifier = Modifier.padding(10.dp)) {
                                if (!mojaPoruka) {
                                    Text(
                                        if (poruka.posiljalac == "MENADZER") "Menadžer" else "Gost",
                                        style = MaterialTheme.typography.labelSmall
                                    )
                                }
                                Text(poruka.tekst)
                            }
                        }
                    }
                }
            }

            Spacer(Modifier.height(8.dp))

            if (zakljucan) {
                Text(
                    "Prijava je zatvorena — chat je arhiviran.",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            } else {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    OutlinedTextField(
                        value = viewModel.novaPoruka,
                        onValueChange = { viewModel.novaPoruka = it },
                        modifier = Modifier.weight(1f),
                        placeholder = { Text("Poruka...") }
                    )
                    Spacer(Modifier.width(8.dp))
                    Button(onClick = { viewModel.posaljiPoruku(mojaUloga) }) {
                        Text("Pošalji")
                    }
                }
            }
        }
    }
}
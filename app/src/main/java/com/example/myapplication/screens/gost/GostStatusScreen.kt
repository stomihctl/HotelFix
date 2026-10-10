package com.example.myapplication.screens.gost

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.Kvar
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.launch
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.platform.LocalContext
import com.example.myapplication.util.posaljiNotifikaciju
import kotlinx.coroutines.tasks.await

@Composable
fun GostStatusScreen(kvarId: String, onOtvoriChat: () -> Unit) {
    val context = LocalContext.current
    var kvar by remember { mutableStateOf<Kvar?>(null) }
    val scope = rememberCoroutineScope()
    val firestore = remember { FirebaseFirestore.getInstance() }

    var prethodniStatus by remember { mutableStateOf<String?>(null) }

    DisposableEffect(kvarId) {// bezbedno brisanje notif
        val listener = firestore
            .collection("kvarovi").document(kvarId)
            .addSnapshotListener { snapshot, _ ->
                val ucitani = snapshot?.toObject(Kvar::class.java)
                if (prethodniStatus != null && ucitani != null &&
                    ucitani.status == "Završeno" && prethodniStatus != "Završeno"
                ) {
                    posaljiNotifikaciju(context, "Kvar je rešen!", "${ucitani.tip} — Soba ${ucitani.soba}")
                }
                prethodniStatus = ucitani?.status
                kvar = ucitani
            }
        onDispose { listener.remove() }
    }

    val trenutniKvar = kvar

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (trenutniKvar == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator()
            }
            return@Column
        }

        val jeZatvoreno = trenutniKvar.status == "Zatvoreno"

        Text("${trenutniKvar.tip} — Soba ${trenutniKvar.soba}", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(8.dp))
        Text("Status: ${trenutniKvar.status}")

        if (trenutniKvar.status == "Završeno") {
            Spacer(Modifier.height(12.dp))
            Text(
                "Da li je kvar zaista rešen?",
                style = MaterialTheme.typography.bodyMedium
            )
            Spacer(Modifier.height(8.dp))
            Button(
                onClick = {
                    scope.launch {
                        firestore.collection("kvarovi").document(kvarId)
                            .update("status", "Zatvoreno")
                            .await()
                    }
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Potvrdi — kvar je rešen")
            }
        }

        if (jeZatvoreno) {
            Spacer(Modifier.height(8.dp))
            Text(
                "Hvala na potvrdi, prijava je zatvorena.",
                color = MaterialTheme.colorScheme.tertiary
            )
        }

        Spacer(Modifier.height(16.dp))
        Button(
            onClick = onOtvoriChat,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Otvori chat sa menadžerom")
        }
    }
}
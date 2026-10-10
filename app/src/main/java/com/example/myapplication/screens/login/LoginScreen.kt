package com.example.myapplication.screens.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.model.Korisnik

@Composable
fun LoginScreen(
    onPrijavaUspesna: (Korisnik) -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val stanje by viewModel.stanje.collectAsState()

    LaunchedEffect(stanje) {
        val trenutno = stanje
        if (trenutno is LoginStanje.Uspeh) onPrijavaUspesna(trenutno.korisnik)
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Prijava zaposlenog", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(24.dp))

        OutlinedTextField(
            value = viewModel.email,
            onValueChange = { viewModel.email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = viewModel.lozinka,
            onValueChange = { viewModel.lozinka = it },
            label = { Text("Lozinka") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(20.dp))

        if (stanje is LoginStanje.Greska) {
            Text(
                (stanje as LoginStanje.Greska).poruka,
                color = MaterialTheme.colorScheme.error
            )
            Spacer(Modifier.height(12.dp))
        }

        Button(
            onClick = { viewModel.prijaviSe() },
            enabled = stanje !is LoginStanje.UToku,
            modifier = Modifier.fillMaxWidth()
        ) {
            if (stanje is LoginStanje.UToku) {
                CircularProgressIndicator(modifier = Modifier.height(20.dp))
            } else {
                Text("Prijavi se")
            }
        }
    }
}
package com.example.myapplication.screens.izboruloge

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun IzborUlogeScreen(
    onGost: () -> Unit,
    onZaposleni: () -> Unit,
    onMojiKvarovi: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text("HotelFix", style = MaterialTheme.typography.headlineMedium)
        Spacer(Modifier.height(32.dp))

        Button(onClick = onGost, modifier = Modifier.fillMaxWidth()) {
            Text("Prijavi kvar (Gost)")
        }
        Spacer(Modifier.height(12.dp))
        OutlinedButton(onClick = onMojiKvarovi, modifier = Modifier.fillMaxWidth()) {
            Text("Moje prijave")
        }
        Spacer(Modifier.height(12.dp))
        Button(onClick = onZaposleni, modifier = Modifier.fillMaxWidth()) {
            Text("Prijava zaposlenog")
        }
    }
}
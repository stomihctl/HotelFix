package com.example.myapplication.screens.menadzer

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.util.sacuvajQrPdf

@Composable
fun SobaQrScreen(viewModel: SobaQrViewModel = viewModel()) {
    val context = LocalContext.current
    val tokeni by viewModel.postojeciTokeni.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Generisanje QR koda za sobu", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        OutlinedTextField(
            value = viewModel.brojSobe,
            onValueChange = { viewModel.brojSobe = it },
            label = { Text("Broj sobe") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(Modifier.height(12.dp))
        Button(
            onClick = { viewModel.generisiZaSobu() },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Generiši QR kod")
        }

        viewModel.generisaniQr?.let { bitmap ->
            Spacer(Modifier.height(16.dp))
            Image(
                bitmap = bitmap.asImageBitmap(),
                contentDescription = "QR kod za sobu ${viewModel.brojSobe}",
                modifier = Modifier.size(200.dp)
            )
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    val uspesno = sacuvajQrPdf(context, bitmap, viewModel.brojSobe)
                    Toast.makeText(
                        context,
                        if (uspesno) "PDF sačuvan u Downloads" else "Greška pri čuvanju PDF-a",
                        Toast.LENGTH_LONG
                    ).show()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("Preuzmi kao PDF")
            }
        }

        Spacer(Modifier.height(24.dp))
        Text("Postojeći QR kodovi", style = MaterialTheme.typography.titleMedium)
        Spacer(Modifier.height(8.dp))

        LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
            items(tokeni) { sobaToken ->
                Card(modifier = Modifier.fillMaxWidth()) {
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(12.dp),
                        horizontalArrangement = Arrangement.SpaceBetween
                    ) {
                        Column {
                            Text("Soba ${sobaToken.broj}")
                            Text(
                                if (sobaToken.aktivan) "Aktivan" else "Deaktiviran",
                                style = MaterialTheme.typography.bodySmall
                            )
                        }
                        if (sobaToken.aktivan) {
                            TextButton(onClick = { viewModel.deaktivirajToken(sobaToken.token) }) {
                                Text("Deaktiviraj")
                            }
                        }
                    }
                }
            }
        }
    }
}
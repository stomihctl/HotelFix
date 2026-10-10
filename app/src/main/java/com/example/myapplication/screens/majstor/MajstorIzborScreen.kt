package com.example.myapplication.screens.majstor

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.myapplication.model.hardkodovaniMajstori

@Composable
fun MajstorIzborScreen(onMajstorOdabran: (String) -> Unit) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        verticalArrangement = Arrangement.Center
    ) {
        Text("Ko si ti?", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        hardkodovaniMajstori.forEach { majstor ->
            Button(
                onClick = { onMajstorOdabran(majstor.ime) },
                modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp)
            ) {
                Text("${majstor.ime} (${majstor.specijalizacija})")
            }
        }
    }
}
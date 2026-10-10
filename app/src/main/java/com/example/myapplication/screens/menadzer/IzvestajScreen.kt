package com.example.myapplication.screens.menadzer

import android.widget.Toast
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.myapplication.model.bojePaleta
import com.example.myapplication.util.sacuvajIzvestajPdf

@Composable
fun IzvestajScreen(viewModel: IzvestajViewModel = viewModel()) {
    val context = LocalContext.current
    val statistika by viewModel.statistika.collectAsState()
    val isLoading by viewModel.isLoading.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp)
    ) {
        Text("Izveštaj o kvarovima", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        if (isLoading) {
            CircularProgressIndicator()
            return@Column
        }

        Text("Ukupno kvarova: ${statistika.ukupnoKvarova}")
        Text("Završeno: ${statistika.zavrseno}")
        Text("Hitnih: ${statistika.hitnih}")

        if (statistika.poTipu.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            Text("Po tipu kvara", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            PieChart(podaci = statistika.poTipu)
        }

        if (statistika.poMajstoru.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            Text("Po majstoru", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            PieChart(podaci = statistika.poMajstoru)
        }

        if (statistika.poDanu.isNotEmpty()) {
            Spacer(Modifier.height(20.dp))
            Text("Kvarovi po danima", style = MaterialTheme.typography.titleMedium)
            Spacer(Modifier.height(8.dp))
            BarChartPoDanu(podaci = statistika.poDanu)
        }

        Spacer(Modifier.height(24.dp))
        Button(
            onClick = {
                val uspesno = sacuvajIzvestajPdf(context, statistika)
                Toast.makeText(
                    context,
                    if (uspesno) "Izveštaj sačuvan u Downloads" else "Greška pri čuvanju",
                    Toast.LENGTH_LONG
                ).show()
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Preuzmi kao PDF")
        }
    }
}

@Composable
fun PieChart(podaci: Map<String, Int>, modifier: Modifier = Modifier) {
    val ukupno = podaci.values.sum().toFloat()
    if (ukupno == 0f) return
    val boje = bojePaleta.map { Color(it) }

    Row(modifier = modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Canvas(modifier = Modifier.size(120.dp)) {
            var pocetniUgao = -90f
            podaci.entries.forEachIndexed { index, (_, vrednost) ->
                val ugao = (vrednost / ukupno) * 360f
                drawArc(
                    color = boje[index % boje.size],
                    startAngle = pocetniUgao,
                    sweepAngle = ugao,
                    useCenter = true
                )
                pocetniUgao += ugao
            }
        }
        Spacer(Modifier.width(16.dp))
        Column {
            podaci.entries.forEachIndexed { index, (naziv, vrednost) ->
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(10.dp)
                            .background(boje[index % boje.size])
                    )
                    Spacer(Modifier.width(6.dp))
                    Text("$naziv ($vrednost)", style = MaterialTheme.typography.bodySmall)
                }
            }
        }
    }
}

@Composable
fun BarChartPoDanu(podaci: List<Pair<String, Int>>, modifier: Modifier = Modifier) {
    val maxVrednost = (podaci.maxOfOrNull { it.second } ?: 1).coerceAtLeast(1)

    Row(
        modifier = modifier
            .fillMaxWidth()
            .height(140.dp),
        horizontalArrangement = Arrangement.spacedBy(4.dp)
    ) {
        podaci.forEach { (datum, broj) ->
            Column(
                modifier = Modifier.weight(1f).fillMaxHeight(),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Bottom
            ) {
                Text(broj.toString(), style = MaterialTheme.typography.labelSmall)
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height((broj.toFloat() / maxVrednost * 90).dp)
                        .background(Color(0xFF1565C0))
                )
                Spacer(Modifier.height(2.dp))
                Text(datum, style = MaterialTheme.typography.labelSmall, maxLines = 1)
            }
        }
    }
}
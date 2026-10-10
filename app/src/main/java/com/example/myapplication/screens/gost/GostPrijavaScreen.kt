package com.example.myapplication.screens.gost

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import coil.compose.AsyncImage
import com.journeyapps.barcodescanner.ScanContract
import com.journeyapps.barcodescanner.ScanOptions

private val tipoviKvara = listOf("Svetlo", "Klima", "Grejanje", "Tuš/Voda", "Drugo")

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun GostPrijavaScreen(
    onPrijavaPoslata: (String) -> Unit,
    viewModel: GostPrijavaViewModel = viewModel()
) {
    val context = LocalContext.current
    val stanje by viewModel.stanjeSlanja.collectAsState()
    var dropdownOtvoren by remember { mutableStateOf(false) }
    var kameraUri by remember { mutableStateOf<Uri?>(null) }

    LaunchedEffect(Unit) {
        viewModel.proveriSacuvaniToken(context) //provera
    }

    //priprema ka sistemskim funkc
    val qrLauncher = rememberLauncherForActivityResult(ScanContract()) { rezultat ->
        if (rezultat.contents != null) {
            viewModel.validirajQr(context, rezultat.contents)
        }
    }

    val kameraLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.TakePicture()
    ) { uspesno ->
        if (uspesno) {
            viewModel.slikaUri = kameraUri
        }
    }

    val permisijaLauncher = rememberLauncherForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { odobreno -> //dozvola
        if (odobreno) {
            val uri = kreirajTempUriZaSliku(context)
            kameraUri = uri
            kameraLauncher.launch(uri)
        }
    }

    LaunchedEffect(stanje) { //svaki put kad se stanje promeni
        val trenutno = stanje
        if (trenutno is StanjeSlanja.Uspeh) onPrijavaPoslata(trenutno.kvarId)
    }

    if (viewModel.proveraUToku) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            CircularProgressIndicator()
        }
        return
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp)
    ) {
        Text("Prijava kvara", style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.height(16.dp))

        if (viewModel.sobaPotvrdjena) {
            Text(
                "Soba ${viewModel.soba} — potvrđeno ✓",
                color = MaterialTheme.colorScheme.primary,
                style = MaterialTheme.typography.titleMedium
            )
        } else {
            Text("Niste skenirali QR kod sobe.", style = MaterialTheme.typography.bodyMedium)
        }

        if (viewModel.greskaSkeniranja.isNotBlank()) {
            Spacer(Modifier.height(4.dp))
            Text(
                viewModel.greskaSkeniranja,
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall
            )
        }

        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            onClick = {
                qrLauncher.launch(
                    ScanOptions()
                        .setDesiredBarcodeFormats(ScanOptions.QR_CODE)
                        .setPrompt("Skenirajte QR kod u sobi")
                        .setBeepEnabled(false)
                )
            },
            modifier = Modifier.fillMaxWidth()
        ) {
            Text(if (viewModel.sobaPotvrdjena) "Skeniraj ponovo" else "Skeniraj QR kod sobe")
        }

        Spacer(Modifier.height(16.dp))

        ExposedDropdownMenuBox(
            expanded = dropdownOtvoren,
            onExpandedChange = { dropdownOtvoren = it }
        ) {
            OutlinedTextField(
                value = viewModel.tip,
                onValueChange = {},
                readOnly = true,
                label = { Text("Tip kvara") },
                modifier = Modifier.menuAnchor(MenuAnchorType.PrimaryNotEditable).fillMaxWidth()
            )
            ExposedDropdownMenu(
                expanded = dropdownOtvoren,
                onDismissRequest = { dropdownOtvoren = false }
            ) {
                tipoviKvara.forEach { opcija ->
                    DropdownMenuItem(
                        text = { Text(opcija) },
                        onClick = {
                            viewModel.tip = opcija
                            dropdownOtvoren = false
                        }
                    )
                }
            }
        }

        Spacer(Modifier.height(12.dp))
        OutlinedTextField(
            value = viewModel.opis,
            onValueChange = { viewModel.opis = it },
            label = { Text("Opis kvara") },
            minLines = 3,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(Modifier.height(12.dp))
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("Hitno")
            Spacer(Modifier.width(8.dp))
            Switch(checked = viewModel.hitno, onCheckedChange = { viewModel.hitno = it })
        }

        Spacer(Modifier.height(12.dp))
        Button(onClick = {
            permisijaLauncher.launch(android.Manifest.permission.CAMERA)
        }) {
            Text("Fotografiši kvar")
        }

        viewModel.slikaUri?.let { uri ->
            Spacer(Modifier.height(12.dp))
            AsyncImage(
                model = uri,
                contentDescription = "Slika kvara",
                modifier = Modifier.fillMaxWidth().height(180.dp)
            )
        }

        Spacer(Modifier.height(24.dp))

        when (stanje) {
            is StanjeSlanja.UToku -> CircularProgressIndicator()
            is StanjeSlanja.Greska -> Text(
                (stanje as StanjeSlanja.Greska).poruka,
                color = MaterialTheme.colorScheme.error
            )
            else -> {}
        }

        Button(
            onClick = { viewModel.posaljiPrijavu(context) },
            enabled = stanje !is StanjeSlanja.UToku && viewModel.sobaPotvrdjena && viewModel.opis.isNotBlank(),
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Prijavi kvar")
        }
    }
}
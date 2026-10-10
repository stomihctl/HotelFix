package com.example.myapplication.screens.gost

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.FileProvider
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.myapplication.model.Kvar
import com.example.myapplication.model.SobaToken
import com.example.myapplication.util.sacuvajMojKvar
import com.example.myapplication.util.sacuvajPoslednjiToken
import com.example.myapplication.util.ucitajPoslednjiToken
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream
import java.io.File
import java.util.UUID

class GostPrijavaViewModel : ViewModel() {

    private val firestore = FirebaseFirestore.getInstance()

    var tip by mutableStateOf("Svetlo")
    var opis by mutableStateOf("")
    var soba by mutableStateOf("")
    var sobaPotvrdjena by mutableStateOf(false)
    var greskaSkeniranja by mutableStateOf("")
    var hitno by mutableStateOf(false)
    var slikaUri by mutableStateOf<Uri?>(null)
    var proveraUToku by mutableStateOf(true)

    private val _stanjeSlanja = MutableStateFlow<StanjeSlanja>(StanjeSlanja.Prazno)
    val stanjeSlanja: StateFlow<StanjeSlanja> = _stanjeSlanja

    fun proveriSacuvaniToken(context: Context) { // proverava da li ima qr kod i vadi ga iz baze
        val sacuvaniToken = ucitajPoslednjiToken(context)
        if (sacuvaniToken == null) {
            proveraUToku = false
            return
        }

        viewModelScope.launch { //corut
            try {
                val dokument = firestore.collection("sobe_tokeni").document(sacuvaniToken).get().await()
                val sobaToken = dokument.toObject(SobaToken::class.java)

                if (sobaToken != null && sobaToken.aktivan) {
                    soba = sobaToken.broj
                    sobaPotvrdjena = true
                }
            } catch (e: Exception) {
                // nema interneta ili greška — ostaje nepotvrđeno, traži novo skeniranje
            }
            proveraUToku = false
        }
    }

    fun validirajQr(context: Context, token: String) {
        viewModelScope.launch {
            try {
                val dokument = firestore.collection("sobe_tokeni").document(token).get().await()
                val sobaToken = dokument.toObject(SobaToken::class.java)

                if (sobaToken == null) {
                    greskaSkeniranja = "Nepoznat QR kod. Obratite se recepciji."
                    sobaPotvrdjena = false
                    return@launch
                }
                if (!sobaToken.aktivan) {
                    greskaSkeniranja = "QR kod je istekao ili deaktiviran. Obratite se recepciji."
                    sobaPotvrdjena = false
                    return@launch
                }

                soba = sobaToken.broj
                sobaPotvrdjena = true
                greskaSkeniranja = ""
                sacuvajPoslednjiToken(context, token)
            } catch (e: Exception) {
                greskaSkeniranja = "Greška pri proveri QR koda. Proverite internet konekciju."
                sobaPotvrdjena = false
            }
        }
    }

    fun posaljiPrijavu(context: Context) {
        if (!sobaPotvrdjena) {
            _stanjeSlanja.value = StanjeSlanja.Greska("Morate skenirati validan QR kod sobe.")
            return
        }

        viewModelScope.launch {
            _stanjeSlanja.value = StanjeSlanja.UToku
            try {
                var slikaBase64 = ""
                if (slikaUri != null) {
                    slikaBase64 = kompresujSlikuUBase64(context, slikaUri!!)
                }

                val kvar = Kvar(
                    id = UUID.randomUUID().toString(),
                    tip = tip,
                    opis = opis,
                    soba = soba,
                    hitno = hitno,
                    slikaBase64 = slikaBase64
                )

                firestore.collection("kvarovi").document(kvar.id).set(kvar).await()
                sacuvajMojKvar(context, kvar.id)
                _stanjeSlanja.value = StanjeSlanja.Uspeh(kvar.id)
            } catch (e: Exception) {
                _stanjeSlanja.value = StanjeSlanja.Greska(e.message ?: "Greška pri slanju")
            }
        }
    }

    fun kompresujSlikuUBase64(context: Context, uri: Uri): String {
        val inputStream = context.contentResolver.openInputStream(uri)
        val originalBitmap = BitmapFactory.decodeStream(inputStream)
        inputStream?.close()

        val skaliran = Bitmap.createScaledBitmap(originalBitmap, 600, 600, true)
        val stream = ByteArrayOutputStream()
        skaliran.compress(Bitmap.CompressFormat.JPEG, 50, stream)

        return Base64.encodeToString(stream.toByteArray(), Base64.DEFAULT)
    }
}

fun kreirajTempUriZaSliku(context: Context): Uri {//priprema fajla za kameru
    val fajl = File.createTempFile("kvar_", ".jpg", context.cacheDir)
    return FileProvider.getUriForFile(context, "${context.packageName}.fileprovider", fajl)
}

sealed class StanjeSlanja {
    object Prazno : StanjeSlanja()
    object UToku : StanjeSlanja()
    data class Uspeh(val kvarId: String) : StanjeSlanja()
    data class Greska(val poruka: String) : StanjeSlanja()
}
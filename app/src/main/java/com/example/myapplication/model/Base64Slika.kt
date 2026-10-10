package com.example.myapplication.model

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap

@Composable
fun Base64Slika(base64: String, modifier: Modifier = Modifier) {
    if (base64.isBlank()) return
    val bytes = remember(base64) { android.util.Base64.decode(base64, android.util.Base64.DEFAULT) }
    val bitmap = remember(bytes) { BitmapFactory.decodeByteArray(bytes, 0, bytes.size) }
    Image(
        bitmap = bitmap.asImageBitmap(),
        contentDescription = "Slika kvara",
        modifier = modifier
    )
}
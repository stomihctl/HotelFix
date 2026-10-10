package com.example.myapplication.model

import android.graphics.Bitmap
import com.google.zxing.BarcodeFormat
import com.google.zxing.qrcode.QRCodeWriter

fun generisiQrBitmap(sadrzaj: String, velicina: Int = 512): Bitmap {
    val writer = QRCodeWriter()
    val bitMatrix = writer.encode(sadrzaj, BarcodeFormat.QR_CODE, velicina, velicina)
    val bitmap = Bitmap.createBitmap(velicina, velicina, Bitmap.Config.RGB_565)
    for (x in 0 until velicina) {
        for (y in 0 until velicina) {
            bitmap.setPixel(
                x, y,
                if (bitMatrix[x, y]) android.graphics.Color.BLACK else android.graphics.Color.WHITE
            )
        }
    }
    return bitmap
}
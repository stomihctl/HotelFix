package com.example.myapplication.util

import android.content.ContentValues
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Paint
import android.graphics.pdf.PdfDocument
import android.os.Build
import android.os.Environment
import android.provider.MediaStore
import java.io.ByteArrayOutputStream

fun sacuvajQrPdf(context: Context, qrBitmap: Bitmap, sobaBroj: String): Boolean {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(320, 420, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas

    val qrVelicina = 256
    val qrX = (320 - qrVelicina) / 2f
    val skaliranBitmap = Bitmap.createScaledBitmap(qrBitmap, qrVelicina, qrVelicina, false)
    canvas.drawBitmap(skaliranBitmap, qrX, 30f, null)

    val paint = Paint().apply {
        textAlign = Paint.Align.CENTER
        textSize = 22f
        isFakeBoldText = true
    }
    canvas.drawText("HotelFix — Soba $sobaBroj", 160f, 330f, paint)

    val paintMalo = Paint().apply {
        textAlign = Paint.Align.CENTER
        textSize = 14f
    }
    canvas.drawText("Skenirajte za prijavu kvara", 160f, 355f, paintMalo)

    pdfDocument.finishPage(page)

    val stream = ByteArrayOutputStream()
    pdfDocument.writeTo(stream)
    pdfDocument.close()

    val imeFajla = "QR_Soba_$sobaBroj.pdf"

    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, imeFajla)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            uri?.let {
                resolver.openOutputStream(it)?.use { out -> out.write(stream.toByteArray()) }
            }
            uri != null
        } else {
            val folder = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            val file = java.io.File(folder, imeFajla)
            file.writeBytes(stream.toByteArray())
            true
        }
    } catch (e: Exception) {
        false
    }
}

fun sacuvajIzvestajPdf(context: Context, statistika: com.example.myapplication.model.Statistika): Boolean {
    val pdfDocument = PdfDocument()
    val pageInfo = PdfDocument.PageInfo.Builder(595, 842, 1).create()
    val page = pdfDocument.startPage(pageInfo)
    val canvas = page.canvas

    val boje = com.example.myapplication.model.bojePaleta.map { it.toInt() }

    val naslovPaint = Paint().apply { textSize = 22f; isFakeBoldText = true }
    val podnaslovPaint = Paint().apply { textSize = 15f; isFakeBoldText = true }
    val tekstPaint = Paint().apply { textSize = 13f }

    var y = 45f
    canvas.drawText("HotelFix — Izveštaj o kvarovima", 40f, y, naslovPaint)
    y += 30f
    canvas.drawText("Ukupno: ${statistika.ukupnoKvarova}   Završeno: ${statistika.zavrseno}   Hitnih: ${statistika.hitnih}", 40f, y, tekstPaint)
    y += 35f

    // Pie chart  po tipu
    y = nacrtajPieSaLegendom(canvas, statistika.poTipu, "Po tipu kvara", 40f, y, boje, podnaslovPaint, tekstPaint)
    y += 25f

    // Pie chart  po majstoru
    if (statistika.poMajstoru.isNotEmpty()) {
        y = nacrtajPieSaLegendom(canvas, statistika.poMajstoru, "Angažovanost majstora", 40f, y, boje, podnaslovPaint, tekstPaint)
        y += 25f
    }

    // Bar chart  po danima
    if (statistika.poDanu.isNotEmpty()) {
        canvas.drawText("Kvarovi po danima", 40f, y, podnaslovPaint)
        y += 20f
        val maxVrednost = statistika.poDanu.maxOf { it.second }.coerceAtLeast(1)
        val barSirina = 30f
        val maxVisina = 100f
        var x = 40f
        val barPaint = Paint().apply { color = boje[0] }
        statistika.poDanu.forEach { (datum, broj) ->
            val visina = (broj.toFloat() / maxVrednost) * maxVisina
            canvas.drawRect(x, y + maxVisina - visina, x + barSirina - 4f, y + maxVisina, barPaint)
            canvas.drawText(broj.toString(), x, y + maxVisina - visina - 4f, tekstPaint)
            canvas.drawText(datum, x, y + maxVisina + 14f, tekstPaint)
            x += barSirina
        }
    }

    pdfDocument.finishPage(page)

    val stream = ByteArrayOutputStream()
    pdfDocument.writeTo(stream)
    pdfDocument.close()

    val imeFajla = "HotelFix_Izvestaj_${System.currentTimeMillis()}.pdf"

    return try {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            val resolver = context.contentResolver
            val values = ContentValues().apply {
                put(MediaStore.MediaColumns.DISPLAY_NAME, imeFajla)
                put(MediaStore.MediaColumns.MIME_TYPE, "application/pdf")
                put(MediaStore.MediaColumns.RELATIVE_PATH, Environment.DIRECTORY_DOWNLOADS)
            }
            val uri = resolver.insert(MediaStore.Downloads.EXTERNAL_CONTENT_URI, values)
            uri?.let { resolver.openOutputStream(it)?.use { out -> out.write(stream.toByteArray()) } }
            uri != null
        } else {
            val folder = context.getExternalFilesDir(Environment.DIRECTORY_DOWNLOADS)
            val file = java.io.File(folder, imeFajla)
            file.writeBytes(stream.toByteArray())
            true
        }
    } catch (e: Exception) {
        false
    }
}

private fun nacrtajPieSaLegendom(
    canvas: android.graphics.Canvas,
    podaci: Map<String, Int>,
    naslov: String,
    startX: Float,
    startY: Float,
    boje: List<Int>,
    naslovPaint: Paint,
    tekstPaint: Paint
): Float {
    canvas.drawText(naslov, startX, startY, naslovPaint)
    val y = startY + 15f
    val precnik = 100f
    val rect = android.graphics.RectF(startX, y, startX + precnik, y + precnik)

    val ukupno = podaci.values.sum().toFloat()
    var pocetniUgao = -90f
    val paint = Paint()

    podaci.entries.forEachIndexed { index, (_, vrednost) ->
        val ugao = (vrednost / ukupno) * 360f
        paint.color = boje[index % boje.size]
        canvas.drawArc(rect, pocetniUgao, ugao, true, paint)
        pocetniUgao += ugao
    }

    var legendaY = y + 14f
    val legendaX = startX + precnik + 20f
    podaci.entries.forEachIndexed { index, (naziv, vrednost) ->
        paint.color = boje[index % boje.size]
        canvas.drawRect(legendaX, legendaY - 10f, legendaX + 10f, legendaY, paint)
        canvas.drawText("$naziv ($vrednost)", legendaX + 16f, legendaY, tekstPaint)
        legendaY += 18f
    }

    return y + precnik
}
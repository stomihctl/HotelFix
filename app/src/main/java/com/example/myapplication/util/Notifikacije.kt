package com.example.myapplication.util

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat

private const val KANAL_ID = "hotelfix_kanal"
private var brojac = 0

fun napraviNotifikacioniKanal(context: Context) {
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        val kanal = NotificationChannel(
            KANAL_ID,
            "HotelFix obavestenja",
            NotificationManager.IMPORTANCE_HIGH //na vrh ekrana
        )
        val manager = context.getSystemService(NotificationManager::class.java)
        manager.createNotificationChannel(kanal) // registruje ga u sistemu pri pokretanju aplikacije
    }
}

fun posaljiNotifikaciju(context: Context, naslov: String, tekst: String) {
    val notifikacija = NotificationCompat.Builder(context, KANAL_ID)
        .setSmallIcon(android.R.drawable.ic_dialog_info) // obavezna mala ikonica
        .setContentTitle(naslov)                         // naslov notifikacije
        .setContentText(tekst)                           // tekst notifikacije
        .setPriority(NotificationCompat.PRIORITY_HIGH)
        .setAutoCancel(true)                             // brise se
        .build()

    try {
        NotificationManagerCompat.from(context).notify(brojac++, notifikacija) // prikaz
    } catch (e: SecurityException) {
        // permisija nije odobrena
    }
}
package com.sultlab.murmur

import android.app.Application
import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.os.Build
import com.google.firebase.messaging.FirebaseMessaging
import org.koin.android.ext.koin.androidContext

class MurmurApp : Application() {
    override fun onCreate() {
        super.onCreate()

        FirebaseMessaging.getInstance().token
        createNotificationChannel(applicationContext)
        initKoin {
            androidContext(this@MurmurApp)
        }
    }

    fun createNotificationChannel(context: Context) {

        val channel = NotificationChannel(
            "murmur_default",
            "murmur",
            NotificationManager.IMPORTANCE_DEFAULT
        )

        val manager =
            context.getSystemService(
                NotificationManager::class.java
            )

        manager.createNotificationChannel(channel)
    }
}
package com.sultlab.murmur.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.work.Constraints
import androidx.work.ExistingWorkPolicy
import androidx.work.NetworkType
import androidx.work.OneTimeWorkRequestBuilder
import androidx.work.WorkManager
import androidx.work.workDataOf
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sultlab.murmur.MainActivity
import com.sultlab.murmur.R
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.koin.core.component.KoinComponent
import org.koin.core.component.inject
import androidx.core.net.toUri


class MurmurFcmService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()
        Log.d("FCM", "onNewToken: $token")
        val work = OneTimeWorkRequestBuilder<PushTokenSyncWorker>()
            .setConstraints(constraints)
            .setInputData(
                workDataOf(
                    PushTokenSyncWorker.KEY_TOKEN to token
                )
            )
            .build()

        WorkManager
            .getInstance(applicationContext)
            .enqueueUniqueWork(
                "push-token-sync",
                ExistingWorkPolicy.REPLACE,
                work
            )

    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        val title =
            message.notification?.title
                ?: message.data["title"]
                ?: return

        val body =
            message.notification?.body
                ?: message.data["body"]
                ?: return

        val postId =
            message.data["post_id"]

        showNotification(
            title = title,
            body = body,
            postId = postId
        )
    }


    private fun showNotification(
        title: String,
        body: String,
        postId: String?
    ) {

        val channelId = "murmur_default"

        val intent = Intent(
            Intent.ACTION_VIEW,
            "murmur://post/$postId".toUri(),
            this,
            MainActivity::class.java
        )

        val pendingIntent =
            PendingIntent.getActivity(
                this,
                postId?.hashCode() ?: 0,
                intent,
                PendingIntent.FLAG_UPDATE_CURRENT or
                        PendingIntent.FLAG_IMMUTABLE
            )

        val notification =
            NotificationCompat.Builder(
                this,
                channelId
            )
                .setSmallIcon(
                    R.drawable.icon
                )
                .setContentTitle(title)
                .setContentText(body)
                .setAutoCancel(true)
                .setContentIntent(pendingIntent)
                .build()

        val id =
            postId?.hashCode()
                ?: System.currentTimeMillis().toInt()

        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            // TODO: Consider calling
            //    ActivityCompat#requestPermissions
            // here to request the missing permissions, and then overriding
            //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
            //                                          int[] grantResults)
            // to handle the case where the user grants the permission. See the documentation
            // for ActivityCompat#requestPermissions for more details.
            return
        }
        NotificationManagerCompat
            .from(this)
            .notify(id, notification)
    }
}

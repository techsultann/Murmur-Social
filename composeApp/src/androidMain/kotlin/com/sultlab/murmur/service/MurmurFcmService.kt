package com.sultlab.murmur.service

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import androidx.core.net.toUri
import androidx.work.*
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.sultlab.murmur.MainActivity
import com.sultlab.murmur.R

class MurmurFcmService : FirebaseMessagingService() {

    // ── Token refresh ─────────────────────────────────────────

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCM", "onNewToken: $token")

        val constraints = Constraints.Builder()
            .setRequiredNetworkType(NetworkType.CONNECTED)
            .build()

        val work = OneTimeWorkRequestBuilder<PushTokenSyncWorker>()
            .setConstraints(constraints)
            .setInputData(workDataOf(PushTokenSyncWorker.KEY_TOKEN to token))
            .build()

        WorkManager.getInstance(applicationContext)
            .enqueueUniqueWork(
                "push-token-sync",
                ExistingWorkPolicy.REPLACE,
                work,
            )
    }

    // ── Message received ──────────────────────────────────────

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)

        // Support both notification payload (FCM foreground/background)
        // and data-only payload (FCM data messages)
        val title = message.notification?.title ?: message.data["title"] ?: return
        val body  = message.notification?.body  ?: message.data["body"]  ?: return
        val data  = message.data

        val type    = data["type"] ?: "general"
        val postId  = data["post_id"]
        val groupId = data["group_id"]

        Log.d("FCM", "onMessageReceived: type=$type groupId=$groupId postId=$postId")

        when (type) {
            // ── Existing: post activity ──────────────────────
            "post_like",
            "post_comment"  -> showPostNotification(title, body, postId)

            // ── New: group notifications ─────────────────────
            "group_message" -> showGroupMessageNotification(title, body, groupId)
            "join_request"  -> showJoinRequestNotification(title, body, groupId)
            "member_joined" -> showMemberJoinedNotification(title, body, groupId)

            // ── Fallback ─────────────────────────────────────
            else  -> showPostNotification(title, body, postId)
        }
    }

    // ── Post like / comment ───────────────────────────────────
    // Preserved exactly from your original implementation,
    // just extracted into a named function.

    private fun showPostNotification(
        title: String,
        body: String,
        postId: String?,
    ) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            "murmur://post/$postId".toUri(),
            this,
            MainActivity::class.java,
        )

        showNotification(
            id            = postId?.hashCode() ?: System.currentTimeMillis().toInt(),
            title         = title,
            body          = body,
            channelId     = CHANNEL_POST_ACTIVITY,
            channelName   = "Post activity",
            importance    = NotificationManager.IMPORTANCE_DEFAULT,
            pendingIntent = buildPendingIntent(postId?.hashCode() ?: 0, intent),
        )
    }

    // ── Group message ─────────────────────────────────────────

    private fun showGroupMessageNotification(
        title: String,
        body: String,
        groupId: String?,
    ) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            "murmur://group/$groupId".toUri(),
            this,
            MainActivity::class.java,
        )

        // Use groupId as notification ID so rapid messages in the same
        // group update the same notification rather than stacking
        showNotification(
            id            = groupId.hashCode(),
            title         = title,
            body          = body,
            channelId     = CHANNEL_GROUP_MESSAGES,
            channelName   = "Group messages",
            importance    = NotificationManager.IMPORTANCE_DEFAULT,
            pendingIntent = buildPendingIntent(groupId.hashCode(), intent),
        )
    }

    // ── Join request (high importance — admin action needed) ──

    private fun showJoinRequestNotification(
        title: String,
        body: String,
        groupId: String?,
    ) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            "murmur://group/$groupId/members".toUri(),
            this,
            MainActivity::class.java,
        )

        showNotification(
            id            = "join_$groupId".hashCode(),
            title         = title,
            body          = body,
            channelId     = CHANNEL_JOIN_REQUESTS,
            channelName   = "Join requests",
            importance    = NotificationManager.IMPORTANCE_HIGH,
            pendingIntent = buildPendingIntent("join_$groupId".hashCode(), intent),
        )
    }

    // ── Member joined (low importance — informational) ────────

    private fun showMemberJoinedNotification(
        title: String,
        body: String,
        groupId: String?,
    ) {
        val intent = Intent(
            Intent.ACTION_VIEW,
            "murmur://group/$groupId/members".toUri(),
            this,
            MainActivity::class.java,
        )

        showNotification(
            id            = "member_$groupId".hashCode(),
            title         = title,
            body          = body,
            channelId     = CHANNEL_MEMBER_UPDATES,
            channelName   = "Member updates",
            importance    = NotificationManager.IMPORTANCE_LOW,
            pendingIntent = buildPendingIntent("member_$groupId".hashCode(), intent),
        )
    }

    // ── Core builder ──────────────────────────────────────────

    private fun showNotification(
        id: Int,
        title: String,
        body: String,
        channelId: String,
        channelName: String,
        importance: Int,
        pendingIntent: PendingIntent,
    ) {
        if (ActivityCompat.checkSelfPermission(
                this,
                Manifest.permission.POST_NOTIFICATIONS,
            ) != PackageManager.PERMISSION_GRANTED
        ) return

        // Create channel (safe to call repeatedly — no-op if already exists)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(channelId, channelName, importance)
            getSystemService(NotificationManager::class.java)
                .createNotificationChannel(channel)
        }

        val priority = when (importance) {
            NotificationManager.IMPORTANCE_HIGH -> NotificationCompat.PRIORITY_HIGH
            NotificationManager.IMPORTANCE_LOW  -> NotificationCompat.PRIORITY_LOW
            else                                -> NotificationCompat.PRIORITY_DEFAULT
        }

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(R.drawable.app_icon)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(priority)
            .build()

        NotificationManagerCompat.from(this).notify(id, notification)
    }

    private fun buildPendingIntent(requestCode: Int, intent: Intent): PendingIntent =
        PendingIntent.getActivity(
            this,
            requestCode,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE,
        )

    // ── Channel IDs ───────────────────────────────────────────

    companion object {
        const val CHANNEL_POST_ACTIVITY  = "murmur_post_activity"
        const val CHANNEL_GROUP_MESSAGES = "murmur_group_messages"
        const val CHANNEL_JOIN_REQUESTS  = "murmur_join_requests"
        const val CHANNEL_MEMBER_UPDATES = "murmur_member_updates"
    }
}
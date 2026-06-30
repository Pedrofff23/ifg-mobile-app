package com.example.gymapp.data.notification

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.core.app.NotificationCompat
import com.example.gymapp.MainActivity
import com.example.gymapp.data.local.TokenManager
import com.example.gymapp.data.remote.ErpService
import com.example.gymapp.data.remote.FCMTokenRequest
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import javax.inject.Inject

@AndroidEntryPoint
class GymFirebaseMessagingService : FirebaseMessagingService() {

    @Inject
    lateinit var erpService: ErpService

    @Inject
    lateinit var tokenManager: TokenManager

    private val serviceScope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("FCMService", "Refreshed token: $token")
        
        serviceScope.launch {
            val accessToken = tokenManager.getAccessTokenSync()
            if (!accessToken.isNullOrEmpty()) {
                try {
                    erpService.storeFCMToken(FCMTokenRequest(token, android.os.Build.MODEL))
                    Log.d("FCMService", "FCM Token registered successfully on backend")
                } catch (e: Exception) {
                    Log.e("FCMService", "Failed to register FCM Token on backend: ${e.message}")
                }
            }
        }
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        Log.d("FCMService", "From: ${message.from}")

        val title = message.notification?.title ?: message.data["title"] ?: "Aviso da Academia"
        val body = message.notification?.body ?: message.data["body"] ?: ""
        val announcementId = message.data["announcement_id"]

        sendNotification(title, body, announcementId)
    }

    private fun sendNotification(title: String, body: String, announcementId: String?) {
        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
            if (announcementId != null) {
                putExtra("announcement_id", announcementId)
            }
        }

        val pendingIntent = PendingIntent.getActivity(
            this, 0, intent,
            PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE
        )

        val defaultIcon = applicationInfo.icon

        val notificationBuilder = NotificationCompat.Builder(this, "announcements")
            .setSmallIcon(defaultIcon)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_DEFAULT)

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(System.currentTimeMillis().toInt(), notificationBuilder.build())
    }
}

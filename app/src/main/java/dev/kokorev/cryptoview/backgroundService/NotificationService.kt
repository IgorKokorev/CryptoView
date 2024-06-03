package dev.kokorev.cryptoview.backgroundService

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Parcelable
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat.getSystemService
import dev.kokorev.cryptoview.Constants
import dev.kokorev.cryptoview.R
import dev.kokorev.cryptoview.views.MainActivity

// Service for push notifications
class NotificationService(val context: Context) {
    private val CHANNEL_ID = "coin_view_channel"
    private val CHANNEL_NAME = "CoinView"
    private val CHANNEL_DESCRIPTION = "CoinView Notification channel"
    private val importance = NotificationManager.IMPORTANCE_DEFAULT
    private val icon = R.drawable.icon_coin_light

    private val notificationManager: NotificationManager?


    init {
        notificationManager = getSystemService(context, NotificationManager::class.java)
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, importance).apply {
            description = CHANNEL_DESCRIPTION
        }
        notificationManager?.createNotificationChannel(channel)
    }

    fun sendNotification(id: Int, title: String, text: String, pendingIntent: PendingIntent) {

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(icon)
            .setContentTitle(title)
            .setContentText(text)
            .setContentIntent(pendingIntent)
            .setShowWhen(true)
            .setAutoCancel(true)
            .setOnlyAlertOnce(true)
            .build()

        notificationManager?.notify(id, notification)
    }

    fun send(title: String, text: String, extra: Parcelable, id: Int = Constants.NOTIFICATION_ID) {
        val intent = Intent(context, MainActivity::class.java)
        intent.putExtra(Constants.INTENT_EXTRA_FAVORITE_COIN, extra)
        intent.action = id.toString()
        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
        )

        sendNotification(id, title, text, pendingIntent)
    }
}

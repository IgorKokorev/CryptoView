package dev.kokorev.cryptoview.backgroundService

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dev.kokorev.cryptoview.appDagger
import dev.kokorev.cryptoview.logd
import dev.kokorev.cryptoview.utils.NotificationData
import dev.kokorev.cryptoview.utils.NotificationService
import javax.inject.Inject

class PortfolioNotificationReceiver: BroadcastReceiver() {
    @Inject lateinit var notificationService: NotificationService
    init { appDagger.inject(this) }
    
    override fun onReceive(context: Context?, intent: Intent?) {
        val sb = StringBuilder().apply {
            append("Action: ${intent?.action}\n")
            append("URI: ${intent?.toUri(Intent.URI_INTENT_SCHEME)}\n")
            toString().also { log ->
                logd(log)
            }
        }
        val data = NotificationData(
            title = "PortfolioNotificationReceiver",
            text = sb.toString(),
        )
        notificationService.send(data)
    }
}
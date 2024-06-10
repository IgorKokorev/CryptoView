package dev.kokorev.cryptoview.backgroundService

import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import androidx.core.content.ContextCompat.getSystemService
import dev.kokorev.cryptoview.logd
import java.time.Instant

class AlarmScheduler(val context: Context) {
    companion object {
        val PORTFOLIO_EVALUATION_CLASS = PortfolioEvaluationService::class.java
        val PORTFOLIO_NOTIFICATION_CLASS = PortfolioNotificationReceiver::class.java
        
        private const val PORTFOLIO_EVALUATION_SERVICE_ACTION: String = "portfolio_evaluation_action"
        private const val PORTFOLIO_EVALUATION_SERVICE_REQUEST_ID: Int = 1
        private const val PORTFOLIO_NOTIFICATION_BROADCAST_ACTION: String = "portfolio_notification_action"
        private const val PORTFOLIO_NOTIFICATION_BROADCAST_REQUEST_ID: Int = 2
        
        val portfolioEvaluationData = AlarmData(
            PORTFOLIO_EVALUATION_SERVICE_REQUEST_ID,
            PORTFOLIO_EVALUATION_SERVICE_ACTION,
            0L,
            AlarmManager.INTERVAL_FIFTEEN_MINUTES,
            PORTFOLIO_EVALUATION_CLASS
        )
        
        val portfolioNotificationData = AlarmData(
            PORTFOLIO_NOTIFICATION_BROADCAST_REQUEST_ID,
            PORTFOLIO_NOTIFICATION_BROADCAST_ACTION,
            0L,
            AlarmManager.INTERVAL_DAY,
            PORTFOLIO_NOTIFICATION_CLASS
        )
    }
    val alarmManager = getSystemService(context, AlarmManager::class.java)
    
    fun createPendingIntent(alarmData: AlarmData): PendingIntent {
        logd("createPendingIntent")
        val intent = Intent(context, alarmData.cls).apply {
            action = alarmData.action
        }
        
        val pendingIntent = when (alarmData.cls) {
            PORTFOLIO_EVALUATION_CLASS -> {
                logd("PortfolioEvaluationService -> starting Service")
                PendingIntent.getService(
                    context,
                    alarmData.id,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }
            PORTFOLIO_NOTIFICATION_CLASS -> {
                logd("PortfolioNotificationReceiver -> starting Broadcast")
                PendingIntent.getBroadcast(
                    context,
                    alarmData.id,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }
            else -> {
                logd("else -> starting Activity")
                PendingIntent.getActivity(
                    context,
                    alarmData.id,
                    intent,
                    PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
                )
            }
        }
        
        return pendingIntent
    }
    
    fun schedule(alarmData: AlarmData) {
        logd("scheduleAlarm, action: ${alarmData.action}")
        
        if (alarmManager == null) {
            logd("Alarm manager can't be instantiated")
            return
        }
        logd("schedule: setting alarm at ${Instant.ofEpochMilli(alarmData.time)}")

        alarmManager.setInexactRepeating(
            AlarmManager.RTC,
            alarmData.time,
            alarmData.period,
            createPendingIntent(alarmData)
        )
    }
    
    fun cancel(alarmData: AlarmData) {
        logd("cancel Alarm, action: ${alarmData.action}")
        if (alarmManager == null) {
            logd("Alarm manager can't be instantiated")
            return
        }
        
        alarmManager.cancel(createPendingIntent(alarmData))
    }
}
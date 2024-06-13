package dev.kokorev.cryptoview.viewModel

import androidx.lifecycle.ViewModel
import dev.kokorev.cryptoview.appDagger
import dev.kokorev.cryptoview.backgroundService.AlarmScheduler
import javax.inject.Inject

class SettingsViewModel : ViewModel() {
    @Inject lateinit var alarmScheduler: AlarmScheduler
    init { appDagger.inject(this) }
    
    fun cancelPortfolioNotifications() {
        alarmScheduler.cancel(AlarmScheduler.portfolioNotificationData)
    }
}
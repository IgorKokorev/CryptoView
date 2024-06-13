package dev.kokorev.cryptoview.viewModel

import androidx.lifecycle.ViewModel
import dev.kokorev.cryptoview.appDagger
import dev.kokorev.cryptoview.backgroundService.AlarmScheduler
import dev.kokorev.cryptoview.domain.RemoteApi
import dev.kokorev.cryptoview.domain.Repository
import dev.kokorev.cryptoview.utils.NotificationService
import javax.inject.Inject

class ActivityViewModel: ViewModel() {
    @Inject lateinit var remoteApi: RemoteApi
    @Inject lateinit var repository: Repository
    @Inject lateinit var notificationService: NotificationService
    @Inject lateinit var alarmScheduler: AlarmScheduler


    init {
        appDagger.inject(this)
    }
}
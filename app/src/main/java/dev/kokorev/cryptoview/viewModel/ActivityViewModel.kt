package dev.kokorev.cryptoview.viewModel

import androidx.lifecycle.ViewModel
import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.backgroundService.NotificationService
import dev.kokorev.cryptoview.domain.RemoteApi
import dev.kokorev.cryptoview.domain.Repository
import javax.inject.Inject

class ActivityViewModel: ViewModel() {
    @Inject
    lateinit var remoteApi: RemoteApi
    @Inject
    lateinit var repository: Repository
    @Inject
    lateinit var notificationService: NotificationService


    init {
        App.instance.dagger.inject(this)

    }
}
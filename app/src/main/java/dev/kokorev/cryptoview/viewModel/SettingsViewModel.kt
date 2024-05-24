package dev.kokorev.cryptoview.viewModel

import androidx.lifecycle.ViewModel
import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.backgroundService.NotificationManager
import dev.kokorev.cryptoview.data.PreferenceProvider
import dev.kokorev.cryptoview.domain.Repository
import javax.inject.Inject

class SettingsViewModel : ViewModel() {
    @Inject
    lateinit var repository: Repository
    @Inject
    lateinit var preferences: PreferenceProvider
    @Inject
    lateinit var notificationManager: NotificationManager

    init {
        App.instance.dagger.inject(this)
    }
}
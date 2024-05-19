package dev.kokorev.cryptoview.viewModel

import androidx.lifecycle.ViewModel
import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.domain.Repository
import javax.inject.Inject

class SettingsViewModel : ViewModel() {
    @Inject
    lateinit var repository: Repository

    init {
        App.instance.dagger.inject(this)
    }
}
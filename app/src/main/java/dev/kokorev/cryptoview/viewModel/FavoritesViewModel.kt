package dev.kokorev.cryptoview.viewModel

import androidx.lifecycle.ViewModel
import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.domain.RemoteApi
import javax.inject.Inject

class FavoritesViewModel : ViewModel() {
    @Inject
    lateinit var remoteApi: RemoteApi

    init {
        App.instance.dagger.inject(this)
    }
}
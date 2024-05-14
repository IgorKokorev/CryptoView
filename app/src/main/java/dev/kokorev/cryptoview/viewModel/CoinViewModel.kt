package dev.kokorev.cryptoview.viewModel

import androidx.lifecycle.ViewModel
import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.domain.RemoteApi
import dev.kokorev.cryptoview.domain.Repository
import javax.inject.Inject

class CoinViewModel : ViewModel() {
    @Inject
    lateinit var remoteApi: RemoteApi
    @Inject
    lateinit var repository: Repository

    lateinit var coinPaprikaId: String
    lateinit var symbol: String
    lateinit var name: String

    init {
        App.instance.dagger.inject(this)
    }
}
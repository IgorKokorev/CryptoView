package dev.kokorev.cryptoview.viewModel

import androidx.lifecycle.ViewModel
import com.coinpaprika.apiclient.entity.CoinDetailsEntity
import dev.kokorev.cmc_api.entity.cmc_metadata.CmcCoinDataDTO
import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.domain.RemoteApi
import dev.kokorev.cryptoview.domain.Repository
import javax.inject.Inject

class BinanceViewModel : ViewModel() {
    @Inject
    lateinit var remoteApi: RemoteApi
    @Inject
    lateinit var repository: Repository

    init {
        App.instance.dagger.inject(this)
    }
}
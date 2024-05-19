package dev.kokorev.cryptoview.viewModel

import androidx.lifecycle.ViewModel
import com.coinpaprika.apiclient.entity.CoinDetailsEntity
import dev.kokorev.cmc_api.entity.cmc_metadata.CmcCoinDataDTO
import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.domain.RemoteApi
import dev.kokorev.cryptoview.domain.Repository
import io.reactivex.rxjava3.subjects.BehaviorSubject
import javax.inject.Inject

class CoinViewModel : ViewModel() {

    @Inject
    lateinit var remoteApi: RemoteApi
    @Inject
    lateinit var repository: Repository

    lateinit var coinPaprikaId: String
    lateinit var symbol: String
    lateinit var name: String

    var cmcInfo: CmcCoinDataDTO? = null
    var cpInfo: CoinDetailsEntity? = null


    init {
        App.instance.dagger.inject(this)
    }
}
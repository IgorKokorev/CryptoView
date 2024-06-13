package dev.kokorev.cryptoview.viewModel

import androidx.lifecycle.ViewModel
import dev.kokorev.binance_api.entity.BinanceKLineInterval
import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.Constants
import dev.kokorev.cryptoview.domain.RemoteApi
import dev.kokorev.cryptoview.domain.Repository
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class BinanceViewModel : ViewModel() {
    
    
    @Inject
    lateinit var remoteApi: RemoteApi
    @Inject
    lateinit var repository: Repository

    init {
        App.instance.dagger.inject(this)
    }
    
    fun getBinance24hrStats(symbol: String) = remoteApi.getBinance24hrStats(symbol)
    fun getBinanceKLines(symbol: String, interval: BinanceKLineInterval): Single<ArrayList<ArrayList<Any>>> {
        return remoteApi.getBinanceKLines(symbol = symbol, interval = interval, limit = Constants.BINANCE_KLINES_LIMIT)
    }
    
    fun findBinanceSymbolsByBaseAsset(symbol: String) = repository.findBinanceSymbolsByBaseAsset(symbol)
}
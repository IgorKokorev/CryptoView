package dev.kokorev.cryptoview.viewModel

import androidx.lifecycle.ViewModel
import com.coinpaprika.apiclient.entity.CoinDetailsEntity
import com.coinpaprika.apiclient.entity.FavoriteCoinDB
import dev.kokorev.cmc_api.entity.cmc_metadata.CmcCoinDataDTO
import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.domain.RemoteApi
import dev.kokorev.cryptoview.domain.Repository
import dev.kokorev.cryptoview.utils.Converter
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
    
    fun getPricePrediction() = remoteApi.getPricePrediction(symbol = symbol)
    fun getAIReport() = remoteApi.getAIReport(symbol)
    fun getCoinPaprikaTickerHistorical() = remoteApi.getCoinPaprikaTickerHistorical(coinPaprikaId)
    fun getCoinPaprikaTicker() = remoteApi.getCoinPaprikaTicker(coinPaprikaId)
    fun getCmcMetadata() = remoteApi.getCmcMetadata(symbol)
    fun getCoinPaprikaCoinInfo() = remoteApi.getCoinPaprikaCoinInfo(coinPaprikaId)
    
    fun saveRecent(cpCoin: CoinDetailsEntity) {
        val recentCoinDB = Converter.CoinDetailsEntityToRecentCoinDB(cpCoin)
        repository.saveRecent(recentCoinDB)
    }
    fun getPortfolioPositionByCPId() = repository.getPortfolioPositionByCPId(coinPaprikaId)
    fun deleteFavorite() = repository.deleteFavorite(coinPaprikaId)
    fun saveFavorite(cpCoin: CoinDetailsEntity) {
        val favoriteCoinDB: FavoriteCoinDB = Converter.CoinDetailsEntityToFavoriteCoinDB(cpCoin)
        repository.saveFavorite(favoriteCoinDB)
    }
    fun findFavoriteCoinByCoinPaprikaId() = repository.findFavoriteCoinByCoinPaprikaId(coinPaprikaId)
    
}
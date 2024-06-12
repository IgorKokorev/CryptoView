package dev.kokorev.cryptoview.viewModel

import androidx.lifecycle.ViewModel
import com.coinpaprika.apiclient.entity.FavoriteCoinDB
import com.coinpaprika.apiclient.entity.PortfolioPositionDB
import com.coinpaprika.apiclient.entity.RecentCoinDB
import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.domain.Repository
import dev.kokorev.room_db.core_api.entity.CoinPaprikaTickerDB
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import javax.inject.Inject

class SavedViewModel : ViewModel() {
    @Inject
    lateinit var repository: Repository

    private var disposable: Disposable? = null
    
    val favorites: Observable<List<FavoriteCoinDB>>
    val recents: Observable<List<RecentCoinDB>>
    val portfolio: Observable<List<PortfolioPositionDB>>
    val tickers: Observable<List<CoinPaprikaTickerDB>>

    init {
        App.instance.dagger.inject(this)
        favorites = repository.getFavoriteCoins()
        recents = repository.getRecentCoins()
        tickers = repository.getAllCoinPaprikaTickers()
        portfolio = repository.getAllPortfolioPositions()
    }
    override fun onCleared() {
        super.onCleared()
        disposable?.dispose()
    }
}
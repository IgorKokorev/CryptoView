package dev.kokorev.cryptoview.viewModel

import androidx.lifecycle.ViewModel
import com.coinpaprika.apiclient.entity.PortfolioEvaluationDB
import com.coinpaprika.apiclient.entity.PortfolioPositionDB
import dev.kokorev.cryptoview.appDagger
import dev.kokorev.cryptoview.domain.Repository
import dev.kokorev.room_db.core_api.entity.CoinPaprikaTickerDB
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import javax.inject.Inject

class PortfolioPerformanceViewModel : ViewModel() {
    @Inject
    lateinit var repository: Repository
    val portfolioEvaluations: Maybe<List<PortfolioEvaluationDB>>
    
    val portfolio: Observable<List<PortfolioPositionDB>>
    val tickers: Observable<List<CoinPaprikaTickerDB>>
    
    init {
        appDagger.inject(this)
        portfolioEvaluations = repository.getAllPortfolioEvaluationsMaybe()
        tickers = repository.getAllCoinPaprikaTickers()
        portfolio = repository.getAllPortfolioPositions()
    }
}
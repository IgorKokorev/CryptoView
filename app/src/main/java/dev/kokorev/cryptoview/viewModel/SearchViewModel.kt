package dev.kokorev.cryptoview.viewModel

import androidx.lifecycle.ViewModel
import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.Constants
import dev.kokorev.cryptoview.data.preferencesLong
import dev.kokorev.cryptoview.domain.RemoteApi
import dev.kokorev.cryptoview.domain.Repository
import dev.kokorev.cryptoview.utils.Converter
import dev.kokorev.room_db.core_api.entity.CoinPaprikaTickerDB
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

class SearchViewModel : ViewModel() {
    @Inject
    lateinit var remoteApi: RemoteApi
    @Inject
    lateinit var repository: Repository
    
    private val compositeDisposable = CompositeDisposable()
    
    var allTickers: Observable<List<CoinPaprikaTickerDB>>
    
    private var minMcap: Long by preferencesLong("minMcap")
    private var minVol: Long by preferencesLong("minVol")
    private var cpTickersTime: Long by preferencesLong("cpTickersTime")

    init {
        App.instance.dagger.inject(this)
        allTickers = repository.getAllCoinPaprikaTickersFiltered(minMcap, minVol)
        loadTickers()
    }

    fun loadTickers() {
        // If enough time pasts call the API
        if (System.currentTimeMillis() > (cpTickersTime + Constants.CP_TICKERS_UPDATE_INTERVAL)) {
            cpTickersTime = 0L

            val disposable = remoteApi.getCoinPaprikaTickers()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnSuccess {
                    val tickers = it
                        .map { dto -> Converter.dtoToCoinPaprikaTicker(dto) }
                        .toList()
                    repository.saveCoinPaprikaTickers(tickers)
                }
                .doOnError {

                }
                .subscribe()

            compositeDisposable.add(disposable)
        }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }

}
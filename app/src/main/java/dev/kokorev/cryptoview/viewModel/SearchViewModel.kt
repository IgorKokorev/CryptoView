package dev.kokorev.cryptoview.viewModel

import android.util.Log
import androidx.lifecycle.ViewModel
import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.Constants
import dev.kokorev.cryptoview.data.sharedPreferences.KEY_CP_TICKERS_CALL_TIME
import dev.kokorev.cryptoview.data.sharedPreferences.KEY_MIN_MCAP
import dev.kokorev.cryptoview.data.sharedPreferences.KEY_MIN_VOL
import dev.kokorev.cryptoview.data.sharedPreferences.preferencesInstant
import dev.kokorev.cryptoview.data.sharedPreferences.preferencesLong
import dev.kokorev.cryptoview.domain.RemoteApi
import dev.kokorev.cryptoview.domain.Repository
import dev.kokorev.cryptoview.utils.Converter
import dev.kokorev.room_db.core_api.entity.CoinPaprikaTickerDB
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import java.time.Instant
import javax.inject.Inject

class SearchViewModel : ViewModel() {
    @Inject
    lateinit var remoteApi: RemoteApi
    
    @Inject
    lateinit var repository: Repository
    
    private val compositeDisposable = CompositeDisposable()
    
    var allTickers: Observable<List<CoinPaprikaTickerDB>>
    
    private var minMcap: Long by preferencesLong(KEY_MIN_MCAP)
    private var minVol: Long by preferencesLong(KEY_MIN_VOL)
    private var cpTickersTime: Instant by preferencesInstant(KEY_CP_TICKERS_CALL_TIME)
    
    init {
        App.instance.dagger.inject(this)
        allTickers = repository.getAllCoinPaprikaTickersFiltered(minMcap, minVol)
        loadTickers()
    }
    
    fun loadTickers() {
        val now = Instant.now()
        if (now.isAfter(cpTickersTime.plusSeconds(Constants.CP_TICKERS_UPDATE_SECONDS))) {
            
            val disposable = remoteApi.getCoinPaprikaTickers()
                .doOnSuccess { list ->
                    val tickers = list
                        .map { dto -> Converter.dtoToCoinPaprikaTicker(dto) }
                        .toList()
                    repository.saveCoinPaprikaTickers(tickers)
                    cpTickersTime = now
                }
                .doOnError {
                    Log.d(
                        this.javaClass.simpleName,
                        "getCoinPaprikaTickers error: ${it.localizedMessage}, ${it.stackTrace}"
                    )
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
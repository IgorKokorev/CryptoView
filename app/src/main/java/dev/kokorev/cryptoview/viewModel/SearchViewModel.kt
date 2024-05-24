package dev.kokorev.cryptoview.viewModel

import androidx.lifecycle.ViewModel
import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.data.Constants
import dev.kokorev.cryptoview.data.PreferenceProvider
import dev.kokorev.cryptoview.domain.RemoteApi
import dev.kokorev.cryptoview.domain.Repository
import dev.kokorev.cryptoview.utils.Converter
import dev.kokorev.cryptoview.views.fragments.Sorting
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
    @Inject
    lateinit var preferences: PreferenceProvider
    private val compositeDisposable = CompositeDisposable()
    val cpTickers: Observable<List<CoinPaprikaTickerDB>>
    var sorting = Sorting.NONE // field for RV sorting
    var direction = 1 // sorting direction

    init {
        App.instance.dagger.inject(this)
        cpTickers = repository.getAllCoinPaprikaTickersFiltered(preferences.getMinMcap(), preferences.getMinVol())
        loadTickers()
    }

    fun loadTickers() {
        val lastTime = preferences.getLastCpTickersCallTime()
        // If enough time pasts call the API
        if (System.currentTimeMillis() > (lastTime + Constants.CP_TICKERS_UPDATE_INTERVAL)) {
            preferences.saveLastCpTickersCallTime()

            val disposable = remoteApi.getCoinPaprikaTickers()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .doOnSuccess {
                    val tickers = it
                        .map { dto -> Converter.dtoToCoinPaprikaTicker(dto) }
                        .toList()
                    repository.addCoinPaprikaTickers(tickers)
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
package dev.kokorev.cryptoview.viewModel

import androidx.lifecycle.ViewModel
import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.Constants
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
    private val compositeDisposable = CompositeDisposable()
    val cpTickers: Observable<List<CoinPaprikaTickerDB>>
    var sorting = Sorting.NONE // field for RV sorting
    var direction = 1 // sorting direction
//    val showProgressBar: BehaviorSubject<Boolean>

    init {
        App.instance.dagger.inject(this)
//        showProgressBar = interactor.progressBarState
        cpTickers = repository.getAllCoinPaprikaTickers()
        loadTickers()
    }

    fun loadTickers() {
        val lastTime = repository.getLastCpTickersCallTime()
        // If enough time pasts call the API
        if (System.currentTimeMillis() > (lastTime + Constants.CP_TICKERS_CALL_INTERVAL)) {
            repository.saveLastCpTickersCallTime()

            val disposable = remoteApi.getCoinPaprikaTickers()
                .observeOn(Schedulers.io())
                .subscribe {
                    val tickers = it
                        .map { dto -> Converter.dtoToCoinPaprikaTicker(dto) }
                        .toList()
                    repository.addCoinPaprikaTickers(tickers)
                }
            compositeDisposable.add(disposable)
        }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}
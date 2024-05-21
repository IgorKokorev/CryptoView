package dev.kokorev.cryptoview.viewModel

import androidx.lifecycle.ViewModel
import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.data.PreferenceProvider
import dev.kokorev.cryptoview.domain.RemoteApi
import dev.kokorev.cryptoview.domain.Repository
import dev.kokorev.room_db.core_api.entity.CoinPaprikaTickerDB
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import javax.inject.Inject

class MainViewModel : ViewModel() {
    @Inject
    lateinit var remoteApi: RemoteApi
    @Inject
    lateinit var repository: Repository
    @Inject
    lateinit var preferences: PreferenceProvider
    private val compositeDisposable = CompositeDisposable()
//    val gainers: Observable<List<CoinPaprikaTickerDB>>
//    val losers: Observable<List<CoinPaprikaTickerDB>>
    val cpTickers: Observable<List<CoinPaprikaTickerDB>>


    init {
        App.instance.dagger.inject(this)
//        gainers = repository.getCPGainers()
//        losers = repository.getCPLosers()
        cpTickers = repository.getAllCoinPaprikaTickersFiltered(repository.getMinMcap(), repository.getMinVol())
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}
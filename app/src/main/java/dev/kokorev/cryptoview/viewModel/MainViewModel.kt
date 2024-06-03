package dev.kokorev.cryptoview.viewModel

import androidx.lifecycle.ViewModel
import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.data.preferencesLong
import dev.kokorev.cryptoview.domain.RemoteApi
import dev.kokorev.cryptoview.domain.Repository
import dev.kokorev.cryptoview.utils.CacheManager
import dev.kokorev.room_db.core_api.entity.CoinPaprikaTickerDB
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import javax.inject.Inject

class MainViewModel : ViewModel() {
    @Inject
    lateinit var remoteApi: RemoteApi
    @Inject
    lateinit var cacheManager: CacheManager
    @Inject
    lateinit var repository: Repository
    
    
    private var minMcap: Long by preferencesLong("minMcap")
    private var minVol: Long by preferencesLong("minVol")
    
    private val compositeDisposable = CompositeDisposable()
    val cpTickers: Observable<List<CoinPaprikaTickerDB>>



    init {
        App.instance.dagger.inject(this)
        cpTickers = repository.getAllCoinPaprikaTickersFiltered(minMcap, minVol)
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.dispose()
    }
}
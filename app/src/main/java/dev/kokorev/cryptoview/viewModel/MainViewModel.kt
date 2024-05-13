package dev.kokorev.cryptoview.viewModel

import androidx.lifecycle.ViewModel
import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.Constants
import dev.kokorev.cryptoview.domain.RemoteApi
import dev.kokorev.cryptoview.domain.Repository
import dev.kokorev.cryptoview.utils.Converter
import dev.kokorev.room_db.core_api.entity.TopMoverDB
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

class MainViewModel : ViewModel() {
    @Inject
    lateinit var remoteApi: RemoteApi
    @Inject
    lateinit var repository: Repository
    private var disposable: Disposable? = null

    val topMoversDB: Observable<List<TopMoverDB>>
//    val showProgressBar: BehaviorSubject<Boolean>

    init {
        App.instance.dagger.inject(this)
//        showProgressBar = interactor.progressBarState
        topMoversDB = repository.getTopMovers()
        loadTopMovers()
    }

    fun loadTopMovers() {
        val lastTime = repository.getLastTopMoversCallTime()
        // If enough time pasts call the API
        if (System.currentTimeMillis() > (lastTime + Constants.TOP_MOVERS_CALL_INTERVAL)) {
            disposable = remoteApi.getCoinPaprikaTop10Movers()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe {
                    repository.saveLastTopMoversCallTime()
                    val result = (it.losers + it.gainers).map { dto -> Converter.dtoToTopMover(dto) }
                    repository.saveTopMovers(result)
                }
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposable?.dispose()
    }
}
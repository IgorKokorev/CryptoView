package dev.kokorev.cryptoview.viewModel

import androidx.lifecycle.ViewModel
import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.Constants
import dev.kokorev.cryptoview.domain.RemoteApi
import dev.kokorev.cryptoview.domain.Repository
import dev.kokorev.cryptoview.utils.ConvertData
import dev.kokorev.room_db.core_api.entity.TopMover
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

class MainViewModel : ViewModel() {
    @Inject
    lateinit var remoteApi: RemoteApi

    @Inject
    lateinit var repository: Repository

    val topMovers: Observable<List<TopMover>>
//    val showProgressBar: BehaviorSubject<Boolean>

    init {
        App.instance.dagger.inject(this)
//        showProgressBar = interactor.progressBarState
        topMovers = repository.getTopMovers()
        loadTopMovers()
    }

    fun loadTopMovers() {
        val lastTime = repository.getLastTopMoversCallTime()
        // If enough time pasts call the API
        if (System.currentTimeMillis() > (lastTime + Constants.TOP_MOVERS_CALL_INTERVAL)) {
            repository.clearTopMovers()
            repository.saveLastTopMoversCallTime()
            remoteApi.getCoinPaprikaTop10Movers()
                .subscribeOn(Schedulers.io())
                .observeOn(Schedulers.io())
                .subscribe {
                    val result = (it.losers + it.gainers).map { dto -> ConvertData.dtoToTopMover(dto) }
                    repository.saveTopMovers(result)
                }
        }
    }
}
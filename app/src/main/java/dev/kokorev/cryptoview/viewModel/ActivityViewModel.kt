package dev.kokorev.cryptoview.viewModel

import androidx.lifecycle.ViewModel
import com.coinpaprika.apiclient.entity.PortfolioEvaluationDB
import dev.kokorev.cryptoview.appDagger
import dev.kokorev.cryptoview.backgroundService.AlarmScheduler
import dev.kokorev.cryptoview.domain.RemoteApi
import dev.kokorev.cryptoview.domain.Repository
import dev.kokorev.cryptoview.utils.NotificationService
import io.reactivex.rxjava3.core.Maybe
import javax.inject.Inject

class ActivityViewModel: ViewModel() {
    
    @Inject lateinit var remoteApi: RemoteApi
    @Inject lateinit var repository: Repository
    @Inject lateinit var notificationService: NotificationService
    @Inject lateinit var alarmScheduler: AlarmScheduler

    val numberPortfolioEvaluations: Maybe<Int>
    val portfolioEvaluations: Maybe<List<PortfolioEvaluationDB>>

    init {
        appDagger.inject(this)
        numberPortfolioEvaluations = repository.numberPortfolioEvaluations()
        portfolioEvaluations = repository.getAllPortfolioEvaluationsMaybe()
    }
    
    fun savePortfolioEvaluation(evaluation: PortfolioEvaluationDB) {
        repository.savePortfolioEvaluation(evaluation)
    }
}
package dev.kokorev.cryptoview

import android.app.Application
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.analytics.FirebaseAnalytics
import com.google.firebase.analytics.analytics
import dev.kokorev.binance_api.DaggerBinanceComponent
import dev.kokorev.cmc_api.DaggerCmcComponent
import dev.kokorev.coin_paprika_api.DaggerCoinPaprikaComponent
import dev.kokorev.cryptoview.databinding.ActivityMainBinding
import dev.kokorev.cryptoview.di.AppComponent
import dev.kokorev.cryptoview.di.DaggerAppComponent
import dev.kokorev.cryptoview.di.DbFacadeComponent
import dev.kokorev.cryptoview.di.modules.DomainModule
import dev.kokorev.room_db.core_api.dao.BinanceSymbolDao
import dev.kokorev.room_db.core_api.dao.CoinPaprikaTickerDao
import dev.kokorev.room_db.core_api.dao.FavoriteCoinDao
import dev.kokorev.room_db.core_api.dao.MessageDao
import dev.kokorev.room_db.core_api.dao.PortfolioEvaluationDao
import dev.kokorev.room_db.core_api.dao.PortfolioPositionDao
import dev.kokorev.room_db.core_api.dao.PortfolioTransactionDao
import dev.kokorev.room_db.core_api.dao.RecentCoinDao
import dev.kokorev.room_db.core_api.dao.TopMoverDao
import dev.kokorev.token_metrics_api.DaggerTokenMetricsComponent
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.subjects.BehaviorSubject
import javax.inject.Inject

lateinit var appDagger: AppComponent

class App : Application() {
    lateinit var dagger: AppComponent
    // Room DB external module daos
    @Inject lateinit var binanceSymbolDao: BinanceSymbolDao
    @Inject lateinit var topMoverDao: TopMoverDao
    @Inject lateinit var coinPaprikaTickerDao: CoinPaprikaTickerDao
    @Inject lateinit var favoriteCoinDao: FavoriteCoinDao
    @Inject lateinit var portfolioPositionDao: PortfolioPositionDao
    @Inject lateinit var portfolioEvaluationDao: PortfolioEvaluationDao
    @Inject lateinit var portfolioTransactionDao: PortfolioTransactionDao
    @Inject lateinit var recentCoinDao: RecentCoinDao
    @Inject lateinit var messageDao: MessageDao
    
    lateinit var activityBinding: ActivityMainBinding
    lateinit var analytics: FirebaseAnalytics
    
    // follow any change in notification permissions
    val notificationPermission: BehaviorSubject<Boolean> = BehaviorSubject.create()
    
    override fun onCreate() {
        super.onCreate()
        instance = this
        
        // Creating dagger component
        dagger = DaggerAppComponent.builder()
            .binanceProvider(DaggerBinanceComponent.create())
            .cmcProvider(DaggerCmcComponent.create())
            .coinPaprikaProvider(DaggerCoinPaprikaComponent.create())
            .tokenMetricsProvider(DaggerTokenMetricsComponent.create())
            .domainModule(DomainModule(this))
            .build()
        appDagger = dagger
        
        analytics = Firebase.analytics
        
        // initialising room db
        getDbFacade().inject(this)
    }

    private fun getDbFacade(): DbFacadeComponent {
        return dbFacadeComponent ?: DbFacadeComponent.init(this).also {
            dbFacadeComponent = it
        }
    }
    
    companion object {
        lateinit var instance: App
            private set
        private var dbFacadeComponent: DbFacadeComponent? = null
    }
    
}

fun Any.logd(log: String) {
    Log.d(this.javaClass.simpleName, log)
}

fun Any.logd(log: String, throwable: Throwable) {
    Log.d(this.javaClass.simpleName, log + "\nError: " + throwable.localizedMessage + "\nStackTrace:\n" + throwable.stackTraceToString())
}

fun Disposable.addToComposite(compositeDisposable: CompositeDisposable) {
    compositeDisposable.add(this)
}
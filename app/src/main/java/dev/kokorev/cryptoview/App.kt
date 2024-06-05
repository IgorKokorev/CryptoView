package dev.kokorev.cryptoview

import android.app.AlarmManager
import android.app.Application
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dev.kokorev.binance_api.DaggerBinanceComponent
import dev.kokorev.cmc_api.DaggerCmcComponent
import dev.kokorev.coin_paprika_api.DaggerCoinPaprikaComponent
import dev.kokorev.cryptoview.backgroundService.BinanceLoaderWorker
import dev.kokorev.cryptoview.backgroundService.PortfolioEvaluationService
import dev.kokorev.cryptoview.backgroundService.TickersLoaderWorker
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
import io.reactivex.rxjava3.subjects.BehaviorSubject
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class App : Application() {
    lateinit var dagger: AppComponent
    
    // Room DB external module daos
    @Inject
    lateinit var binanceSymbolDao: BinanceSymbolDao
    @Inject
    lateinit var topMoverDao: TopMoverDao
    @Inject
    lateinit var coinPaprikaTickerDao: CoinPaprikaTickerDao
    @Inject
    lateinit var favoriteCoinDao: FavoriteCoinDao
    @Inject
    lateinit var portfolioPositionDao: PortfolioPositionDao
    @Inject
    lateinit var portfolioEvaluationDao: PortfolioEvaluationDao
    @Inject
    lateinit var portfolioTransactionDao: PortfolioTransactionDao
    @Inject
    lateinit var recentCoinDao: RecentCoinDao
    @Inject
    lateinit var messageDao: MessageDao
    
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
        
        // initialising room db
        getDbFacade().inject(this)
        
        startWorks()
        startScheduledTasks()
    }
    
    private fun startScheduledTasks() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as? AlarmManager
        if (alarmManager == null) {
            Log.d(this.javaClass.simpleName, "Alarm manager can't be instantiated")
            return
        }
        
        val intent = Intent(applicationContext, PortfolioEvaluationService::class.java).apply {
            action = PORTFOLIO_EVALUATION_SERVICE_ACTION
        }
        val pendingIntent =
            PendingIntent.getService(
                applicationContext, PORTFOLIO_EVALUATION_SERVICE_REQUEST_ID, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_MUTABLE
            )
        
        alarmManager.setInexactRepeating(
            AlarmManager.RTC,
            System.currentTimeMillis() + AlarmManager.INTERVAL_FIFTEEN_MINUTES,
            AlarmManager.INTERVAL_FIFTEEN_MINUTES,
            pendingIntent
        )
    }
    
    private fun startWorks() {
        val workManager = WorkManager.getInstance(this)
        
        // periodic work request to load CoinPaprika tickers and check Favorites and Portfolio state
        val tickerLoaderWorkRequest =
            PeriodicWorkRequestBuilder<TickersLoaderWorker>(15, TimeUnit.MINUTES)
                .addTag(Constants.TICKER_LOADER_TAG)
                .build()
        workManager
            .enqueueUniquePeriodicWork(
                Constants.TICKER_LOADER_WORK,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                tickerLoaderWorkRequest
            )
        
        // periodic work request to download and save Binance symbols
        val binanceLoaderWorkRequest =
            PeriodicWorkRequestBuilder<BinanceLoaderWorker>(15, TimeUnit.MINUTES)
                .addTag(Constants.BINANCE_LOADER_TAG)
                .build()
        workManager
            .enqueueUniquePeriodicWork(
                Constants.BINANCE_LOADER_WORK,
                ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE,
                binanceLoaderWorkRequest
            )
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

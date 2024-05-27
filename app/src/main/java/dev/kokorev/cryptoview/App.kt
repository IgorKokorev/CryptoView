package dev.kokorev.cryptoview

import android.app.Application
import android.content.Intent
import android.util.Log
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkManager
import dev.kokorev.binance_api.DaggerBinanceComponent
import dev.kokorev.cmc_api.DaggerCmcComponent
import dev.kokorev.coin_paprika_api.DaggerCoinPaprikaComponent
import dev.kokorev.cryptoview.backgroundService.FavoritesCheckService
import dev.kokorev.cryptoview.backgroundService.TickersLoaderWorker
import dev.kokorev.cryptoview.data.Constants
import dev.kokorev.cryptoview.di.AppComponent
import dev.kokorev.cryptoview.di.DaggerAppComponent
import dev.kokorev.cryptoview.di.DbFacadeComponent
import dev.kokorev.cryptoview.di.modules.DomainModule
import dev.kokorev.room_db.core_api.BinanceSymbolDao
import dev.kokorev.room_db.core_api.CoinPaprikaTickerDao
import dev.kokorev.room_db.core_api.FavoriteCoinDao
import dev.kokorev.room_db.core_api.MessageDao
import dev.kokorev.room_db.core_api.PortfolioCoinDao
import dev.kokorev.room_db.core_api.RecentCoinDao
import dev.kokorev.room_db.core_api.TopMoverDao
import dev.kokorev.token_metrics_api.DaggerTokenMetricsComponent
import java.util.concurrent.TimeUnit
import javax.inject.Inject


class App : Application() {
    lateinit var dagger: AppComponent
    @Inject
    lateinit var binanceSymbolDao: BinanceSymbolDao
    @Inject
    lateinit var topMoverDao: TopMoverDao
    @Inject
    lateinit var coinPaprikaTickerDao: CoinPaprikaTickerDao
    @Inject
    lateinit var favoriteCoinDao: FavoriteCoinDao
    @Inject
    lateinit var portfolioCoinDao: PortfolioCoinDao
    @Inject
    lateinit var recentCoinDao: RecentCoinDao
    @Inject
    lateinit var messageDao: MessageDao
    private lateinit var intent: Intent

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

        // periodic work request to check Favorites coins and Portfolio state
        val workRequest = PeriodicWorkRequestBuilder<TickersLoaderWorker>(15, TimeUnit.MINUTES)
            .addTag(Constants.TICKER_LOADER_TAG)
            .build()

        WorkManager.getInstance(this)
            .enqueueUniquePeriodicWork(Constants.FAVORITE_CHECKER_WORK, ExistingPeriodicWorkPolicy.CANCEL_AND_REENQUEUE, workRequest)

        // start service to periodically check favorites
        intent = Intent(this, FavoritesCheckService::class.java)
//        startFavoriteCheckService()
        stopFavoriteCheckService()
    }

    fun startFavoriteCheckService() {
        Log.d(this.javaClass.simpleName, "Starting FavoriteCheckService")
        startService(intent)
    }

    fun stopFavoriteCheckService() {
        Log.d(this.javaClass.simpleName, "Stopping FavoriteCheckService")
        stopService(intent)
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

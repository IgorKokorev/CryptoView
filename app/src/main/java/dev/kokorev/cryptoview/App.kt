package dev.kokorev.cryptoview

import android.app.Application
import dev.kokorev.binance_api.DaggerBinanceComponent
import dev.kokorev.cmc_api.DaggerCmcComponent
import dev.kokorev.coin_paprika_api.DaggerCoinPaprikaComponent
import dev.kokorev.cryptoview.di.AppComponent
import dev.kokorev.cryptoview.di.DaggerAppComponent
import dev.kokorev.cryptoview.di.DbFacadeComponent
import dev.kokorev.cryptoview.di.modules.DomainModule
import dev.kokorev.room_db.core_api.BinanceSymbolDao
import dev.kokorev.room_db.core_api.CoinPaprikaTickerDao
import dev.kokorev.room_db.core_api.FavoriteCoinDao
import dev.kokorev.room_db.core_api.MessageDao
import dev.kokorev.room_db.core_api.RecentCoinDao
import dev.kokorev.room_db.core_api.TopMoverDao
import dev.kokorev.token_metrics_api.DaggerTokenMetricsComponent
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
    lateinit var recentCoinDao: RecentCoinDao
    @Inject
    lateinit var messageDao: MessageDao

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

/*
package dev.kokorev.cryptoview.backgroundService

import android.app.Service
import android.content.Intent
import android.os.Binder
import android.os.IBinder
import android.util.Log
import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.R
import dev.kokorev.cryptoview.Constants
import dev.kokorev.cryptoview.data.PreferenceProvider
import dev.kokorev.cryptoview.domain.Repository
import dev.kokorev.cryptoview.utils.Converter
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject
import kotlin.math.abs

class FavoritesCheckService : Service() {
    private val compositeDisposable = CompositeDisposable()
    @Inject
    lateinit var notificationService: NotificationService
    @Inject
    lateinit var repository: Repository
    @Inject
    lateinit var preferences: PreferenceProvider
    init {
        App.instance.dagger.inject(this)
    }

    override fun onCreate() {
        super.onCreate()
        Log.d(this.javaClass.simpleName, "onCreate")
        if (!preferences.toCheckFavorites()) {
            Log.d(this.javaClass.simpleName, "before stopSelf")
            stopSelf()
            Log.d(this.javaClass.simpleName, "after stopSelf")
        } // no need to do something if user doesn't want to get notifications

        val minChange = preferences.getFavoriteMinChange()
        Log.d(this.javaClass.simpleName, "minChange = $minChange")

        val disposable = Observable.combineLatest(
            repository.getFavoriteCoins(),
            repository.getAllCoinPaprikaTickers()
        ) { favorites, tickers ->
            val ids = favorites.map { favorite -> favorite.coinPaprikaId }
            val filtered = tickers.filter { db -> ids.contains(db.coinPaprikaId) }
            favorites.map { db -> Converter.favoriteCoinDBToFavoriteCoin(db, filtered) }
        }
            .subscribeOn(Schedulers.io())
            .observeOn(Schedulers.io())
            .subscribe { list ->
                Log.d(this.javaClass.simpleName, "DB change triggered")
                list.forEach { coin ->
                    val change = coin.percentChange ?: 0.0
                    Log.d(this.javaClass.simpleName, "Coin ${coin.symbol} has changed by ${change}%")
                    if (abs(change) >= minChange &&
                        coin.timeNotified + Constants.INTERVAL_TO_SHOW_FAVORITE_CHANGE < System.currentTimeMillis()
                    ) {
                        Log.d(this.javaClass.simpleName, "Sending notification")
                        repository.setFavoriteTimeNotified(coin)
                        notificationService.send(
                            getString(R.string.favorite_coin_price_change),
                            "Your favorite coin ${coin.symbol} has ${if (change > 0) "grown" else "fallen"} by $change%",
                            coin,
                            coin.id
                        )
                    }
                }
            }
        compositeDisposable.add(disposable)
    }

    override fun onBind(intent: Intent): IBinder {
        Log.d(this.javaClass.simpleName, "onBind")
        val binder = Binder()
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(this.javaClass.simpleName, "onStartCommand")
        return START_STICKY
    }

    override fun onDestroy() {
        super.onDestroy()
        Log.d(this.javaClass.simpleName, "onDestroy")
        compositeDisposable.clear()
    }
}*/

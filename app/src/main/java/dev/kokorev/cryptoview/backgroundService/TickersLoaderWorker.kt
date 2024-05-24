package dev.kokorev.cryptoview.backgroundService

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import androidx.work.rxjava3.RxWorker
import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.R
import dev.kokorev.cryptoview.data.Constants
import dev.kokorev.cryptoview.data.PreferenceProvider
import dev.kokorev.cryptoview.domain.RemoteApi
import dev.kokorev.cryptoview.domain.Repository
import dev.kokorev.cryptoview.utils.Converter
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject
import kotlin.math.abs

// RxJava Worker that periodically calls CoinPaprika API to get and save all the tickers
class TickersLoaderWorker(
    private val context: Context,
    private val params: WorkerParameters
) : RxWorker(context, params) {
    @Inject
    lateinit var repository: Repository
    @Inject
    lateinit var remoteApi: RemoteApi
    @Inject
    lateinit var notificationManager: NotificationManager
    @Inject
    lateinit var preferences: PreferenceProvider

    init {
        App.instance.dagger.inject(this)
    }

    override fun createWork(): Single<Result> {
        Log.d(this.javaClass.simpleName, "TickerLoader is running")
        Log.d(this.javaClass.simpleName, Thread.currentThread().name)

        val minMcap = Constants.minMCaps.get(0)
        val minVol = Constants.minVols.get(0)
        val minChange = preferences.getFavoriteMinChange()
        val toCheckFavorites = preferences.toCheckFavorites()

        val tickersSingle = remoteApi.getCoinPaprikaTickers()
            .map { list ->
                Log.d(this.javaClass.simpleName, "Tickers are loaded from CoinPaprika")
                val tickers = list
                    .filter { ticker ->
                        val quote = ticker.quotes?.get("USD")
                        if (quote == null) false
                        else {
                            ticker.rank > 0 &&
                                    quote.marketCap >= minMcap &&
                                    quote.dailyVolume >= minVol
                        }
                    }
                    .map { dto -> Converter.dtoToCoinPaprikaTicker(dto) }
                repository.addCoinPaprikaTickers(tickers)
                tickers
            }
            .doOnError {
                Log.d(
                    this.javaClass.simpleName,
                    "Error calling getCoinPaprikaTickers: " + it.localizedMessage,
                    it
                )
            }

        if (toCheckFavorites) {
            val favoriteSingle = repository.getFavoriteCoinsSingle()

            val result = Single.zip(tickersSingle, favoriteSingle) { tickers, favorites ->
                val ids = favorites.map { favorite -> favorite.coinPaprikaId }
                val filtered = tickers.filter { db -> ids.contains(db.coinPaprikaId) }
                favorites.map { db -> Converter.favoriteCoinDBToFavoriteCoin(db, filtered) }
            }
                .doOnSuccess { list ->
                    Log.d(this.javaClass.simpleName, "Checking Favorites")
                    list.forEach { coin ->
                        val change = coin.percentChange ?: 0.0
                        Log.d(this.javaClass.simpleName, "Coin ${coin.symbol} has changed by ${change}%")
                        if (abs(change) >= minChange &&
                            coin.timeNotified + Constants.INTERVAL_TO_SHOW_FAVORITE_CHANGE < System.currentTimeMillis()
                        ) {
                            Log.d(this.javaClass.simpleName, "Sending notification")
                            repository.setFavoriteTimeNotified(coin)
                            notificationManager.send(
                                context.getString(R.string.favorite_coin_price_change),
                                "Your favorite coin ${coin.symbol} has ${if (change > 0) "grown" else "fallen"} by $change%",
                                coin,
                                coin.id
                            )
                        }
                    }
                }
            return result.map { Result.success() }
        } else return tickersSingle.map { Result.success() }
    }
}
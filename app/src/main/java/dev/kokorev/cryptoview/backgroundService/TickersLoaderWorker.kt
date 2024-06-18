package dev.kokorev.cryptoview.backgroundService

import android.content.Context
import androidx.work.WorkerParameters
import androidx.work.rxjava3.RxWorker
import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.COIN_ACTION
import dev.kokorev.cryptoview.Constants
import dev.kokorev.cryptoview.R
import dev.kokorev.cryptoview.data.sharedPreferences.KEY_FAVORITE_CHANGE
import dev.kokorev.cryptoview.data.sharedPreferences.KEY_TO_CHECK_FAVORITES
import dev.kokorev.cryptoview.data.sharedPreferences.MIN_MCAPS
import dev.kokorev.cryptoview.data.sharedPreferences.MIN_VOLS
import dev.kokorev.cryptoview.data.sharedPreferences.preferencesBoolean
import dev.kokorev.cryptoview.data.sharedPreferences.preferencesFloat
import dev.kokorev.cryptoview.domain.RemoteApi
import dev.kokorev.cryptoview.domain.Repository
import dev.kokorev.cryptoview.logd
import dev.kokorev.cryptoview.utils.Converter
import dev.kokorev.cryptoview.utils.NotificationData
import dev.kokorev.cryptoview.utils.NotificationService
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject
import kotlin.math.abs

// RxJava Worker that periodically calls CoinPaprika API to get and save all the tickers
class TickersLoaderWorker(
    private val context: Context,
    params: WorkerParameters
) : RxWorker(context, params) {
    @Inject
    lateinit var repository: Repository
    @Inject
    lateinit var remoteApi: RemoteApi
    @Inject
    lateinit var notificationService: NotificationService
    
    private var toCheckFavorites: Boolean by preferencesBoolean(KEY_TO_CHECK_FAVORITES)
    private var favoriteChange: Float by preferencesFloat(KEY_FAVORITE_CHANGE)
    
    init {
        App.instance.dagger.inject(this)
    }

    override fun createWork(): Single<Result> {
        val minMcap = MIN_MCAPS.get(0)
        val minVol = MIN_VOLS.get(0)

        val tickersSingle = remoteApi.getCoinPaprikaTickers()
            .map { list ->
                logd("Tickers are loaded from CoinPaprika")
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
                repository.saveCoinPaprikaTickers(tickers)
                tickers
            }
            .doOnError {
                logd("Error calling getCoinPaprikaTickers: ", it)
            }

        if (toCheckFavorites) {
            val favoriteSingle = repository.getFavoriteCoinsSingle()

            val result = Single.zip(tickersSingle, favoriteSingle) { tickers, favorites ->
                val ids = favorites.map { favorite -> favorite.coinPaprikaId }
                val filtered = tickers.filter { db -> ids.contains(db.coinPaprikaId) }
                favorites.map { db -> Converter.favoriteCoinDBToFavoriteCoin(db, filtered) }
            }
                .doOnSuccess { list ->
                    logd("Checking Favorites")
                    list.forEach { coin ->
                        val change = coin.percentChange ?: 0.0
                        logd("Coin ${coin.symbol} has changed by ${change}%")
                        if (abs(change) >= favoriteChange &&
                            coin.timeNotified + Constants.SHOW_FAVORITE_CHANGE_TIME_MILLIS < System.currentTimeMillis()
                        ) {
                            logd("Sending notification")
                            repository.setFavoriteTimeNotified(coin)
                            val data = NotificationData(
                                title = context.getString(R.string.favorite_coin_price_change),
                                text = "Your favorite coin ${coin.symbol} has ${if (change > 0) "grown" else "fallen"} by $change%",
                                keyExtra = Constants.INTENT_EXTRA_FAVORITE_COIN,
                                extra = coin,
                                action = COIN_ACTION,
                                id = coin.id
                            )
                            notificationService.send(data)
                        }
                    }
                }
            return result.map { Result.success() }
        } else return tickersSingle.map { Result.success() }
    }
}
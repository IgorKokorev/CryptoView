package dev.kokorev.cryptoview.backgroundService

import android.content.Context
import android.util.Log
import androidx.work.WorkerParameters
import androidx.work.rxjava3.RxWorker
import dev.kokorev.cryptoview.App
import dev.kokorev.cryptoview.domain.RemoteApi
import dev.kokorev.cryptoview.domain.Repository
import dev.kokorev.cryptoview.logd
import dev.kokorev.cryptoview.utils.Converter
import dev.kokorev.cryptoview.utils.NotificationService
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

// RxJava Worker that periodically calls CoinPaprika API to get and save all the tickers
class BinanceLoaderWorker(
    context: Context,
    params: WorkerParameters
) : RxWorker(context, params) {
    @Inject
    lateinit var repository: Repository
    @Inject
    lateinit var remoteApi: RemoteApi
    @Inject
    lateinit var notificationService: NotificationService

    init {
        App.instance.dagger.inject(this)
    }

    override fun createWork(): Single<Result> {
        Log.d(this.javaClass.simpleName, "Start loading Binance info")
        return remoteApi.getBinanceInfo()
            .doOnSuccess { binanceSymbolList ->
                logd("Successfully loaded ${binanceSymbolList.binanceSymbolDTOS.size} symbols")
                val list = binanceSymbolList.binanceSymbolDTOS.map { dto ->
                    Converter.dtoToBinanceSymbol(dto)
                }
                repository.saveBinanceSymbols(list)
                logd("Symbols saved")
            }
            .map { Result.success() }
    }
}
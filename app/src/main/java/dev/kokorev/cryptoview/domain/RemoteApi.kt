package dev.kokorev.cryptoview.domain

import android.content.Context
import com.coinpaprika.apiclient.entity.CoinDetailsEntity
import com.coinpaprika.apiclient.entity.TopMoversEntity
import dev.kokorev.binance_api.BinanceApi
import dev.kokorev.binance_api.entity.Binance24hrStatsDTO
import dev.kokorev.binance_api.entity.Binance24hrStatsType
import dev.kokorev.binance_api.entity.BinanceAvgPriceDTO
import dev.kokorev.binance_api.entity.BinanceExchangeInfoDTO
import dev.kokorev.cmc_api.CmcApi
import dev.kokorev.cmc_api.entity.cmc_listing.CmcListingDTO
import dev.kokorev.cmc_api.entity.cmc_metadata.CmcMetadataDTO
import dev.kokorev.coin_paprika_api.CoinPaprikaApi
import dev.kokorev.cryptoview.R
import dev.kokorev.token_metrics_api.TokenMetricsApi
import dev.kokorev.token_metrics_api.entity.AiAnswer
import dev.kokorev.token_metrics_api.entity.AiQuestion
import dev.kokorev.token_metrics_api.entity.AiReport
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import retrofit2.HttpException


// Interactor to communicate with remote apis
class RemoteApi(
    private val context: Context,
    private val binanceApi: BinanceApi,
    private val cmcApi: CmcApi,
    private val coinPaprikaApi: CoinPaprikaApi,
    private val tokenMetricsApi: TokenMetricsApi
) {
    var progressBarState: BehaviorSubject<Boolean> = BehaviorSubject.create()

    // Binance API info
    fun getBinanceInfo(): Observable<BinanceExchangeInfoDTO> = binanceApi.getExchangeInfo().addProgressBar()

    fun getBinanceCurrentAvgPrice(symbol: String): Observable<BinanceAvgPriceDTO> =
        binanceApi.getCurrentAvgPrice(symbol).addProgressBar()

    fun getBinance24hrstats(
        symbol: String,
        type: Binance24hrStatsType = Binance24hrStatsType.FULL
    ): Observable<Binance24hrStatsDTO> = binanceApi.get24hrStats(symbol, type).addProgressBar()

    fun getBinance24hrstatsAll(type: Binance24hrStatsType = Binance24hrStatsType.MINI): Observable<List<Binance24hrStatsDTO>> =
        binanceApi.get24hrStatsAll(type).addProgressBar()


    // CoinMarketCap API info
    fun getCmcMetadata(symbol: String): Observable<CmcMetadataDTO> = cmcApi.getMetadata(symbol).addProgressBar()

    fun getCmcListingLatest(): Observable<CmcListingDTO> = cmcApi.getListingLatest().addProgressBar()


    // CoinPaprika API info
    fun getCoinPaprikaAllCoins() = coinPaprikaApi.getCoins().addProgressBar()
    fun getCoinPaprikaTop10Movers(): Observable<TopMoversEntity> = coinPaprikaApi.getTop10Movers().addProgressBar()
    fun getCoinPaprikaCoinInfo(id: String): Observable<CoinDetailsEntity> =
        coinPaprikaApi.getCoin(id).addProgressBar()

    fun getCoinPaprikaTicker(id: String) = coinPaprikaApi.getTicker(id).addProgressBar()
    fun getCoinPaprikaTickers() = coinPaprikaApi.getTickers().addProgressBar()
    fun getCoinPaprikaTickerHistorical(id: String) =
        coinPaprikaApi.getTickerHistoricalTicks(id).addProgressBar()

    // TokenMetrics API
    fun getAIReport(symbol: String): Observable<AiReport> {
        return tokenMetricsApi.getAiReports(symbol = symbol)
            .onErrorReturn { e ->
                return@onErrorReturn AiReport(
                    success = false,
                    message = exceptionToErrorText(e),
                    length = 0
                )
            }
            .addProgressBar()
    }

    fun askAi(aiQuestion: AiQuestion): Observable<AiAnswer> {
        return tokenMetricsApi.aiQuestion(aiQuestion)
            .onErrorReturn { e ->
                val emptyAnswer = AiAnswer().apply {
                    success = false
                    answer = exceptionToErrorText(e)
                }
                return@onErrorReturn emptyAnswer
            }
            .addProgressBar()
    }

    private fun exceptionToErrorText(e: Throwable) = if (e is HttpException) {
        httpCodeToError(e.code())
    } else "Unknown error"

    private fun httpCodeToError(code: Int): String =
        if (code == 400) context.getString(R.string.http400)
        else if (code == 429) context.getString(R.string.http429)
        else if (code == 500) context.getString(R.string.http500)
        else context.getString(R.string.unknown_http_error)

    private fun <T : Any> Observable<T>.addProgressBar(): Observable<T> {
        return this
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                progressBarState.onNext(true)
            }
            .doAfterTerminate {
                progressBarState.onNext(false)
            }
    }
}

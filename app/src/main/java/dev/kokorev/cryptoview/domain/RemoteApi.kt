package dev.kokorev.cryptoview.domain

import android.content.Context
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.vertexai.type.GenerateContentResponse
import com.google.firebase.vertexai.vertexAI
import dev.kokorev.binance_api.BinanceApi
import dev.kokorev.binance_api.entity.Binance24hrStatsType
import dev.kokorev.binance_api.entity.BinanceKLineInterval
import dev.kokorev.cmc_api.CmcApi
import dev.kokorev.coin_paprika_api.CoinPaprikaApi
import dev.kokorev.cryptoview.R
import dev.kokorev.token_metrics_api.TokenMetricsApi
import dev.kokorev.token_metrics_api.entity.AiAnswer
import dev.kokorev.token_metrics_api.entity.AiQuestion
import dev.kokorev.token_metrics_api.entity.AiReportData
import dev.kokorev.token_metrics_api.entity.TMMarketMetricsData
import dev.kokorev.token_metrics_api.entity.TMPricePredictionData
import dev.kokorev.token_metrics_api.entity.TMResponse
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Maybe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.schedulers.Schedulers
import io.reactivex.rxjava3.subjects.BehaviorSubject
import kotlinx.coroutines.rx3.rxMaybe
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
    val generativeModel = Firebase.vertexAI.generativeModel("gemini-1.5-flash-preview-0514")
    
    // Ask Google Gemini
    fun askGemini(question: String): Maybe<GenerateContentResponse> {
        return rxMaybe {
            generativeModel.generateContent(question)
        }
            .addProgressBar()
    }
    
    // Binance API info
    fun getBinanceInfo() = binanceApi.getExchangeInfo().addProgressBar()
    fun getBinanceCurrentAvgPrice(symbol: String) =
        binanceApi.getCurrentAvgPrice(symbol).addProgressBar()
    
    fun getBinance24hrStats(
        symbol: String,
        type: Binance24hrStatsType = Binance24hrStatsType.FULL
    ) = binanceApi.get24hrStats(symbol, type).addProgressBar()
    
    fun getBinance24hrStatsAll(type: Binance24hrStatsType = Binance24hrStatsType.MINI) =
        binanceApi.get24hrStatsAll(type).addProgressBar()
    
    fun getBinanceKLines(
        symbol: String,
        interval: BinanceKLineInterval = BinanceKLineInterval.HOUR,
        startTime: Long? = null,
        endTime: Long? = null,
        limit: Int = 100
    ) = binanceApi.getKLines(symbol, interval.value, startTime, endTime, limit).addProgressBar()
    
    
    // CoinMarketCap API info
    fun getCmcMetadata(symbol: String) = cmcApi.getMetadata(symbol).addProgressBar()
    
    fun getCmcListingLatest() = cmcApi.getListingLatest().addProgressBar()
    
    
    // CoinPaprika API info
    fun getCoinPaprikaAllCoins() = coinPaprikaApi.getCoins().addProgressBar()
    fun getCoinPaprikaTop10Movers() = coinPaprikaApi.getTop10Movers().addProgressBar()
    fun getCoinPaprikaCoinInfo(id: String) =
        coinPaprikaApi.getCoin(id).addProgressBar()
    
    fun getCoinPaprikaTicker(id: String) = coinPaprikaApi.getTicker(id)
        .addProgressBar()
    
    fun getCoinPaprikaTickers() = coinPaprikaApi.getTickers().addProgressBar()
    fun getCoinPaprikaTickerHistorical(id: String) =
        coinPaprikaApi.getTickerHistoricalTicks(id).addProgressBar()
    
    fun getCoinPaprikaOhlcvLatest(id: String) = coinPaprikaApi.getCoinOhlcvLatest(id).addProgressBar()
    
    // TokenMetrics API
    fun getAIReport(symbol: String): Maybe<TMResponse<AiReportData>> {
        return tokenMetricsApi.getAiReports(symbol = symbol)
            .onErrorReturn { e ->
                TMResponse(
                    success = false,
                    message = exceptionToErrorText(e),
                    length = 0
                )
            }
            .addProgressBar()
    }
    
    fun askTokenMetricsAi(aiQuestion: AiQuestion): Maybe<AiAnswer> {
        return tokenMetricsApi.aiQuestion(aiQuestion)
            .onErrorReturn { e ->
                val emptyAnswer = AiAnswer().apply {
                    success = false
                    answer = exceptionToErrorText(e)
                }
                emptyAnswer
            }
            .addProgressBar()
    }
    
    fun getSentiment() = tokenMetricsApi.getSentiment().addProgressBar()
    fun getMarketMetrics(
        startDate: String? = null,
        endDate: String? = null,
        limit: Int? = null,
        page: Int? = null,
    ): Maybe<TMResponse<TMMarketMetricsData>> =
        tokenMetricsApi.getMarketMetrics(startDate, endDate, limit, page).addProgressBar()
    
    fun getPricePrediction(
        tokenId: Int? = null,
        symbol: String? = null,
        category: String? = null,
        exchange: String? = null,
        limit: Int? = null,
        page: Int? = null,
    ): Maybe<TMResponse<TMPricePredictionData>> =
        tokenMetricsApi.getPricePrediction(tokenId, symbol, category, exchange, limit, page)
            .onErrorReturn {
                Log.d(this.javaClass.simpleName, "getPricePrediction error: ${it.localizedMessage}, ${it.stackTrace}")
                TMResponse(
                    false,
                    it.localizedMessage ?: exceptionToErrorText(it),
                    0
                )
            }
            .addProgressBar()
    
    
    // Service functions
    private fun exceptionToErrorText(e: Throwable) =
        if (e is HttpException) {
            httpCodeToError(e.code())
        } else "Unknown error"
    
    private fun httpCodeToError(code: Int): String =
        when (code) {
            400 -> context.getString(R.string.http400)
            429 -> context.getString(R.string.http429)
            500 -> context.getString(R.string.http500)
            else -> context.getString(R.string.unknown_http_error)
        }
    
    // Automatically turn progress bar on and off
    private fun <T : Any> Maybe<T>.addProgressBar(): Maybe<T> {
        return this
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                progressBarState.onNext(true)
            }
            .doOnTerminate {
                progressBarState.onNext(false)
            }
    }
    
    private fun <T : Any> Single<T>.addProgressBar(): Single<T> {
        return this
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                progressBarState.onNext(true)
            }
            .doOnTerminate {
                progressBarState.onNext(false)
            }
    }
    
    private fun <T : Any> Observable<T>.addProgressBar(): Observable<T> {
        return this
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doOnSubscribe {
                progressBarState.onNext(true)
            }
            .doOnTerminate {
                progressBarState.onNext(false)
            }
    }
}

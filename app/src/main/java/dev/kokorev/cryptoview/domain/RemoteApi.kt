package dev.kokorev.cryptoview.domain

import android.content.Context
import android.util.Log
import android.view.View
import android.widget.Toast
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
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.observers.DisposableObserver
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
    fun getBinanceInfo() : Observable<BinanceExchangeInfoDTO> = binanceApi.getExchangeInfo()

    fun getBinanceCurrentAvgPrice(symbol: String) : Observable<BinanceAvgPriceDTO> = binanceApi.getCurrentAvgPrice(symbol)

    fun getBinance24hrstats(symbol: String, type: Binance24hrStatsType = Binance24hrStatsType.FULL) : Observable<Binance24hrStatsDTO> = binanceApi.get24hrStats(symbol, type)

    fun getBinance24hrstatsAll(type: Binance24hrStatsType = Binance24hrStatsType.MINI) : Observable<List<Binance24hrStatsDTO>> = binanceApi.get24hrStatsAll(type)


    // CoinMarketCap API info
    fun getCmcMetadata(symbol: String) : Observable<CmcMetadataDTO> = cmcApi.getMetadata(symbol)

    fun getCmcListingLatest() : Observable<CmcListingDTO> = cmcApi.getListingLatest()


    // CoinPaprika API info
    fun getCoinPaprikaAllCoins() = coinPaprikaApi.getCoins()
    fun getCoinPaprikaTop10Movers() : Observable<TopMoversEntity> = coinPaprikaApi.getTop10Movers()
    fun getCoinPaprikaCoinInfo(id: String): Observable<CoinDetailsEntity> = coinPaprikaApi.getCoin(id)
    fun getCoinPaprikaTicker(id: String) = coinPaprikaApi.getTicker(id)
    fun getCoinPaprikaTickers() = coinPaprikaApi.getTickers()
    fun getCoinPaprikaTickerHistorical(id: String) = coinPaprikaApi.getTickerHistoricalTicks(id)

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
    }

    private fun exceptionToErrorText(e: Throwable) = if (e is HttpException) {
        httpCodeToError(e.code())
    } else "Unknown error"

    private fun httpCodeToError(code: Int): String =
        if (code == 400) context.getString(R.string.http400)
        else if (code == 429) context.getString(R.string.http429)
        else if (code == 500) context.getString(R.string.http500)
        else context.getString(R.string.unknown_http_error)
}

abstract class TokenMetricsObserver(private val view: View): DisposableObserver<AiAnswer>() {
    override fun onNext(t: AiAnswer) {
        onSuccess(t)
    }
    abstract fun onSuccess(t: AiAnswer)
    override fun onComplete() {}
    override fun onError(e: Throwable) {
        if (e is HttpException) {
            Toast.makeText(view.context, "Error code: " + e.code(), Toast.LENGTH_SHORT).show()
            Log.d("RemoteApi Error handling", "Error code: "+ e.code())
        }
    }

}

package dev.kokorev.binance_api

import dev.kokorev.binance_api.entity.Binance24hrStatsDTO
import dev.kokorev.binance_api.entity.Binance24hrStatsType
import dev.kokorev.binance_api.entity.BinanceAvgPriceDTO
import dev.kokorev.binance_api.entity.BinanceExchangeInfoDTO
import dev.kokorev.binance_api.entity.BinanceKLineInterval
import io.reactivex.rxjava3.core.Maybe
import retrofit2.http.GET
import retrofit2.http.Query

interface BinanceApi {

    @GET("/api/v3/exchangeInfo")
    fun getExchangeInfo() : Maybe<BinanceExchangeInfoDTO>

    @GET("/api/v3/klines")
    fun getKLines(
        @Query("symbol") symbol: String,
        @Query("interval") interval: BinanceKLineInterval,
        @Query("startTime") startTime: Long? = null,
        @Query("endTime") endTime: Long? = null,
        @Query("limit") limit: Int = 500
    ) : Maybe<ArrayList<ArrayList<Any>>>

    @GET("/api/v3/avgPrice")
    fun getCurrentAvgPrice(
        @Query("symbol") symbol: String
    ) : Maybe<BinanceAvgPriceDTO>

    @GET("/api/v3/ticker/24hr")
    fun get24hrStats(
        @Query("symbol") symbol: String,
        @Query("type") type: Binance24hrStatsType = Binance24hrStatsType.FULL
    ) : Maybe<Binance24hrStatsDTO>

    @GET("/api/v3/ticker/24hr")
    fun get24hrStatsAll(
        @Query("type") type: Binance24hrStatsType = Binance24hrStatsType.MINI
    ) : Maybe<List<Binance24hrStatsDTO>>

}

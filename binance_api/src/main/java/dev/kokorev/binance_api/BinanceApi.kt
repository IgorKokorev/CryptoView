package dev.kokorev.binance_api

import dev.kokorev.binance_api.entity.Binance24hrStatsDTO
import dev.kokorev.binance_api.entity.BinanceAvgPriceDTO
import dev.kokorev.binance_api.entity.BinanceExchangeInfoDTO
import dev.kokorev.binance_api.entity.BinanceKLineInterval
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET
import retrofit2.http.Query

interface BinanceApi {

    @GET("/api/v3/exchangeInfo")
    fun getExchangeInfo() : Observable<BinanceExchangeInfoDTO>

    @GET("/api/v3/klines")
    fun getKLines(
        @Query("symbol") symbol: String,
        @Query("interval") interval: BinanceKLineInterval,
        @Query("startTime") startTime: Long? = null,
        @Query("endTime") endTime: Long? = null,
        @Query("limit") limit: Int = 500
    ) : Observable<ArrayList<ArrayList<Any>>>

    @GET("/api/v3/avgPrice")
    fun getCurrentAvgPrice(
        @Query("symbol") symbol: String
    ) : Observable<BinanceAvgPriceDTO>

    @GET("GET /api/v3/ticker/24hr")
    fun get24hrstats(
        @Query("symbol") symbol: String
    ) : Observable<Binance24hrStatsDTO>

}

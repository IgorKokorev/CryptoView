package dev.kokorev.binance_api

import dev.kokorev.binance_api.entity.Binance24hrStatsDTO
import dev.kokorev.binance_api.entity.Binance24hrStatsType
import dev.kokorev.binance_api.entity.BinanceAvgPriceDTO
import dev.kokorev.binance_api.entity.BinanceExchangeInfoDTO
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Query

interface BinanceApi {

    @GET("/api/v3/exchangeInfo")
    fun getExchangeInfo() : Single<BinanceExchangeInfoDTO>

    @GET("/api/v3/klines")
    fun getKLines(
        @Query("symbol") symbol: String,
        @Query("interval") interval: String,
        @Query("startTime") startTime: Long? = null,
        @Query("endTime") endTime: Long? = null,
        @Query("limit") limit: Int = 500
    ) : Single<ArrayList<ArrayList<Any>>>

    @GET("/api/v3/avgPrice")
    fun getCurrentAvgPrice(
        @Query("symbol") symbol: String
    ) : Single<BinanceAvgPriceDTO>

    @GET("/api/v3/ticker/24hr")
    fun get24hrStats(
        @Query("symbol") symbol: String,
        @Query("type") type: Binance24hrStatsType = Binance24hrStatsType.FULL
    ) : Single<Binance24hrStatsDTO>

    @GET("/api/v3/ticker/24hr")
    fun get24hrStatsAll(
        @Query("type") type: Binance24hrStatsType = Binance24hrStatsType.MINI
    ) : Single<List<Binance24hrStatsDTO>>

}

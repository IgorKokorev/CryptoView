package dev.kokorev.coin_paprika_api

import com.coinpaprika.apiclient.entity.CoinDetailsEntity
import com.coinpaprika.apiclient.entity.CoinEntity
import com.coinpaprika.apiclient.entity.CoinType
import com.coinpaprika.apiclient.entity.EventEntity
import com.coinpaprika.apiclient.entity.ExchangeEntity
import com.coinpaprika.apiclient.entity.GlobalStatsEntity
import com.coinpaprika.apiclient.entity.MarketEntity
import com.coinpaprika.apiclient.entity.TickerEntity
import com.coinpaprika.apiclient.entity.TopMoversEntity
import com.coinpaprika.apiclient.entity.TweetEntity
import dev.kokorev.coin_paprika_api.entity.TickerTickEntity
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query
import retrofit2.http.QueryMap
import java.time.Instant

interface CoinPaprikaApi {
    @GET("global")
    fun getGlobalStats(): Single<GlobalStatsEntity>
    
    @GET("coins/{id}")
    fun getCoin(
        @Path("id") id: String
    ): Single<CoinDetailsEntity>

    @GET("coins")
    fun getCoins(
        @Query("additional_fields") additionalFields: String? = null
    ): Single<List<CoinEntity>>

    @GET("coins/{id}/events/")
    fun getEvents(
        @Path("id") id: String
    ): Single<List<EventEntity>>

    @GET("coins/{id}/exchanges/")
    fun getExchanges(
        @Path("id") id: String
    ): Single<List<ExchangeEntity>>

    @GET("coins/{id}/markets/")
    fun getMarkets(
        @Path("id") id: String,
        @Query("quotes") quotes: String
    ): Single<List<MarketEntity>>

    @GET("coins/{id}/twitter/")
    fun getTweets(
        @Path("id") id: String
    ): Single<List<TweetEntity>>

    @GET("rankings/top10movers/")
    fun getTop10Movers(
        @Query("type") type: CoinType? = null
    ): Single<TopMoversEntity>

    @GET("rankings/top-movers/")
    fun getMovers(
        @Query("results_number") results: Int = 20,
        @Query("marketcap_limit") range: String? = null
    ): Single<TopMoversEntity>

    @GET("tickers/{id}/")
    fun getTicker(
        @Path("id") id: String,
        @Query("quotes") quotes: String? = null,
        @QueryMap options: Map<String, String> = emptyMap()
    ): Single<TickerEntity>

    @GET("tickers")
    fun getTickers(
        @Query("quotes") quotes: String? =null,
        @Query("page") page: Int? = null,
        @QueryMap options: Map<String, String> = emptyMap()
    ): Single<List<TickerEntity>>

    @GET("tickers/{id}/historical")
    fun getTickerHistoricalTicks(
        @Path("id") id: String,
        @Query("interval") interval: String = "24h",
        @Query("start") start: Long = Instant.now().toEpochMilli() / 1000 - 60*60*24*364L
    ): Single<List<TickerTickEntity>>
}
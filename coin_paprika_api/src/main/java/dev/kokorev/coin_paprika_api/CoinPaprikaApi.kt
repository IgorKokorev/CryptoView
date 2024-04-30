package dev.kokorev.coin_paprika_api

import com.coinpaprika.apiclient.entity.CoinDetailsEntity
import com.coinpaprika.apiclient.entity.CoinEntity
import com.coinpaprika.apiclient.entity.EventEntity
import com.coinpaprika.apiclient.entity.ExchangeEntity
import com.coinpaprika.apiclient.entity.GlobalStatsEntity
import com.coinpaprika.apiclient.entity.MarketEntity
import com.coinpaprika.apiclient.entity.TopMoversEntity
import com.coinpaprika.apiclient.entity.TweetEntity
import io.reactivex.rxjava3.core.Observable
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface CoinPaprikaApi {
    @GET("global")
    fun getGlobalStats(): Observable<GlobalStatsEntity>
    
    @GET("coins/{id}")
    fun getCoin(
        @Path("id") id: String
    ): Observable<CoinDetailsEntity>

    @GET("coins")
    fun getCoins(
        @Query("additional_fields") additionalFields: String? = null
    ): Observable<List<CoinEntity>>

    @GET("coins/{id}/events/")
    fun getEvents(
        @Path("id") id: String
    ): Observable<List<EventEntity>>

    @GET("coins/{id}/exchanges/")
    fun getExchanges(
        @Path("id") id: String
    ): Observable<List<ExchangeEntity>>

    @GET("coins/{id}/markets/")
    fun getMarkets(
        @Path("id") id: String,
        @Query("quotes") quotes: String
    ): Observable<List<MarketEntity>>

    @GET("coins/{id}/twitter/")
    fun getTweets(
        @Path("id") id: String
    ): Observable<List<TweetEntity>>

    @GET("rankings/top10movers/")
    fun getTop10Movers(
        @Query("type") type: String? = null
    ): Observable<TopMoversEntity>

    @GET("rankings/top-movers/")
    fun getMovers(
        @Query("results_number") results: Int = 10,
        @Query("marketcap_limit") range: String? = null
    ): Observable<TopMoversEntity>
}
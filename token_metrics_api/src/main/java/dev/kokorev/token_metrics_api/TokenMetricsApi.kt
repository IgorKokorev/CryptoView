package dev.kokorev.token_metrics_api

import dev.kokorev.token_metrics_api.entity.TMAiAnswer
import dev.kokorev.token_metrics_api.entity.TMAiQuestion
import dev.kokorev.token_metrics_api.entity.TMAiReport
import dev.kokorev.token_metrics_api.entity.TMInvestorGrade
import dev.kokorev.token_metrics_api.entity.TMMarketMetrics
import dev.kokorev.token_metrics_api.entity.TMPricePredictionData
import dev.kokorev.token_metrics_api.entity.TMResponse
import dev.kokorev.token_metrics_api.entity.TMSentiment
import dev.kokorev.token_metrics_api.entity.TMTraderGrade
import io.reactivex.rxjava3.core.Maybe
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface TokenMetricsApi {
    @GET("ai-reports")
    fun getAiReports(
        @Query("token_id") tokenId: Int? = null,
        @Query("symbol") symbol: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("page") page: Int? = null,
    ): Maybe<TMResponse<TMAiReport>>

    @POST("tmai")
    fun aiQuestion(
        @Body TMAiQuestion: TMAiQuestion
    ) : Maybe<TMAiAnswer>

    @GET("market-metrics")
    fun getMarketMetrics(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("page") page: Int? = null,
    ): Maybe<TMResponse<TMMarketMetrics>>

    @GET("price-prediction")
    fun getPricePrediction(
        @Query("token_id") tokenId: Int? = null,
        @Query("symbol") symbol: String? = null,
        @Query("category") category: String? = null,
        @Query("exchange") exchange: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("page") page: Int? = null,
    ): Maybe<TMResponse<TMPricePredictionData>>

    @GET("trader-grades")
    fun getTraderGrades(
        @Query("token_id") tokenId: Int? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("symbol") symbol: String? = null,
        @Query("category") category: String? = null,
        @Query("exchange") exchange: String? = null,
        @Query("marketcap") marketcap: Double? = null,
        @Query("fdv") fdv: Double? = null,
        @Query("volume") volume: Double? = null,
        @Query("traderGrade") traderGrade: Double? = null,
        @Query("traderGradePercentChange") traderGradePercentChange: Double? = null,
        @Query("limit") limit: Int? = null,
        @Query("page") page: Int? = null,
    ): Maybe<TMResponse<TMTraderGrade>>

    @GET("investor-grades")
    fun getInvestorGrades(
        @Query("token_id") tokenId: Int? = null,
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("symbol") symbol: String? = null,
        @Query("category") category: String? = null,
        @Query("exchange") exchange: String? = null,
        @Query("marketcap") marketcap: Double? = null,
        @Query("fdv") fdv: Double? = null,
        @Query("volume") volume: Double? = null,
        @Query("investorGrade") traderGrade: Double? = null,
        @Query("limit") limit: Int? = null,
        @Query("page") page: Int? = null,
    ): Maybe<TMResponse<TMInvestorGrade>>

    @GET("sentiments")
    fun getSentiment(): Maybe<TMResponse<TMSentiment>>
}
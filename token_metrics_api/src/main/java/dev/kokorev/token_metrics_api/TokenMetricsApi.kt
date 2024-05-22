package dev.kokorev.token_metrics_api

import dev.kokorev.token_metrics_api.entity.AiAnswer
import dev.kokorev.token_metrics_api.entity.AiQuestion
import dev.kokorev.token_metrics_api.entity.AiReportData
import dev.kokorev.token_metrics_api.entity.TMInvestorGrade
import dev.kokorev.token_metrics_api.entity.TMMarketMetricsData
import dev.kokorev.token_metrics_api.entity.TMPricePredictionData
import dev.kokorev.token_metrics_api.entity.TMResponse
import dev.kokorev.token_metrics_api.entity.TMSentiment
import dev.kokorev.token_metrics_api.entity.TMTraderGrade
import io.reactivex.rxjava3.core.Observable
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
    ): Observable<TMResponse<AiReportData>>

    @POST("tmai")
    fun aiQuestion(
        @Body aiQuestion: AiQuestion
    ) : Observable<AiAnswer>

    @GET("market-metrics")
    fun getMarketMetrics(
        @Query("startDate") startDate: String? = null,
        @Query("endDate") endDate: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("page") page: Int? = null,
    ): Observable<TMResponse<TMMarketMetricsData>>

    @GET("price-prediction")
    fun getPricePrediction(
        @Query("token_id") tokenId: Int? = null,
        @Query("symbol") symbol: String? = null,
        @Query("category") category: String? = null,
        @Query("exchange") exchange: String? = null,
        @Query("limit") limit: Int? = null,
        @Query("page") page: Int? = null,
    ): Observable<TMResponse<TMPricePredictionData>>

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
    ): Observable<TMResponse<TMTraderGrade>>

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
    ): Observable<TMResponse<TMInvestorGrade>>

    @GET("sentiments")
    fun getSentiment(): Observable<TMResponse<TMSentiment>>
}